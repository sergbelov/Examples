package ru.utils.load.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.utils.db.DBService;
import ru.utils.files.PropertiesService;
import ru.utils.load.ScriptRun;
import ru.utils.load.graph.GraphProperty;
import ru.utils.load.data.testplan.TestPlans;
import ru.utils.load.data.testplan.TestPlan;
import ru.utils.load.runnable.RunnableLoadAPI;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiRun {
    private static final Logger LOG = LoggerFactory.getLogger(MultiRun.class);
    private static final String PROPERTIES_FILE = "load.properties";
    private static PropertiesService propertiesService = new PropertiesService(new LinkedHashMap<String, String>() {{
        put("STOP_TEST_ON_ERROR", "true");
        put("COUNT_ERROR_FOR_STOP_TEST", "100");
        put("WARM_DURATION", "60");

        put("JOBS_SAVE_TO_INFLUXDB", "false");
        put("JOBS_WAITING_TIME_MAX", "10");
        put("CountForBreak.BpmsJobEntityImpl", "1500");
        put("CountForBreak.BpmsTimerJobEntityImpl", "10000");
        put("CountForBreak.RetryPolicyJobEntityImpl", "100000");

        put("DB_URL", "");
        put("DB_USER_NAME", "");
        put("DB_USER_PASSWORD", "");

        put("InfluxDB.SAVE", "false");
        put("InfluxDB.URL", "http://localhost:8086");
        put("InfluxDB.USER_NAME", "admin");
        put("InfluxDB.PASSWORD", "admin");
        put("InfluxDB.DB_NAME", "bpm_load");
        put("InfluxDB.MEASUREMENT", "call");
        put("InfluxDB.Batch.Actions", "1000");
        put("InfluxDB.Batch.BufferLimit", "10000");
        put("InfluxDB.Batch.FlushDuration", "200");
        put("InfluxDB.Batch.JitterDuration", "0");

        put("CSM_URL", "");

        put("FILE_TEST_PLAN", "TestPlans.json");
        put("FILE_GRAFANA_GRAPHS", "GrafanaGraphs.json");
        put("PATH_REPORT", "Reports/");
    }});

//    private List<TestPlans> testPlansList; // = new ArrayList<>();
    private TestPlans[] testPlansArray;
    private List<MultiRunService> multiRunServiceList = new ArrayList<>();
    private GraphProperty graphProperty = new GraphProperty();
    private int apiMax = -1;

    private DBService dbService = null;
    private InfluxDB influxDB = null;

    public MultiRun() {
        propertiesService.readProperties(PROPERTIES_FILE);
        String fileTestPlan = propertiesService.getString("FILE_TEST_PLAN");
        ObjectMapper mapper = new ObjectMapper();
        try {
//            testPlansList = mapper.readValue(new File(FILE_TEST_PLAN), new TypeReference<List<TestPlans>>() {});
            testPlansArray = mapper.readValue(new File(fileTestPlan), TestPlans[].class);
        } catch (Exception e) {
            LOG.error("Ошибка при чтении данных из файла {}\n", fileTestPlan, e);
        }
    }

    public void end() {
        for (MultiRunService multiRunService : multiRunServiceList) {
            multiRunService.end();
        }
        if (dbService != null) {
            dbService.close();
        }
        if (influxDB != null) {
            influxDB.close();
        }
    }


    /**
     * Инициализация запуска сервисов (API) для заданного сценария (Класса)
     *
     * @param className
     */
    public boolean init(String className) {
        if (propertiesService.getString("DB_URL").isEmpty() ||
                connectToDB(
                        propertiesService.getString("DB_URL"),
                        propertiesService.getString("DB_USER_NAME"),
                        propertiesService.getStringDecode("DB_USER_PASSWORD"))) {

            connectToInfluxDB(); // подключение к InfluxDB

            for (TestPlans testPlans : testPlansArray) {
                if (testPlans.getClassName().equals(className)) {
                    for (TestPlan testPlan : testPlans.getTestPlanList()) {
                        apiMax++;
                        multiRunServiceList.add(new MultiRunService());
                        multiRunServiceList.get(apiMax).init(
                                this,
                                propertiesService,
                                testPlan,
                                dbService,
                                influxDB);
                    }
                    return true;
                }
            }
            LOG.error("Не найден план тестирования для {}", className);
            return false;
        } else {
            dbService.close();
            System.exit(-1);
        }
        return false;
    }

    /**
     * Подключение к InfluxDB
     */
    private void connectToInfluxDB(){
        if (propertiesService.getBoolean("InfluxDB.SAVE") && !propertiesService.getString("InfluxDB.URL").isEmpty()) {
            try {
                // подключение к InfluxDB
                influxDB = InfluxDBFactory.connect(
                        propertiesService.getString("InfluxDB.URL"),
                        propertiesService.getString("InfluxDB.USER_NAME"),
                        propertiesService.getStringDecode("InfluxDB.PASSWORD"));

//                if (!influxDB.databaseExists(propertiesService.getString("INFLUXDB_DB_NAME"))) {
//                    influxDB.createDatabase(propertiesService.getString("INFLUXDB_DB_NAME"));
//                }

                influxDB.setDatabase(propertiesService.getString("InfluxDB.DB_NAME"));

                influxDB.enableBatch( // параметры кеширования записи точек
                        BatchOptions.DEFAULTS
                                .actions(propertiesService.getInt("InfluxDB.Batch.Actions"))
                                .bufferLimit(propertiesService.getInt("InfluxDB.Batch.BufferLimit"))
                                .flushDuration(propertiesService.getInt("InfluxDB.Batch.FlushDuration"))
                                .jitterDuration(propertiesService.getInt("InfluxDB.Batch.JitterDuration"))
                                .exceptionHandler((points, throwable) -> LOG.error("Ошибка influxDB.write(point)", throwable)));

                if (1 == 2) { // отладка
                    long start = System.currentTimeMillis();
                    long stop = start + 1000;
                    long dur = stop - start;

                    Point point1 = Point.measurement(propertiesService.getString("InfluxDB.MEASUREMENT"))
                            .time(start, TimeUnit.MILLISECONDS)
                            .tag("type", "call")
//                        .tag("num", "1")
                            .tag("thread", "1")
                            .tag("api", "API1")
                            .tag("key", "KEY1")
                            .addField("i", 1)
                            .addField("duration", dur)
                            .build();
                    Point point2 = Point.measurement(propertiesService.getString("InfluxDB.MEASUREMENT"))
                            .time(start, TimeUnit.MILLISECONDS)
                            .tag("type", "call")
//                        .tag("num", "2")
                            .tag("thread", "2")
                            .tag("api", "API1")
                            .tag("key", "KEY1")
                            .addField("i", 1)
                            .addField("duration", dur)
                            .build();
                    Point point3 = Point.measurement(propertiesService.getString("InfluxDB.MEASUREMENT"))
                            .time(start, TimeUnit.MILLISECONDS)
                            .tag("type", "call")
//                        .tag("num", "3")
                            .tag("thread", "3")
                            .tag("api", "API2")
                            .tag("key", "KEY2")
                            .addField("i", 1)
                            .addField("duration", dur)
                            .build();
                    BatchPoints batchPoints = BatchPoints
                            .database(propertiesService.getString("InfluxDB.DB_NAME"))
//                            .retentionPolicy("defaultPolicy")
                            .build();
                    batchPoints.point(point1);
                    batchPoints.point(point2);
                    batchPoints.point(point3);
                    influxDB.write(batchPoints);

                    System.exit(0);
                }
            } catch (Exception e) {
                LOG.error("\nМетрики в базу сохраняться не будут.\nОшибка при подключении к InfluxDB: {}\n", propertiesService.getString("InfluxDB.URL"), e);
                influxDB = null;
            }
        }
    }


    public GraphProperty getGraphProperty() {
        return graphProperty;
    }

    /**
     * Список MultiRunService
     *
     * @return
     */
    public List<MultiRunService> getMultiRunServiceList() {
        return multiRunServiceList;
    }

    /**
     * Сервис для API по номеру
     *
     * @param apiNum
     * @return
     */
    public MultiRunService getMultiRunService(int apiNum) {
        return multiRunServiceList.get(apiNum);
    }

    public void start(ScriptRun baseScript) {
/*
        // отладка
        final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        long startTime = 0L, stopTime = 0L;
        try {
            startTime= sdf2.parse("01-01-2020 00:00:00").getTime();
            stopTime= sdf2.parse("10-02-2020 23:59:59").getTime();
        } catch (ParseException e) {
            LOG.error("", e);
        }
        LOG.info("\n{}", dataFromSQL.getDoubleCheck(startTime, stopTime));
        if (1==1) {
            return;
        }
*/

        if (apiMax == -1) {
            LOG.error("Не задано количество тестируемых сервисов");
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(apiMax + 1);
        ExecutorService executorService = Executors.newFixedThreadPool(apiMax + 1);

        for (MultiRunService multiRunService : multiRunServiceList) {
            executorService.submit(new RunnableLoadAPI(
                    multiRunService.getName(),
                    baseScript,
                    multiRunService,
                    countDownLatch));
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        executorService.shutdown();
    }

    public boolean isWarmingCompleted() {
        for (MultiRunService multiRunService : multiRunServiceList) {
            if (multiRunService.isWarming()) {
                return false;
            }
        }
        return true;
    }


    /**
     * Подключение к БД, создание пула
     *
     * @param dbUrl
     * @param dbUserName
     * @param dbPassword
     * @return
     */
    private boolean connectToDB(
            String dbUrl,
            String dbUserName,
            String dbPassword
    ) {
        dbService = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUserName)
                .dbPassword(dbPassword)
                .build();

        if (dbService.connectPooled(
                50,
                300,
                200,
                0)) {

            SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
//            if (1==1){return true;}
            // проверка занятости БПМ
//            String sql = sqlSelectBuilder.getCountJobEntityImpl("BpmsJobEntityImpl");
            String sql = sqlSelectBuilder.getCountJobsAll();
            try {
                boolean res = true;
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);
                ResultSet resultSet = dbService.executeQuery(statement, sql);
                if (resultSet.next()) { // есть задачи в статусе Running
                    int jobCount = resultSet.getInt("JobCount");
                    int timerJobCount = resultSet.getInt("TimerJobCount");
                    int retryPolicyJobCount = resultSet.getInt("RetryPolicyJobCount");
                    int cnt = jobCount + timerJobCount + retryPolicyJobCount;
                    if (cnt > 0) {
                        LOG.error("####\n" +
                                "Подача нагрузки не имеет смысла, в очереди имеются незавершенные процессы\n" +
                                "{}\n" +
                                "JobCount:      {}\n" +
                                "TimerJobCount: {}\n" +
                                "RetryPolicyJobCount: {}\n" +
                                "CountAll:      {}\n\n" +
                                "Дождитесь завершения обработки, либо выполните:\n {}",
                                sql,
                                jobCount,
                                timerJobCount,
                                retryPolicyJobCount,
                                cnt,
                                sqlSelectBuilder.getClearRunningProcess());
                        res = false;
                    }
                }
                resultSet.close();
                statement.close();
                connection.close();
                return res;
            } catch (SQLException e) {
                LOG.error("Ошибка при получении данных из БД\n", e);
                return false;
            }
        }
        return false;
    }

}
