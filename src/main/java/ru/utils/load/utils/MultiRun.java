package ru.utils.load.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
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
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);
    private static final String PROPERTIES_FILE = "load.properties";
    private static PropertiesService propertiesService = new PropertiesService(new LinkedHashMap<String, String>() {{
        put("STOP_TEST_ON_ERROR", "true");
        put("COUNT_ERROR_FOR_STOP_TEST", "100");
        put("WARM_DURATION", "60");

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

        put("GRAFANA_HOSTS_DETAIL", "");
        put("GRAFANA_HOSTS_DETAIL_CPU", "");
        put("GRAFANA_TRANSPORT_THREAD_POOLS", "");
        put("GRAFANA_TS", "");
        put("SPLUNK", "");
        put("CSM", "");

        put("FILE_TEST_PLAN", "TestPlans.json");
        put("PATH_REPORT", "Reports/");
    }});

//    private List<TestPlans> testPlansList; // = new ArrayList<>();
    private TestPlans[] testPlansArray;
    private List<MultiRunService> multiRunServiceList = new ArrayList<>();
    private GraphProperty graphProperty = new GraphProperty();
    private final boolean STOP_TEST_ON_ERROR;
    private final int COUNT_ERROR_FOR_STOP_TEST;
    private final int WARM_DURATION;
    private final String GRAFANA_HOSTS_DETAIL_URL;
    private final String GRAFANA_HOSTS_DETAIL_CPU_URL;
    private final String GRAFANA_TRANSPORT_THREAD_POOLS;
    private final String GRAFANA_TS;
    private final String SPLUNK_URL;
    private final String CSM_URL;
    private final String FILE_TEST_PLAN;
    private final String PATH_REPORT;
    private int apiMax = -1;

    private DBService dbService = null;
    private InfluxDB influxDB = null;

    public MultiRun() {
        propertiesService.readProperties(PROPERTIES_FILE);

        WARM_DURATION = propertiesService.getInt("WARM_DURATION");
        STOP_TEST_ON_ERROR = propertiesService.getBoolean("STOP_TEST_ON_ERROR");
        COUNT_ERROR_FOR_STOP_TEST = propertiesService.getInt("COUNT_ERROR_FOR_STOP_TEST");
        GRAFANA_HOSTS_DETAIL_URL = propertiesService.getString("GRAFANA_HOSTS_DETAIL");
        GRAFANA_HOSTS_DETAIL_CPU_URL = propertiesService.getString("GRAFANA_HOSTS_DETAIL_CPU");
        GRAFANA_TRANSPORT_THREAD_POOLS = propertiesService.getString("GRAFANA_TRANSPORT_THREAD_POOLS");
        GRAFANA_TS = propertiesService.getString("GRAFANA_TS");
        SPLUNK_URL = propertiesService.getString("SPLUNK");
        CSM_URL = propertiesService.getString("CSM");
        FILE_TEST_PLAN = propertiesService.getString("FILE_TEST_PLAN");
        PATH_REPORT = propertiesService.getString("PATH_REPORT");

        ObjectMapper mapper = new ObjectMapper();
        try {
//            testPlansList = mapper.readValue(new File(FILE_TEST_PLAN), new TypeReference<List<TestPlans>>() {});
            testPlansArray = mapper.readValue(new File(FILE_TEST_PLAN), TestPlans[].class);
        } catch (Exception e) {
            LOG.error("Ошибка при чтении данных из файла {}\n", FILE_TEST_PLAN, e);
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

/*
    testDuration    - сек
    vuStepTime      - сек
    vuStepTimeDelay - мс
    pacing          - мс
*/

            for (TestPlans testPlans : testPlansArray) {
                if (testPlans.getClassName().equals(className)) {
                    for (TestPlan testPlan : testPlans.getTestPlanList()) {
                        apiMax++;
                        multiRunServiceList.add(new MultiRunService());
                        multiRunServiceList.get(apiMax).init(
                                this,
                                testPlan.getApiNum(),
                                testPlan.getName(),
                                testPlan.isAsync(),
                                testPlan.getTestDuration_min(),
                                testPlan.getVuCountMin(),
                                testPlan.getVuCountMax(),
                                testPlan.getVuStepTime_sec(),
                                testPlan.getVuStepTimeDelay_ms(),
                                testPlan.getVuStepCount(),
                                testPlan.getPacing_ms(),
                                testPlan.getPacingType(),
                                testPlan.getResponseTimeMax_ms(),
                                WARM_DURATION,
                                STOP_TEST_ON_ERROR,
                                COUNT_ERROR_FOR_STOP_TEST,
                                GRAFANA_HOSTS_DETAIL_URL,
                                GRAFANA_HOSTS_DETAIL_CPU_URL,
                                GRAFANA_TRANSPORT_THREAD_POOLS,
                                GRAFANA_TS,
                                SPLUNK_URL,
                                CSM_URL,
                                dbService,
                                testPlan.getKeyBpm(),
                                PATH_REPORT,
                                influxDB,
                                propertiesService.getString("InfluxDB.DB_NAME"),
                                propertiesService.getString("InfluxDB.MEASUREMENT"));
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
                200,
                210,
                3,
                120,
                3,
                0)) {

            SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
//            if (1==1){return true;}
            // проверка занятости БПМ
            String sql = sqlSelectBuilder.getBpmsJobEntityImpl();
            try {
                boolean res = true;
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);
                ResultSet resultSet = dbService.executeQuery(statement, sql);
                if (resultSet.next()) { // есть задачи в статусе Running
                    int cnt = resultSet.getInt("cnt");
                    if (cnt > 0) {
                        LOG.error("####\n" +
                                "Подача нагрузки не имеет смысла, в очереди есть не завершенные процессы\n" +
                                "{}: {}\n" +
                                "Дождитесь завершения обработки, либо выполните:\n {}",
                                sql,
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
