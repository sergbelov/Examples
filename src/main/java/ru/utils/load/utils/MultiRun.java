package ru.utils.load.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.files.PropertiesService;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.metrics.MetricView;
import ru.utils.load.data.metrics.MetricViewGroup;
import ru.utils.load.data.testplan.TestPlans;
import ru.utils.load.data.testplan.TestPlan;
import ru.utils.load.runnable.RunnableLoadAPI;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
        put("PATH_REPORT", "Reports/");
    }});

    // список метрик
    // 0  - durMin
    // 1  - durAvg
    // 2  - dur90
    // 3  - durMax
    // 4  - tpc
    // 5  - tpcComplete
    // 6  - countCallAll
    // 7  - countCallComplete
    // 8  - db.bpms.COMPLETE
    // 9  - db.bpms.RUNNING
    // 10 - ошибки
    private final String[] vars = {"durMin", "durAvg", "dur90", "durMax",
            "tpc", "tpcComplete",
            "countCallAll", "countCallComplete",
            "COMPLETE", "RUNNING",
            "errors"};

    private List<MetricViewGroup> metricViewGroupList = new ArrayList<>();
    private List<TestPlans> testPlansList = new ArrayList<>();
    private List<MultiRunService> multiRunServiceList = new ArrayList<>();
    private DataFromSQL dataFromSQL = new DataFromSQL(); // получение данных из БД БПМ
    private final boolean STOP_TEST_ON_ERROR;
    private final int COUNT_ERROR_FOR_STOP_TEST;
    private final String GRAFANA_HOSTS_DETAIL_URL;
    private final String GRAFANA_HOSTS_DETAIL_CPU_URL;
    private final String GRAFANA_TRANSPORT_THREAD_POOLS;
    private final String SPLUNK_URL;
    private final String CSM_URL;
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
        PATH_REPORT = propertiesService.getString("PATH_REPORT");

        dataFromSQL.init( // подключаемся к БД
                propertiesService.getString("DB_URL"),
                propertiesService.getString("DB_USER_NAME"),
                propertiesService.getStringDecode("DB_USER_PASSWORD"));

        String fileNameLoadParameters = "TestPlans.json";
        Gson gson = new GsonBuilder() // с форматированием
                .setPrettyPrinting()
                .create();
        try(
                JsonReader reader = new JsonReader(new InputStreamReader(
                        new FileInputStream(fileNameLoadParameters),
                        "UTF-8"));
        )
        {
            testPlansList = gson.fromJson(reader, new TypeToken<List<TestPlans>>(){}.getType());
        } catch (Exception e) {
            LOG.error("Ошибка при чтении данных из файла {}\n", fileNameLoadParameters, e);
        }

        // === Графики

        // 0 - VU
        metricViewGroupList.add(new MetricViewGroup("Running Vusers",
                Arrays.asList(new MetricView(0, "", "#0000ff"))));

        // 1 - Длительность выполнения
        metricViewGroupList.add(new MetricViewGroup("Длительность выполнения",
                Arrays.asList(
                        new MetricView(0, "минимальная длительность (мс)", "#00009f"),
                        new MetricView(1, "средняя длительность (мс)", "#00af00"),
                        new MetricView(2, "перцентиль 90% (мс)", "#a0a000"),
                        new MetricView(3, "максимальная длительность (мс)", "#ff0000"))));

        // 2 - TPC
        metricViewGroupList.add(new MetricViewGroup("Количество операций в секунду (TPC)",
                Arrays.asList(
                        new MetricView(4, "TPC - отправлено", "#00009f"),
                        new MetricView(5, "TPC - выполнено", "#00af00"))));


        // 3 - Производительность БПМ
        metricViewGroupList.add(new MetricViewGroup("Производительность БПМ",
                Arrays.asList(
                        new MetricView(6, "отправлено запросов", "#00009f"),
                        new MetricView(8, "COMPLETE", "#00af00"),
                        new MetricView(9, "RUNNING", "#a0a000"))));

        // 4 - Ошибки
        metricViewGroupList.add(new MetricViewGroup("Ошибки",
                Arrays.asList(new MetricView(10, "", "#ff0000"))));

//        String[] colors = {"#00009f", "#00af00", "#afaf00", "#ff0000", "#00afaf", "#af00af"};

    }

    public void end(){
        dataFromSQL.end();
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
                            dataFromSQL,
                            testPlan.getKeyBpm(),
                            PATH_REPORT);
                }
                return true;
            }
        }
        LOG.error("Не найден план тестирования для {}", className);
        return false;
    }

    /**
     * Сервис для API по номеру
     * @param apiNum
     * @return
     */
    public MultiRunService getMultiRunService(int apiNum) {
        return multiRunServiceList.get(apiNum);
    }

    public void start(ScriptRun baseScript){
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
/*
        for (int i = 0; i <= apiMax; i++){
            executorService.submit(new RunnableLoadAPI(
                    multiRunServiceList.get(i).getName(),
                    baseScript,
                    multiRunServiceList.get(i),
                    countDownLatch));
        }
*/
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        executorService.shutdown();
    }

    public boolean isWarmingComplete(){
        for (MultiRunService multiRunService: multiRunServiceList){
            if (multiRunService.isWarming()){
                return false;
            }
        }
        return true;
    }

    public List<MetricViewGroup> getMetricViewGroupList() {
        return  metricViewGroupList;
    }

    public MetricViewGroup getMetricViewGroup(String title) {
        for (int i = 0; i < metricViewGroupList.size(); i++){
            if (metricViewGroupList.get(i).getTitle().equalsIgnoreCase(title)){
                return getMetricViewGroup(i);
            }
        }
        return null;
    }

    public MetricViewGroup getMetricViewGroup(int num) {
        return  metricViewGroupList.get(num);
    }

}
