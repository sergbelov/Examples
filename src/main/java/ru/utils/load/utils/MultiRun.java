package ru.utils.load.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.files.PropertiesService;
import ru.utils.load.ScriptRun;
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
        put("PATH_REPORT", "Reports/");
    }});

    private List<TestPlans> testPlansList = new ArrayList<>();
    private List<MultiRunService> multiRunService = new ArrayList<>();
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
    }

    public void end(){
        dataFromSQL.end();
    }


    /**
     * Инициализация запуска сервисов (API) для заданного сценария (Класса)
     * @param className
     */
    public void init(String className) {
        for (TestPlans testPlans : testPlansList) {
            if (testPlans.getClassName().equals(className)){
                for (TestPlan testPlan : testPlans.getTestPlanList()) {
                    apiMax++;
                    multiRunService.add(new MultiRunService());
                    multiRunService.get(apiMax).init(
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
                break;
            }
        }
    }

    /**
     * Сервис для API по номеру
     * @param apiNum
     * @return
     */
    public MultiRunService getMultiRunService(int apiNum) {
        return multiRunService.get(apiNum);
    }

    public void start(ScriptRun baseScript){
        CountDownLatch countDownLatch = new CountDownLatch(apiMax+1);
        ExecutorService executorService = Executors.newFixedThreadPool(apiMax+1);

        for (int i = 0; i <= apiMax; i++){
            executorService.submit(new RunnableLoadAPI(
                    multiRunService.get(i).getName(),
                    baseScript,
                    multiRunService.get(i),
                    countDownLatch));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        executorService.shutdown();
    }
}
