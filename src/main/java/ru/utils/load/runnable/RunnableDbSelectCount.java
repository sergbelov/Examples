package ru.utils.load.runnable;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.utils.load.data.DateTimeValues;
import ru.utils.load.utils.MultiRunService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Belov Sergey
 * Количество записей по заданному SQL - запросу
 */
public class RunnableDbSelectCount implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RunnableDbSelectCount.class);
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    private final String name;                      // наименование потока
    private final String type;                      // тип метрики
    private final String sql0;                      // sql - запрос
    private final long timeStep;                    // временной интервал между снятиями метрик (мс)
    private MultiRunService multiRunService;        //
    private List<DateTimeValues> sqlSelectCountList; // list для сохранеиня метрик
    private final int countForBreak;                // произойдет прерываени нагрузки при достижении данного значения ( > 0)
    private InfluxDB influxDB;
    private CountDownLatch countDownLatch;
    private List<Integer> metricCountList;

    private Map<String, Integer> prevCountMap = new HashMap<>();

    public RunnableDbSelectCount(
            String name,
            String type,
            String sql0,
            long timeStep,
            MultiRunService multiRunService,
            List<DateTimeValues> sqlSelectCountList,
            int countForBreak,
            InfluxDB influxDB,
            CountDownLatch countDownLatch,
            List<Integer> metricCountList

    ) {
        this.type = type;
        this.name = name + " " + type;
        this.sql0 = sql0;
        this.timeStep = timeStep;
        LOG.debug("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
        this.sqlSelectCountList = sqlSelectCountList;
        this.countForBreak = countForBreak;
        this.influxDB = influxDB;
        this.countDownLatch = countDownLatch;
        this.metricCountList = metricCountList;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
        if (multiRunService.getDbService() != null) {
            metricCountList.add(0);
            int num = metricCountList.size() - 1;
            Connection connection = multiRunService.getDbService().getConnection();
            Statement statement = multiRunService.getDbService().createStatement(connection);
            long start = System.currentTimeMillis();
            long stop = start + timeStep;
            long nextTime = start + timeStep * 2;
            int emptyCount = 0;
            while (System.currentTimeMillis() < multiRunService.getTestStopTime() ||
                    (System.currentTimeMillis() < (multiRunService.getTestStopTime() + multiRunService.getJobsWaitingTimeMax()) && emptyCount < 1)) {
                if (System.currentTimeMillis() >= nextTime) {
                    List<String> keyList = new ArrayList<>();
                    try {
                        String sql = sql0
                                .replace("{startTimeBegin}", sdf1.format(multiRunService.getTestStartTime()))
                                .replace("{startTime}", sdf1.format(start))
                                .replace("{stopTime}", sdf1.format(stop));
//                        LOG.info("{}", sql);

                        ResultSet resultSet = multiRunService.getDbService().executeQuery(statement, sql);
                        int countAll = 0;
                        long time = System.currentTimeMillis();
                        if (sql0.contains("{stopTime}")){
                            time = stop;
                        }
                        while (resultSet.next()) {
                            int cnt = resultSet.getInt("cnt");
                            countAll = countAll + cnt;
                            String jobType = "";
                            if (sql.contains(" as JobType")){
                                jobType = resultSet.getString("JobType");
                            }
                            String host = "";
                            if (sql.contains(" as host")){
                                host = resultSet.getString("host");
                            }
                            keyList.add(jobType + '.' + host);
                            if (cnt > 0 | getPrevCount(jobType, host, cnt) > 0) {

                                if (influxDB != null) {
                                    try {
                                        if (host.isEmpty()) {
                                            Point point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                                                    .time(time, TimeUnit.MILLISECONDS)
                                                    .tag("type", type)
                                                    .tag("thread", String.valueOf(1))
                                                    .tag("api", multiRunService.getName())
                                                    .tag("key", multiRunService.getProcessDefinitionKey())
                                                    .addField("i", 1)
                                                    .addField("count", cnt)
                                                    .build();
                                            influxDB.write(point);
                                        } else {
                                            Point point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                                                    .time(time, TimeUnit.MILLISECONDS)
                                                    .tag("type", type)
                                                    .tag("JobType", jobType)
                                                    .tag("host", host)
                                                    .tag("thread", String.valueOf(1))
                                                    .tag("api", multiRunService.getName())
                                                    .tag("key", multiRunService.getProcessDefinitionKey())
                                                    .addField("i", 1)
                                                    .addField("count", cnt)
                                                    .build();
                                            influxDB.write(point);
                                        }
                                    } catch (Exception e) {
                                        LOG.error("", e);
                                    }
                                }
                            }
                            if (countForBreak > 0 && cnt > countForBreak) { // прерываем подачу нагрузки из-за большой очереди
                                multiRunService.stop(sql + ": " + cnt);
                            }
                        }
                        resultSet.close();

                        sqlSelectCountList.add(new DateTimeValues(time, countAll));
                        LOG.info("{} (VU:{} Threads:{}): {}",
                                name,
                                multiRunService.getVuCount(),
                                multiRunService.getThreadCount(),
                                countAll);

                        metricCountList.set(num, countAll);
                        if (getCountAllJobs() == 0){
                            emptyCount++;
                        } else {
                            emptyCount = 0;
                        }

                    } catch (SQLException e) {
                        LOG.error("", e);
                    }
                    start = stop + 1;
                    stop = stop + timeStep;
                    nextTime = nextTime + timeStep;

                    // обнулим исчезнувшие записи
                    clearData(keyList, stop);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    LOG.error("", e);
                }
            }
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
        countDownLatch.countDown();
        LOG.info("Остановка потока {}", name);
    }

    /**
     * Общее количество записей во всех Jobs
     * @return
     */
    private int getCountAllJobs(){
        int countAll = 0;
        for (Integer count: metricCountList){
            LOG.debug("Количество записей в таблице: {}", count);
            countAll = countAll + count;
        }
        return countAll;
    }

    /**
     * Предыдущее значение
     * @param host
     * @param count
     * @return
     */
    private int getPrevCount(String jobType, String host, int count){
        int prevCount = 0;
        String key = jobType + '.' + host;
        if (prevCountMap.containsKey(key)){
            prevCount = prevCountMap.get(key);
        }
        prevCountMap.put(key, count);
        return prevCount;
    }

    /**
     * Обнулим значения по отсутсвующим записям
     * @param keyList
     */
    private void clearData(List<String> keyList, long stopTime){
        String key = null;
        try {
            for (Map.Entry<String, Integer> entry : prevCountMap.entrySet()) {
                if (entry.getValue() > 0) {
                    key = entry.getKey();
                    if (keyList.indexOf(key) == -1) {
                        prevCountMap.put(key, 0);
                        String[] keys = key.split("\\.");

                        if (influxDB != null) {
                            Point point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                                    .time(stopTime, TimeUnit.MILLISECONDS)
                                    .tag("type", type)
                                    .tag("JobType", keys[0])
                                    .tag("host", keys[1])
                                    .tag("thread", String.valueOf(1))
                                    .tag("api", multiRunService.getName())
                                    .tag("key", multiRunService.getProcessDefinitionKey())
                                    .addField("i", 1)
                                    .addField("count", 0)
                                    .build();

                            influxDB.write(point);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Ошибка при выполнении clearData {}\n", key, e);
        }
    }

}
