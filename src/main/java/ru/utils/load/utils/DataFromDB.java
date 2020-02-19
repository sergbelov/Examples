package ru.utils.load.utils;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import ru.utils.load.data.graph.VarInList;
import ru.utils.load.data.sql.DBData;
import ru.utils.load.data.sql.DBMetric;
import ru.utils.load.data.sql.DBResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;
import ru.utils.load.runnable.RunnableSelectDB;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сбор информации из БД БПМ
 */
public class DataFromDB {
    private static final Logger LOG = LogManager.getLogger(DataFromDB.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private DBService dbServiceCommon = null;
    private List<DBData> dbDataList = new CopyOnWriteArrayList<>();

    public DataFromDB() {
    }

    public void init(
            String dbUrl,
            String dbUser,
            String dbPassword) {

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        // подключаемся к БД БПМ
        dbServiceCommon = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUser)
                .dbPassword(dbPassword)
                .build();
        dbServiceCommon.connect();
    }

    /**
     * Новое подключение к БД
     * @return
     */
    public DBService getDbService(){
        if (dbUrl != null && !dbUrl.isEmpty()) {
            return new DBService.Builder()
                    .dbUrl(dbUrl)
                    .dbUserName(dbUser)
                    .dbPassword(dbPassword)
                    .build();
        } else {
            return null;
        }
    }

    /**
     * Отключаемся от БД
     */
    public void end() {
        if (dbServiceCommon != null) {
            dbServiceCommon.close();
        }
    }


    /**
     * Чтение данных из БД БПМ за период
     * Результат сохраняется в dbDataList
     * @param key
     * @param startTime
     * @param stopTime
     */
    public void getDataFromDbSelect(
            String key,
            long startTime,
            long stopTime
    ) {
        dbDataList.clear();
        long start = startTime;
        long stop;
        long step;
        int thread;
        if (stopTime - startTime > 1000 * 60) { // интервал больше минуты, сбор данных в несколько потоков
            thread = 10;
            step  = (stopTime - startTime) / thread;
        } else {
            thread = 1;
            step = stopTime - startTime;
        }
        CountDownLatch countDownLatch = new CountDownLatch(thread);
        ExecutorService executorService = Executors.newFixedThreadPool(thread);

        int cnt = 0;
        while (start < stopTime){
            stop = start + step;
            if (stop > stopTime){
                stop = stopTime;
            }
            LOG.info("{} - {}", sdf1.format(start), sdf1.format(stop));
            executorService.submit(new RunnableSelectDB(
                    ++cnt,
                    key,
                    start,
                    stop,
                    this,
                    countDownLatch,
                    dbDataList));
            start = stop + 1;
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        executorService.shutdown();
    }


    /**
     * Сбор статистики по выполнению процессов в БПМ
     * Для формирования результата используется предварительная подготовленные данные - dbDataList
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public DBResponse getStatisticsFromDb(
            String key,
            long startTime,
            long stopTime
    ) {
        LOG.debug("Статистика из БД BPM {} - {}", sdf1.format(startTime), sdf1.format(stopTime));
        String[] sql = {
                "select 1",

                "select2"
        };

        int[] count = {0, 0, 0, 0};
        long[] dur = {999999999999999999L, 0L, 0L, 0L}; // 0-min, 1-avg, 2-90%, 3-max
        
        dbDataList
                .stream()
                .filter(x -> (x.getStartTime() >= startTime && x.getStartTime() <= stopTime))
                .forEach(x -> {
                    count[2]++;
                    if (x.getDuration() != null) {
                        count[3]++;
                        dur[0] = Math.min(dur[0], x.getDuration()); // min
                        dur[1] = dur[1] + x.getDuration();          // avg
                        dur[3] = Math.max(dur[3], x.getDuration()); // max
                    }
                    
                    switch (x.getProcessState()){
                        case "COMPLETED":
                            count[0]++;
                            break;
                        case "RUNNING":
                            count[1]++;
                            break;
                        default:
                            LOG.warn("Не задан обработчик для статуса {}", x.getProcessState());
                    }
                });

        if (dur[0] == 999999999999999999L) {
            dur[0] = 0L;
        }

        if (count[3] > 0) {
            dur[1] = dur[1] / count[3]; // avg
            Percentile percentile90 = new Percentile();
            dur[2] = (long) percentile90.evaluate(
                    dbDataList
                            .stream()
                            .filter(x -> (x.getDuration() != null & x.getStartTime() >= startTime && x.getStopTime() <= stopTime))
                            .mapToDouble(DBData::getDuration)
                            .toArray(), 90);
        } else {
            dur[1] = 0; // avg
        }
        
        List<DBMetric> dbMetricList = new ArrayList<>();
        dbMetricList.add(new DBMetric(VarInList.DbCompleted, count[0]));
        dbMetricList.add(new DBMetric(VarInList.DbRunning, count[1]));
        dbMetricList.add(new DBMetric(VarInList.DurMin, dur[0]));
        dbMetricList.add(new DBMetric(VarInList.DurAvg, dur[1]));
        dbMetricList.add(new DBMetric(VarInList.Dur90, dur[2]));
        dbMetricList.add(new DBMetric(VarInList.DurMax, dur[3]));
        return new DBResponse(sql, dbMetricList);
    }

    /**
     * Проверка на дубли
     *
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getDoubleCheck(long startTime, long stopTime) {
        LOG.info("Поиск дублей в БД БПМ...");
        String sql = "select distinct PROCESSDEFINITIONKEY, PROCESSINSTANCEID, ACTIVITYID, min(STARTTIME) as STARTTIME, count(1) as cnt \n" +


                "where hai.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "group by PROCESSDEFINITIONKEY, processinstanceid, ACTIVITYID, EXECUTIONID, ACTIVITYNAME\n" +
                "having count(1) > 1\n" +
                "order by 4";
        int row = 0;
        StringBuilder res = new StringBuilder();
        if (dbServiceCommon != null && dbServiceCommon.isConnection()) {
            try {
                LOG.debug("Обработка данных SQL БПМ (дубли)...\n{}", sql);
                ResultSet resultSet = dbServiceCommon.executeQuery(sql);
                while (resultSet.next()) {
                    row++;
                    res.append("<tr>");

                    if (row == 1){
                        res.append("<td width=\"40\">");
                    } else {
                        res.append("<td>");
                    }
                    res.append(row)
                        .append("</td>");

                    if (row == 1){
                        res.append("<td width=\"300\">");
                    } else {
                        res.append("<td>");
                    }
                    res.append(resultSet.getString("PROCESSDEFINITIONKEY"))
                        .append("</td>");

                    if (row == 1){
                        res.append("<td width=\"300\">");
                    } else {
                        res.append("<td>");
                    }
                    res.append(resultSet.getString("PROCESSINSTANCEID"))
                        .append("</td>");

                    if (row == 1){
                        res.append("<td width=\"300\">");
                    } else {
                        res.append("<td>");
                    }
                    res.append(resultSet.getString("ACTIVITYID"))
                        .append("</td>");

                    if (row == 1){
                        res.append("<td width=\"200\">");
                    } else {
                        res.append("<td>");
                    }
                    res.append(resultSet.getString("STARTTIME"))
                        .append("</td>");

                    if (row == 1){
                        res.append("<td width=\"70\">");
                    } else {
                        res.append("<td>");
                    }
                    res.append(resultSet.getString("cnt"));
                    res.append("</td></tr>\n");
                }
                resultSet.close();
                LOG.debug("Обработка данных SQL БПМ (дубли) завершена.");
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
/*
// для отладки
        res.append("<tr><td width=\"40\">1</td><td width=\"300\">process01_CoreServiceStateless</td><td width=\"300\">2473bb61-4cb4-11ea-bc6c-fa163e9071c2</td><td width=\"300\">theEnd</td><td width=\"200\">2020-02-11 12:51:57.954</td><td width=\"70\">2</td></tr>\n" +
                "<tr><td>2</td><td>process01_CoreServiceStateless</td><td>29cc7267-4cb4-11ea-bc6c-fa163e9071c2</td><td>theEnd</td><td>2020-02-11 12:52:06.859</td><td>2</td></tr>\n" +
                "<tr><td>3</td><td>process01_CoreServiceStateless</td><td>2b9a05eb-4cb4-11ea-bc6c-fa163e9071c2</td><td>theEnd</td><td>2020-02-11 12:52:10.173</td><td>2</td></tr>\n" +
                "<tr><td>4</td><td>process01_CoreServiceStateless</td><td>2d710fce-4cb4-11ea-bc6c-fa163e9071c2</td><td>theEnd</td><td>2020-02-11 12:52:13.007</td><td>2</td></tr>\n");
        row = 4;
*/

        if (row > 0) {
            return "\n<br><table" + (row > 5 ? " class=\"scroll\"" : "") +">" +
                    "<caption>Дубли в БД БПМ<br>" + sql + "</caption>" +
                    "<thead>\n" +
                    "<tr><th width=\"40\"></th>" +
                    "<th width=\"300\">PROCESSDEFINITIONKEY</th>" +
                    "<th width=\"300\">PROCESSINSTANCEID</th>" +
                    "<th width=\"300\">ACTIVITYID</th>" +
                    "<th width=\"200\">STARTTIME(min)</th>" +
                    "<th width=\"90\">Количество</th></tr>\n" +
                    "</thead>\n<tbody>\n" +
                    res.toString() +
                    "</tbody></table>\n";
        } else {
            return "";
        }
    }

}
