package ru.utils.load.utils;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import ru.utils.db.DBService;
import ru.utils.load.data.DateTimeValues;
import ru.utils.load.data.StatData;
import ru.utils.load.data.Metric;
import ru.utils.load.data.sql.DBData;
import ru.utils.load.data.sql.DBMetric;
import ru.utils.load.data.sql.DBResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.runnable.RunnableDbSelectData;
import ru.utils.load.runnable.RunnableDbSelectTransitionsTime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Сбор информации из БД БПМ
 */
public class DataFromDB {
    private static final Logger LOG = LogManager.getLogger(DataFromDB.class);

    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private DBService dbService = null;
    private List<DBData> dbDataList = new CopyOnWriteArrayList<>();
    private List<DateTimeValues> countEndInSecList = new ArrayList<>();
    private long waitStartTime;
    private long waitStopTime;
    private Integer waitCountStart = null;
    private Integer waitCountStop = null;
    private long waitTime = 0L;
    private SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();


    public DataFromDB() {
    }

    public void init(DBService dbService) {
        this.dbService = dbService;
    }

    /**
     * Отключаемся от БД
     */
    public void end() {
    }


    /**
     * @return
     */
    public List<DateTimeValues> getCountEndInSecList() {
        return countEndInSecList;
    }

    public Integer getWaitCountStart() {
        return waitCountStart;
    }

    public Integer getWaitCountStop() {
        return waitCountStop;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public long getWaitStartTime() {
        return waitStartTime;
    }

    public long getWaitStopTime() {
        return waitStopTime;
    }

    /**
     * Чтение данных из БД БПМ за период
     * Результат сохраняется в dbDataList
     *
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
        int intervalForThread = 60; // длительность интервала в секундах для одного потока
        thread = (int) ((stopTime - startTime) / (1000 * intervalForThread));
        thread = Math.max(thread, 1);
        thread = Math.min(thread, 100);
        step = (stopTime - startTime) / thread;
        CountDownLatch countDownLatch = new CountDownLatch(thread);
        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        int cnt = 0;
        while (start < stopTime) {
            stop = start + step;
            if (stop > stopTime) {
                stop = stopTime;
            }
            LOG.info("SQL Select Data {} - {}", sdf1.format(start), sdf1.format(stop));
            executorService.submit(new RunnableDbSelectData(
                    ++cnt,
                    key,
                    start,
                    stop,
                    countDownLatch,
                    dbService,
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
     * Ожидания завершения начатых процессов (перед формированием отчета)
     * Если значение очереди остается без изменений последние 3 замера - ожидание прекращаем
     * Не более maxDelay минут
     *
     * @param key
     * @param startTime
     * @param stopTime
     */
    public void waitCompleteProcess(
            String key,
            long startTime,
            long stopTime,
            List<DateTimeValues> bpmsJobEntityImplCountList
    ) {
        int maxDelay = 10; // ждем не более минут
        if (dbService != null) {
            LOG.info("{}: Ожидаем завершения начатых задач (не более {} мин)...", key, maxDelay);
/*
        String sql = "select count(1) as cnt " +
                "from hpi " +
                "join pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "' " +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and hpi.processstate = 'RUNNING' " +
                "group by pdi.key, hpi.processstate";
*/
        String sql = "select count(1) as cnt " +
                "from  j " +
                "join  pdi on pdi.id = j.processdefinitionid and pdi.key = '" + key + "'";
            try {
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);
                int replay = 0;
                int prevCnt = 0;
                waitStartTime = System.currentTimeMillis();
                long stop = waitStartTime + (maxDelay * 60 * 1000);
                while (System.currentTimeMillis() < stop && replay < 3) {
                    ResultSet resultSet = dbService.executeQuery(statement, sql);
                    if (resultSet.next()) { // есть задачи в статусе Running
                        int cnt = resultSet.getInt("cnt");
                        bpmsJobEntityImplCountList.add(new DateTimeValues(
                                System.currentTimeMillis(),
                                cnt));
                        if (cnt > 0) {
                            if (waitCountStart == null) { // начальный размер очереди
                                waitCountStart = cnt;
                            }
                            if (prevCnt <= cnt) {
                                replay++;
                            } else {
                                replay = 0; // значение изменилось сбрасываем счетчик
                            }
                            prevCnt = cnt;
                            LOG.info("{}: Ожидаем завершения начатых задач... {} {}", key, cnt, (replay > 0 ? "(" + replay + ")" : ""));
                        } else {
                            replay = 3;
                        }
                    } else {
                        replay = 3;
                    }
                    resultSet.close();
                    if (replay < 3) {
                        Thread.sleep(20000);
                    }
                }
                statement.close();
                connection.close();
                waitStopTime = System.currentTimeMillis();
                waitCountStop = prevCnt; // конечный размер очереди
                waitTime = waitStopTime - waitStartTime;
            } catch (Exception e) {
                LOG.error("", e);
            }
            LOG.info("{}: Ожидание начатых задач завершено", key);
        }
    }

    /**
     * Сбор статистики по выполнению процессов в БПМ
     * Для формирования результата используется предварительная подготовленные данные - dbDataList
     *
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
/*
        if (sdf1.format(startTime).equals("27/02/2020 10:16:35.139")) {
            LOG.info("Отладка");
        }
*/
        LOG.debug("Статистика из БД BPM {} - {}", sdf1.format(startTime), sdf1.format(stopTime));
        String[] sql = {
                "select pdi.key, hpi.processstate, count(1) as cnt " +
                        "from hpi " +
                        "join pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "' " +
                        "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                        "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                        "group by pdi.key, hpi.processstate",

                "select pdi.key, count(1) as cnt, min(hpi.DURATIONINMILLIS), max(hpi.DURATIONINMILLIS), avg(hpi.DURATIONINMILLIS) " +
                        "from  hpi " +
                        "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "' " +
                        "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                        "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                        "and hpi.processstate = 'COMPLETED' " +
                        "group by pdi.key"
        };

        int[] count = {0, 0, 0, 0, 0};; // 0-COMPLETED, 1-RUNNING; 2-FAILED; 3-All; 4-All end
        double[] dur = {999999999999999999L, 0L, 0L, 0L}; // 0-min, 1-avg, 2-90%, 3-max

        dbDataList
                .stream()
                .filter(x -> (x.getStartTime() >= startTime && x.getStartTime() <= stopTime))
                .forEach(x -> {
                    count[3]++;
                    if (x.getDuration() != null) {
                        count[4]++;
                        dur[0] = Math.min(dur[0], x.getDuration()); // min
                        dur[1] = dur[1] + x.getDuration();          // avg
                        dur[3] = Math.max(dur[3], x.getDuration()); // max
                    }
                    switch (x.getProcessState()) {
                        case "COMPLETED":
                            count[0]++;
                            break;
                        case "RUNNING":
                            count[1]++;
                            break;
                        case "FAILED":
                            count[2]++;
                            break;
                        default:
                            LOG.warn("Не задан обработчик для статуса {}", x.getProcessState());
                    }
                });

        if (dur[0] == 999999999999999999L) {
            dur[0] = 0L;
        }

        if (count[4] > 0) {
            dur[1] = dur[1] / count[4] * 1.00; // avg
            Percentile percentile90 = new Percentile();
            dur[2] = percentile90.evaluate(
                    dbDataList
                            .stream()
                            .filter(x -> (x.getDuration() != null && x.getStartTime() >= startTime && x.getStartTime() <= stopTime))
                            .mapToDouble(DBData::getDuration)
                            .toArray(), 90);
        } else {
            dur[1] = 0; // avg
        }

        List<DBMetric> dbMetricList = new ArrayList<>();
        dbMetricList.add(new DBMetric(Metric.DB_COMPLETED, count[0]));
        dbMetricList.add(new DBMetric(Metric.DB_RUNNING, count[1]));
        dbMetricList.add(new DBMetric(Metric.DB_FAILED, count[2]));
        dbMetricList.add(new DBMetric(Metric.DB_DUR_MIN, dur[0]));
        dbMetricList.add(new DBMetric(Metric.DB_DUR_AVG, dur[1]));
        dbMetricList.add(new DBMetric(Metric.DB_DUR_90, dur[2]));
        dbMetricList.add(new DBMetric(Metric.DB_DUR_MAX, dur[3]));
        return new DBResponse(sql, dbMetricList);
    }

    /**
     * Количество шагов завершенных в секунду
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public StatData getCountStepCompleteInSec(
            String key,
            long startTime,
            long stopTime
    ) {
        LOG.debug("Количество шагов завершенных в секунду {} - {}", sdf1.format(startTime), sdf1.format(stopTime));
        String sql = "select\n" +
                "hpi.PROCESSSTATE, " +
                "to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS') as sec, " +
                "count(hpa.id) as cnt\n" +
                "from  hpi\n" +
                "join  hpa on hpa.PROCESSINSTANCEID = hpi.id\n" +
                "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "'\n" +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
//                "and hpi.PROCESSSTATE = 'COMPLETED'\n" +
                "group by to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS'), hpi.PROCESSSTATE\n" +
                "order by 2, 1";

        int countStep = 0;
        int countAll = 0;
        int countAllCompleted = 0;
        int countMin = 999999999;
        int countMax = 0;
        int count90 = 0;
        double countAvg = 0.00;

        try {
            LOG.debug("Обработка данных SQL БПМ (количество шагов завершенных в секунду)...\n{}", sql);
            Connection connection = dbService.getConnection();
            Statement statement = dbService.createStatement(connection);
            ResultSet resultSet = dbService.executeQuery(statement, sql);
            while (resultSet.next()) {
                String processstate = resultSet.getString("PROCESSSTATE");
                int cnt = resultSet.getInt("cnt");
                countAll = countAll + cnt;
                if (processstate.equals("COMPLETED")) {
                    long time = sdf2.parse(resultSet.getString("sec")).getTime();
                    countEndInSecList.add(new DateTimeValues(time, cnt));
                    countAllCompleted = countAllCompleted + cnt;
                    countMin = Math.min(countMin, cnt); // min
                    countAvg = countAvg + cnt;          // avg
                    countMax = Math.max(countMax, cnt); // max
                    countStep++;
                }
            }
            resultSet.close();
            statement.close();
            connection.close();
            LOG.debug("Обработка данных SQL БПМ (количество шагов завершенных в секунду) завершена.");
        } catch (Exception e) {
            LOG.error("", e);
        }

        if (countMin == 999999999) {
            countMin = 0;
        }
        if (countStep > 0) {
            countAvg = countAvg / countStep * 1.00; // avg
            Percentile percentile90 = new Percentile();
            count90 = (int) percentile90.evaluate(
                    countEndInSecList
                            .stream()
                            .mapToDouble(d -> d.getIntValue())
                            .toArray(), 90);
        } else {
            countAvg = 0; // avg
        }

        if (countAll > 0) {
            StringBuilder res = new StringBuilder();
            res.append("<tr><td>")
                    .append(decimalFormat.format(countAll))
                    .append("</td><td>")
                    .append(decimalFormat.format(countAllCompleted))
                    .append("</td><td>")
                    .append(decimalFormat.format(countMin))
                    .append("</td><td>")
                    .append(decimalFormat.format(countAvg))
                    .append("</td><td>")
                    .append(decimalFormat.format(count90))
                    .append("</td><td>")
                    .append(decimalFormat.format(countMax))
                    .append("</td></tr>");

            return new StatData(
                    countMin,
                    countAvg,
                    count90,
                    countMax,
                    Arrays.asList(
                            countAll,
                            countAllCompleted),
                    "\n<br><table><caption>Количество шагов завершенных в секунду<br>" + sql + "</caption>" +
                    "<thead>\n" +
                    "<tr><th rowspan=\"2\">Всего шагов</th>" +
                    "<th rowspan=\"2\">Всего шагов<br>COMPLETED</th>" +
                    "<th colspan=\"4\">Завершено в секунду</th></tr>" +
                    "<tr><th>MIN</th>" +
                    "<th>AVG</th>" +
                    "<th>90%</th>" +
                    "<th>MAX</th></tr>\n" +
                    "</thead>\n<tbody>\n" +
                    res.toString() +
                    "</tbody></table>\n");
        } else {
            return null;
        }
    }


    /**
     * Время затраченное на переходы между задачами процесса
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public StatData getTransitionsTime(
            String key,
            long startTime,
            long stopTime
    ) {
        long start = startTime;
        long stop;
        long step;
        int thread;
        int intervalForThread = 60; // длительность интервала в секундах для одного потока
        thread = (int) ((stopTime - startTime) / (1000 * intervalForThread));
        thread = Math.max(thread, 1);
        thread = Math.min(thread, 100);
        step = (stopTime - startTime) / thread;
        CountDownLatch countDownLatch = new CountDownLatch(thread);
        ExecutorService executorService = Executors.newFixedThreadPool(thread);

        AtomicInteger countMain = new AtomicInteger(0);
        AtomicLong durMin = new AtomicLong(999999999999999999L);
        AtomicLong durMax = new AtomicLong(0L);
        AtomicDouble durAvg = new AtomicDouble(0.00);
        List<Double> durList = new CopyOnWriteArrayList<>();
        long dur90 = 0L;

        int cnt = 0;
        while (start < stopTime) {
            stop = start + step;
            if (stop > stopTime) {
                stop = stopTime;
            }
            LOG.info("SQL Select TransitionsTime {} - {}", sdf1.format(start), sdf1.format(stop));
            executorService.submit(new RunnableDbSelectTransitionsTime(
                    ++cnt,
                    key,
                    start,
                    stop,
                    countDownLatch,
                    dbService,
                    countMain,
                    durMin,
                    durMax,
                    durAvg,
                    durList,
                    sqlSelectBuilder));
            start = stop + 1;
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        executorService.shutdown();

        if (durMin.get() == 999999999999999999L) {
            durMin.set(0);
        }
        if (countMain.get() > 0) {
//            LOG.info("{} {}", durAvg.get(), countMain.get());
            durAvg.set(durAvg.get() / countMain.get() * 1.00); // avg
            Percentile percentile90 = new Percentile();
            dur90 = (int) percentile90.evaluate(
                    durList
                            .stream()
                            .mapToDouble(d -> d)
                            .toArray(), 90);
        } else {
            durAvg.set(0); // avg
        }
        LOG.info("Обработка данных SQL БПМ (Время затраченное на переходы между задачами процесса) завершена.");
        if (countMain.get() > 0) {
            String sql = sqlSelectBuilder.getTransitionTime(key, startTime, stopTime);
            StringBuilder res = new StringBuilder();
            res.append("<tr><td>")
                    .append(decimalFormat.format(countMain.get()))
                    .append("</td><td>")
                    .append(decimalFormat.format(durMin.get()))
                    .append("</td><td>")
                    .append(decimalFormat.format(durAvg.get()))
                    .append("</td><td>")
                    .append(decimalFormat.format(dur90))
                    .append("</td><td>")
                    .append(decimalFormat.format(durMax.get()))
                    .append("</td></tr>");

            return new StatData(
                    durMin.get(),
                    durAvg.get(),
                    dur90,
                    durMax.get(),
                    Arrays.asList(countMain.get()),
                    "\n<br><table><caption>Время затраченное на переходы между задачами процесса<br>" + sql + "</caption>" +
                    "<thead>\n" +
                    "<tr><th rowspan=\"2\">Всего запросов<br>COMPLETED</th>" +
                    "<th colspan=\"4\">Длительность переходов (мс)</th></tr>" +
                    "<tr><th>MIN</th>" +
                    "<th>AVG</th>" +
                    "<th>90%</th>" +
                    "<th>MAX</th></tr>\n" +
                    "</thead>\n<tbody>\n" +
                    res.toString() +
                    "</tbody></table>\n");
        } else {
            return null;
        }
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
        String sql = "select distinct PROCESSDEFINITIONKEY, PROCESSINSTANCEID, ACTIVITYID, min(STARTTIME) as STARTTIME, count(1) as cnt\n" +
                "from  hai\n" +
                "where hai.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "group by PROCESSDEFINITIONKEY, processinstanceid, ACTIVITYID, EXECUTIONID, ACTIVITYNAME\n" +
                "having count(1) > 1\n" +
                "order by 4";
        int row = 0;
        StringBuilder res = new StringBuilder();
        try {
            LOG.debug("Обработка данных SQL БПМ (дубли)...\n{}", sql);
            Connection connection = dbService.getConnection();
            Statement statement = dbService.createStatement(connection);
            ResultSet resultSet = dbService.executeQuery(statement, sql);
            while (resultSet.next()) {
                row++;
                res.append("<tr>");

                if (row == 1) {
                    res.append("<td width=\"40\">");
                } else {
                    res.append("<td>");
                }
                res.append(row)
                        .append("</td>");

                if (row == 1) {
                    res.append("<td width=\"300\">");
                } else {
                    res.append("<td>");
                }
                res.append(resultSet.getString("PROCESSDEFINITIONKEY"))
                        .append("</td>");

                if (row == 1) {
                    res.append("<td width=\"300\">");
                } else {
                    res.append("<td>");
                }
                res.append(resultSet.getString("PROCESSINSTANCEID"))
                        .append("</td>");

                if (row == 1) {
                    res.append("<td width=\"300\">");
                } else {
                    res.append("<td>");
                }
                res.append(resultSet.getString("ACTIVITYID"))
                        .append("</td>");

                if (row == 1) {
                    res.append("<td width=\"200\">");
                } else {
                    res.append("<td>");
                }
                res.append(resultSet.getString("STARTTIME"))
                        .append("</td>");

                if (row == 1) {
                    res.append("<td width=\"70\">");
                } else {
                    res.append("<td>");
                }
                res.append(resultSet.getString("cnt"));
                res.append("</td></tr>\n");
            }
            resultSet.close();
            statement.close();
            connection.close();
            LOG.debug("Обработка данных SQL БПМ (дубли) завершена.");
        } catch (Exception e) {
            LOG.error("", e);
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
            return "\n<br><table" + (row > 5 ? " class=\"scroll\"" : "") + ">" +
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

    /**
     * Статистика по длительности выполнения задач
     *
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getProcessDuration(String key, long startTime, long stopTime) {
        LOG.info("Статистика по длительности выполнения задач...");
        String sql = sqlSelectBuilder.getProcessDuration(key, startTime, stopTime);
        int row = 0;
        StringBuilder res = new StringBuilder();
        try {
            LOG.debug("Обработка данных SQL БПМ (статистика по длительности выполнения задач)...\n{}", sql);
            Connection connection = dbService.getConnection();
            Statement statement = dbService.createStatement(connection);
            ResultSet resultSet = dbService.executeQuery(statement, sql);
            while (resultSet.next()) {
                row++;
                res.append("<tr>");

                res.append("<td>");
                res.append(row)
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("MAIN_PROCESS"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("PROCESSSTATE"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("ACTIVITYNAME"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("COUNT"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("MIN"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("MAX"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("AVG"))
                        .append("</td></tr>\n");
            }
            resultSet.close();
            statement.close();
            connection.close();
            LOG.debug("Обработка данных SQL БПМ (статистика по длительности выполнения задач) завершена.");
        } catch (Exception e) {
            LOG.error("", e);
        }

        if (row > 0) {
            return "\n<br><table><caption>Статистика по длительности выполнения задач<br\n" + sql + "\n</caption>" +
                    "<thead>\n" +
                    "<tr><th></th>" +
                    "<th>MAIN_PROCESS</th>" +
                    "<th>PROCESSSTATE</th>" +
                    "<th>ACTIVITYNAME</th>" +
                    "<th>COUNT</th>" +
                    "<th>MIN</th>" +
                    "<th>MAX</th>" +
                    "<th>AVG</th></tr>\n" +
                    "</thead>\n<tbody>\n" +
                    res.toString() +
                    "</tbody></table>\n";
        } else {
            return "";
        }
    }


}
