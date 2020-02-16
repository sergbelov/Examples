package ru.utils.load.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.files.PropertiesService;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.graph.GraphProperty;
import ru.utils.load.data.testplan.TestPlans;
import ru.utils.load.data.testplan.TestPlan;
import ru.utils.load.runnable.RunnableLoadAPI;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiRun {
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);
    private static final String PROPERTIES_FILE = "load.properties";
    private static PropertiesService propertiesService = new PropertiesService(new LinkedHashMap<String, String>() {{
        put("STOP_TEST_ON_ERROR", "true");
        put("COUNT_ERROR_FOR_STOP_TEST", "100");

        put("DB_URL", "");
        put("DB_USER_NAME", "");
        put("DB_USER_PASSWORD", "");

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
    }


    /**
     * Инициализация запуска сервисов (API) для заданного сценария (Класса)
     * @param className
     */
    public boolean init(String className) {
        for (TestPlans testPlans : testPlansList) {
            if (testPlans.getClassName().equals(className)){
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
                            testPlan.getStatisticsStepTime(),
                            STOP_TEST_ON_ERROR,
                            COUNT_ERROR_FOR_STOP_TEST,
                            GRAFANA_HOSTS_DETAIL_URL,
                            GRAFANA_HOSTS_DETAIL_CPU_URL,
                            GRAFANA_TRANSPORT_THREAD_POOLS,
                            SPLUNK_URL,
                            CSM_URL,
                            propertiesService.getString("DB_URL"),
                            propertiesService.getString("DB_USER_NAME"),
                            propertiesService.getStringDecode("DB_USER_PASSWORD"),
                            testPlan.getKeyBpm(),
                            PATH_REPORT);
                }
                return true;
            }
        }
        LOG.error("Не найден план тестирования для {}", className);
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

}
