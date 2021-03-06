package ru.utils.load.utils;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.utils.db.DBService;
import ru.utils.load.data.DateTimeValues;
import ru.utils.load.data.StatData;
import ru.utils.load.data.Metric;
import ru.utils.load.data.sql.DBData;
import ru.utils.load.data.sql.DBMetric;
import ru.utils.load.data.sql.DBResponse;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Сбор информации из БД БПМ
 */
public class DataFromDB {
    private static final Logger LOG = LoggerFactory.getLogger(DataFromDB.class);

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
                    dbDataList,
                    sqlSelectBuilder));
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
        String[] sql = {sqlSelectBuilder.getProcessesState(key, startTime, stopTime), sqlSelectBuilder.getProcessesDuration(key, startTime, stopTime)};

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
        String title = "Количество шагов завершенных в секунду";
        String sql = sqlSelectBuilder.getStepStopInSec(key, startTime, stopTime);
        int countStep = 0;
        int countAll = 0;
        int countAllCompleted = 0;
        int countMin = 999999999;
        int countMax = 0;
        int count90 = 0;
        double countAvg = 0.00;
        int row = 0;
        countEndInSecList.clear();

        try {
            LOG.debug("Обработка данных SQL ({})...\n{}", title, sql);
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
            LOG.debug("Обработка данных SQL ({}) завершена.", title);
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

                "\n<br><table style=\"width: 50%;\"" + (row > 10 ? " class=\"scroll\"" : "") + ">\n" +
                    "<thead>\n" +
                    "<tr><th colspan=\"6\">" + title + "</th></tr>\n" +
                    "<tr><td colspan=\"6\" align=\"Center\" style=\"font-size: 10px;\">" + sql + "</td></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"font-size: 10px;\">" +
                    "<th rowspan=\"2\">Всего шагов</th>" +
                    "<th rowspan=\"2\">Всего шагов<br>COMPLETED</th>" +
                    "<th colspan=\"4\">Завершено в секунду</th></tr>" +
                    "<tr><th>MIN</th>" +
                    "<th>AVG</th>" +
                    "<th>90%</th>" +
                    "<th>MAX</th></tr>\n" +
                    res.toString() +
                    "\n</tbody>\n</table>\n");
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
        String title = "Время затраченное на переходы между задачами процесса";
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
        LOG.info("Обработка данных SQL ({}) завершена.", title);
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

            int row = 0;
            return new StatData(
                    durMin.get(),
                    durAvg.get(),
                    dur90,
                    durMax.get(),
                    Arrays.asList(countMain.get()),
                    "\n<br><table style=\"width: 50%;\"" + (row > 10 ? " class=\"scroll\"" : "") + ">\n" +
                    "<thead>\n" +
                    "<tr><th colspan=\"5\">" + title + "</th></tr>\n" +
                    "<tr><td colspan=\"5\" align=\"Center\" style=\"font-size: 10px;\">" + sql + "</td></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"font-size: 10px;\">" +
                    "<th rowspan=\"2\">Всего запросов<br>COMPLETED</th>" +
                    "<th colspan=\"4\">Длительность переходов (мс)</th></tr>" +
                    "<tr><th>MIN</th>" +
                    "<th>AVG</th>" +
                    "<th>90%</th>" +
                    "<th>MAX</th></tr>\n" +
                    res.toString() +
                    "\n</tbody>\n</table>\n");
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
    public String getDuplicateCheck(long startTime, long stopTime) {
        String title = "Поиск дублей в БД БПМ";
        String sql = sqlSelectBuilder.getDuplicateCheck("", startTime, stopTime);
        int row = 0;
        StringBuilder res = new StringBuilder();
        try {
            LOG.debug("Обработка данных SQL ({})...\n{}", title, sql);
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
            LOG.debug("Обработка данных SQL ({}) завершена.", title);
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
            return "\n<br><table " + (row > 10 ? "style=\"width: 95%;\" class=\"scroll\"" : "") + ">\n" +
                    "<thead>\n" +
                    "<tr><th colspan=\"6\">" + title + "</th></tr>\n" +
                    "<tr><td colspan=\"6\" align=\"Center\" style=\"font-size: 10px;\">" + sql + "</td></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"font-size: 10px;\">" +
                    "<th></th>" +
                    "<th>PROCESSDEFINITIONKEY</th>" +
                    "<th>PROCESSINSTANCEID</th>" +
                    "<th>ACTIVITYID</th>" +
                    "<th>STARTTIME(min)</th>" +
                    "<th>Количество</th></tr>\n" +
                    res.toString() +
                    "\n</tbody>\n</table>\n";
        } else {
            return "";
        }
    }

    /**
     * Длительность выполнения задач
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getTaskDuration(String key, long startTime, long stopTime) {
        String title = "Длительность выполнения задач (информация из БД)";
        String sql = sqlSelectBuilder.getTaskDuration(key, startTime, stopTime);
        LOG.info("Обработка данных SQL БПМ ({})...\n{}", title, sql);
        int row = 0;
        StringBuilder res = new StringBuilder();
        try {
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
                res.append(resultSet.getString("root_process_name"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("process_name"))
                        .append("</td>");
                res.append("<td>");
                res.append(decimalFormat.format(resultSet.getDouble("root_process_min")))
                        .append("</td>");
                res.append("<td>");
                res.append(decimalFormat.format(resultSet.getDouble("root_process_max")))
                        .append("</td>");
                res.append("<td>");
                res.append(decimalFormat.format(resultSet.getDouble("root_process_avg")))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("PROCESSSTATE"))
                        .append("</td>");
                res.append("<td>");
                res.append(resultSet.getString("ACTIVITYNAME"))
                        .append("</td>");
                res.append("<td align=\"right\">");
                res.append(decimalFormat.format(resultSet.getInt("COUNT")))
                        .append("</td>");
                res.append("<td align=\"right\">");
                res.append(decimalFormat.format(resultSet.getInt("MIN")))
                        .append("</td>");
                res.append("<td align=\"right\">");
                res.append(decimalFormat.format(resultSet.getInt("MAX")))
                        .append("</td>");
                res.append("<td align=\"right\">");
                res.append(decimalFormat.format(resultSet.getDouble("AVG")))
                        .append("</td>");
                res.append("<td align=\"right\">");
                res.append(decimalFormat.format(resultSet.getInt("RetryCount")))
                        .append("</td></tr>\n");
            }
            resultSet.close();
            statement.close();
            connection.close();
            LOG.info("Обработка данных SQL БПМ ({}) завершена.", title);
        } catch (Exception e) {
            LOG.error("", e);
        }

        if (row > 0) {
            return  "\n<br><br>\n<table style=\"width: 95%;\"" + (row > 50 ? " class=\"scroll\"" : "") + ">" +
                    "\n<thead>\n" +
                    "<tr><th colspan=\"13\">" + title + "</th></tr>\n" +
                    "<tr><td colspan=\"13\" align=\"Center\" style=\"font-size: 10px;\">" + sql + "</td></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"font-size: 10px;\">" +
                    "<th></th>" +
                    "<th>root_process_name</th>" +
                    "<th>process_name</th>" +
                    "<th>root_process_min (ms)</th>" +
                    "<th>root_process_max (ms)</th>" +
                    "<th>root_process_avg (ms)</th>" +
                    "<th>PROCESSSTATE</th>" +
                    "<th>ACTIVITYNAME</th>" +
                    "<th>COUNT</th>" +
                    "<th>MIN (ms)</th>" +
                    "<th>MAX (ms)</th>" +
                    "<th>AVG (ms)</th>" +
                    "<th>Retry</th></tr>\n" +
                    res.toString() +
                    "</tbody></table>\n";
        } else {
            return "";
        }
    }

    /**
     * Количество записей
     * @param sql
     * @return
     */
    public int getCount(String sql) {
        return getCount(sql, dbService);
    }

    /**
     * Количество записей
     * @param sql
     * @param dbService
     * @return
     */
    public int getCount(String sql, DBService dbService) {
        int cnt = -1;
        try {
            Connection connection = dbService.getConnection();
            Statement statement = dbService.createStatement(connection);
            ResultSet resultSet = dbService.executeQuery(statement, sql);
            if (resultSet.next()) { // есть задачи в статусе Running
                cnt = resultSet.getInt("cnt");
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            LOG.error("Ошибка при выполении запроса:\n{}", sql, e);
        }
        return cnt;
    }

}
