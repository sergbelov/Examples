package ru.utils.load.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import ru.utils.db.DBService;
import ru.utils.files.PropertiesService;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.graph.GraphProperty;
import ru.utils.load.data.testplan.TestPlans;
import ru.utils.load.data.testplan.TestPlan;
import ru.utils.load.runnable.RunnableLoadAPI;

import java.io.FileInputStream;
import java.io.InputStreamReader;
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

        put("DB_URL", "");
        put("DB_USER_NAME", "");
        put("DB_USER_PASSWORD", "");

        put("INFLUXDB_URL", "http://localhost:8086");
        put("INFLUXDB_USER_NAME", "admin");
        put("INFLUXDB_PASSWORD", "admin");
        put("INFLUXDB_DB_NAME", "BPM_LOAD");
        put("INFLUXDB_MEASUREMENT", "CALL");

        put("GRAFANA_HOSTS_DETAIL", "");
        put("GRAFANA_HOSTS_DETAIL_CPU", "");
        put("GRAFANA_TRANSPORT_THREAD_POOLS", "");
        put("SPLUNK", "");
        put("CSM", "");

        put("FILE_TEST_PLAN", "TestPlans.json");
        put("PATH_REPORT", "Reports/");
    }});

    private List<TestPlans> testPlansList = new ArrayList<>();
    private List<MultiRunService> multiRunServiceList = new ArrayList<>();
    private GraphProperty graphProperty = new GraphProperty();
    private final boolean STOP_TEST_ON_ERROR;
    private final int COUNT_ERROR_FOR_STOP_TEST;
    private final String GRAFANA_HOSTS_DETAIL_URL;
    private final String GRAFANA_HOSTS_DETAIL_CPU_URL;
    private final String GRAFANA_TRANSPORT_THREAD_POOLS;
    private final String SPLUNK_URL;
    private final String CSM_URL;
    private final String FILE_TEST_PLAN;
    private final String PATH_REPORT;
    private int apiMax = -1;

    private DBService dbService = null;
    private InfluxDB influxDB = null;

    public MultiRun() {
        propertiesService.readProperties(PROPERTIES_FILE);

        STOP_TEST_ON_ERROR = propertiesService.getBoolean("STOP_TEST_ON_ERROR");
        COUNT_ERROR_FOR_STOP_TEST = propertiesService.getInt("COUNT_ERROR_FOR_STOP_TEST");
        GRAFANA_HOSTS_DETAIL_URL = propertiesService.getString("GRAFANA_HOSTS_DETAIL");
        GRAFANA_HOSTS_DETAIL_CPU_URL = propertiesService.getString("GRAFANA_HOSTS_DETAIL_CPU");
        GRAFANA_TRANSPORT_THREAD_POOLS = propertiesService.getString("GRAFANA_TRANSPORT_THREAD_POOLS");
        SPLUNK_URL = propertiesService.getString("SPLUNK");
        CSM_URL = propertiesService.getString("CSM");
        FILE_TEST_PLAN = propertiesService.getString("FILE_TEST_PLAN");
        PATH_REPORT = propertiesService.getString("PATH_REPORT");

        Gson gson = new GsonBuilder() // с форматированием
                .setPrettyPrinting()
                .create();
        try(
                JsonReader reader = new JsonReader(new InputStreamReader(
                        new FileInputStream(FILE_TEST_PLAN),
                        "UTF-8"));
        )
        {
            testPlansList = gson.fromJson(reader, new TypeToken<List<TestPlans>>(){}.getType());
        } catch (Exception e) {
            LOG.error("Ошибка при чтении данных из файла {}\n", FILE_TEST_PLAN, e);
        }
    }

    public void end(){
        for (MultiRunService multiRunService: multiRunServiceList){
            multiRunService.end();
        }
        if (dbService != null) {
            dbService.close();
        }
        if (influxDB != null){
            influxDB.close();
        }
    }


    /**
     * Инициализация запуска сервисов (API) для заданного сценария (Класса)
     * @param className
     */
    public boolean init(String className) {
        if (propertiesService.getString("DB_URL").isEmpty() ||
                getConnectToDB(
                    propertiesService.getString("DB_URL"),
                    propertiesService.getString("DB_USER_NAME"),
                    propertiesService.getStringDecode("DB_USER_PASSWORD"))) {

            try {
                influxDB = InfluxDBFactory.connect(
                        propertiesService.getString("INFLUXDB_URL"),
                        propertiesService.getString("INFLUXDB_USER_NAME"),
                        propertiesService.getString("INFLUXDB_PASSWORD"));

                if (!influxDB.databaseExists(propertiesService.getString("INFLUXDB_DB_NAME"))) {
                    influxDB.createDatabase(propertiesService.getString("INFLUXDB_DB_NAME"));
                }

/*
                long start = System.currentTimeMillis();
                long stop = start + 1000;
                Point point = Point.measurement(propertiesService.getString("INFLUXDB_MEASUREMENT"))
//                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .time(start, TimeUnit.MILLISECONDS)
                        .addField("api", "API")
                        .addField("key", "KEY")
                        .build();
                BatchPoints batchPoints = BatchPoints
                        .database(propertiesService.getString("INFLUXDB_DB_NAME"))
//                .retentionPolicy("defaultPolicy")
                        .build();
                batchPoints.point(point);
                influxDB.write(batchPoints);
*/

            } catch (Exception e) {
                LOG.error("Ошибка при подключении к InfluxDB: {}", propertiesService.getString("INFLUXDB_URL"));
            }

            for (TestPlans testPlans : testPlansList) {
                if (testPlans.getClassName().equals(className)) {
                    for (TestPlan testPlan : testPlans.getTestPlanList()) {
                        apiMax++;
                        multiRunServiceList.add(new MultiRunService());
                        multiRunServiceList.get(apiMax).init(
                                this,
                                testPlan.getApiNum(),
                                testPlan.getName(),
                                testPlan.isAsync(),
                                testPlan.getTestDuration(),
                                testPlan.getVuCountMin(),
                                testPlan.getVuCountMax(),
                                testPlan.getVuStepTime(),
                                testPlan.getVuStepTimeDelay(),
                                testPlan.getVuStepCount(),
                                testPlan.getPacing(),
                                testPlan.getPacingType(),
                                STOP_TEST_ON_ERROR,
                                COUNT_ERROR_FOR_STOP_TEST,
                                GRAFANA_HOSTS_DETAIL_URL,
                                GRAFANA_HOSTS_DETAIL_CPU_URL,
                                GRAFANA_TRANSPORT_THREAD_POOLS,
                                SPLUNK_URL,
                                CSM_URL,
                                dbService,
                                testPlan.getKeyBpm(),
                                PATH_REPORT,
                                influxDB,
                                propertiesService.getString("INFLUXDB_DB_NAME"),
                                propertiesService.getString("INFLUXDB_MEASUREMENT")                                );
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

    public GraphProperty getGraphProperty() { return graphProperty;}

    /**
     * Список MultiRunService
     * @return
     */
    public List<MultiRunService> getMultiRunServiceList() { return multiRunServiceList;}

    /**
     * Сервис для API по номеру
     * @param apiNum
     * @return
     */
    public MultiRunService getMultiRunService(int apiNum) {
        return multiRunServiceList.get(apiNum);
    }

    public void start(ScriptRun baseScript){
/*
        // отладка
        final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        long startTime = 0L, stopTime = 0L;
        try {
            startTime= sdf2.parse("01-01-2020 00:00:00").getTime();
            stopTime= sdf2.parse("10-02-2020 23:59:59").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
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
        CountDownLatch countDownLatch = new CountDownLatch(apiMax+1);
        ExecutorService executorService = Executors.newFixedThreadPool(apiMax+1);

        for (MultiRunService multiRunService: multiRunServiceList){
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

    public boolean isWarmingCompleted(){
        for (MultiRunService multiRunService: multiRunServiceList){
            if (multiRunService.isWarming()){
                return false;
            }
        }
        return true;
    }


    /**
     * Подключение к БД, создание пула
     * @param dbUrl
     * @param dbUserName
     * @param dbPassword
     * @return
     */
    private boolean getConnectToDB(
            String dbUrl,
            String dbUserName,
            String dbPassword
    ){
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

//            if (1==1){return true;}
            // проверка занятости БПМ
            String sql = "select count(1) as cnt from ";
            try {
                boolean res = true;
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);
                ResultSet resultSet = dbService.executeQuery(statement, sql);
                if (resultSet.next()) { // есть задачи в статусе Running
                    int cnt = resultSet.getInt("cnt");
                    if (cnt > 0){
                        LOG.error("####" +
                                "\nПодача нагрузки не имеет смысла, в очереди есть не завершенные процессы" +
                                "\nselect count(1) as cnt from : {}\n" +
                                "Дождитесь завершения обработки, либо выполните:\n" +
                                "--очистка очереди\n", cnt);
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
