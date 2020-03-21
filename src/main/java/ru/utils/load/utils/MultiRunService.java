package ru.utils.load.utils;

import org.influxdb.InfluxDB;
import ru.utils.db.DBService;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.*;
import ru.utils.load.data.errors.ErrorRsGroup;
import ru.utils.load.data.errors.ErrorRs;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.sql.DBResponse;
import ru.utils.load.runnable.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MultiRunService {
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private List<DateTimeValues> vuList = new CopyOnWriteArrayList<>(); // количество виртуальных пользователей на момент времени
    private List<VU> vuListActive = new CopyOnWriteArrayList<>(); // активность потоков
    private List<Call> callList = new CopyOnWriteArrayList<>(); // список вызовов сервиса

    private List<DateTimeValues> metricsList = new ArrayList<>();
    private List<DateTimeValues> bpmsJobEntityImplCountList = new CopyOnWriteArrayList<>();
    private List<DateTimeValues> retryPolicyJobEntityImplCountList = new CopyOnWriteArrayList<>();

    private List<ErrorRs> errorList = new CopyOnWriteArrayList<>(); // ошибки при выполнении API
    private List<ErrorRsGroup> errorRsGroupList = new ArrayList<>(); // количество ошибок по типам

    private ScriptRun baseScript;
    private ExecutorService executorService;

    private AtomicInteger threadCount; // // счетчик потоков
    private AtomicInteger vuCount; // текущее количество VU

    private AtomicBoolean running = new AtomicBoolean(true); // тест продолжается
    private AtomicBoolean warming = new AtomicBoolean(true); // прогрев
    private AtomicBoolean allowedAddVU = new AtomicBoolean(true); // разрешено добавление новых VU

    private AtomicInteger numberRequest = new AtomicInteger(0);

    private MultiRun multiRun;
    private DBService dbService;
    private int apiNum;
    private String name;
    private long testStartTime;
    private AtomicLong testStopTime = new AtomicLong(0);
    private long testStartTimeReal;
    private long testStopTimeReal;
    private long nextTimeAddVU;
    private Long prevStartTimeStatistic;
    private InfluxDB influxDB;
    private String influxDbBaseName;
    private String influxDbMeasurement;

    // параметры теста
    private boolean async; // асинхронный вызов сервиса

    private int warmDuration = 60;// длительность прогрева в секундах
    private int testDuration = 1; // длительность теста в минутах

    private int vuCountMin = 10;     // начальное количество виртуальных пользователей (VU)
    private int vuCountMax = 100;    // максимальное количество VU
    private int vuStepTime = 5;      // через какое время в секундах увеличиваем количество VU
    private long vuStepTimeDelay = 1;// задержка между стартами пользователей в группе (мс)
    private int vuStepCount = 5;     // на сколько увеличиваем количество VU

    private long pacing = 1000;    // задержка перед выполнением следующей итерации (ms)
    private int pacingType = 1;    // 0 - задержка от момента старта операции (без ожидания выполнения); 1 - задержка от момента старта операции (с учетом ожидания выполения); 2 - задержка от момента завершения выполнения операции;

    private long responseTimeMax_ms = 10000; // максимально допустимое значение Response time (мс)

    private boolean stopTestOnError = false; // прерывать тест при большом количестве ошибок
    private int countErrorForStopTest = 100; // количество ошибок для прерывания теста

    private String grafanaHostsDetailUrl; // Графана - Хосты детализованно (URL)
    private String grafanaHostsDetailCpuUrl; // Графана - Хосты детализованно CPU (URL)
    private String grafanaTransportThreadPoolsUrl; //Графана - TransportThreadPools (URL)
    private String grafanaTsUrl; //Графана - ТС (URL)
    private String splunkUrl; // Спланк (URL)
    private String csmUrl; // CSM (URL)

    private String processDefinitionKey;

    private String pathReport;

    private String[] sqlSelect = null;
    private DataFromDB dataFromDB = new DataFromDB(); // получение данных из БД БПМ
    private Report report = new Report();

    /**
     * инициализация параметров
     */
    public void init(
            MultiRun multiRun,
            int apiNum,
            String name,
            boolean async,
            int testDuration,
            int vuCountMin,
            int vuCountMax,
            int vuStepTime,
            long vuStepTimeDelay,
            int vuStepCount,
            long pacing,
            int pacingType,
            long responseTimeMax_ms,
            int warmDuration,
            boolean stopTestOnError,
            int countErrorForStopTest,
            String grafanaHostsDetailUrl,
            String grafanaHostsDetailCpuUrl,
            String grafanaTransportThreadPoolsUrl,
            String grafanaTsUrl,
            String splunkUrl,
            String csmUrl,
            DBService dbService,
            String processDefinitionKey,
            String pathReport,
            InfluxDB influxDB,
            String influxDbBaseName,
            String influxDbMeasurement
    ) {
        this.multiRun = multiRun;
        this.apiNum = apiNum;
        this.name = name;
        this.async = async;
        this.testDuration = testDuration;
        this.vuCountMin = vuCountMin;
        this.vuCountMax = vuCountMax;
        this.vuStepTime = vuStepTime;
        this.vuStepTimeDelay = vuStepTimeDelay;
        this.vuStepCount = vuStepCount;
        this.pacing = pacing;
        this.pacingType = pacingType;
        this.responseTimeMax_ms = responseTimeMax_ms;
        this.warmDuration = warmDuration;
        this.stopTestOnError = stopTestOnError;
        this.countErrorForStopTest = countErrorForStopTest;
        this.grafanaHostsDetailUrl = grafanaHostsDetailUrl;
        this.grafanaHostsDetailCpuUrl = grafanaHostsDetailCpuUrl;
        this.grafanaTransportThreadPoolsUrl = grafanaTransportThreadPoolsUrl;
        this.grafanaTsUrl = grafanaTsUrl;
        this.splunkUrl = splunkUrl;
        this.csmUrl = csmUrl;
        this.processDefinitionKey = processDefinitionKey;
        this.pathReport = pathReport;

        if (!checkParam()) { // ошибка в параметрах
            System.exit(1);
        }

        if (dbService != null) {
            this.dbService = dbService;
            dataFromDB.init(dbService);
        }
        this.influxDB = influxDB;
        this.influxDbBaseName = influxDbBaseName;
        this.influxDbMeasurement = influxDbMeasurement;
    }

    public void end() {
        dataFromDB.end();
    }

    public MultiRunService() {
    }

    public int getApiNum() {
        return apiNum;
    }

    public String getName() {
        return name;
    }

    public boolean isAsync() {
        return async;
    }

    public DBService getDbService() {
        return dbService;
    }

    public DataFromDB getDataFromDB() {
        return dataFromDB;
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }

    public String getInfluxDbBaseName() {
        return influxDbBaseName;
    }

    public String getInfluxDbMeasurement() {
        return influxDbMeasurement;
    }

    public MultiRun getMultiRun() {
        return multiRun;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public List<DateTimeValues> getVuList() {
        return vuList;
    }

    public List<DateTimeValues> getMetricsList() {
        return metricsList;
    }

    public List<DateTimeValues> getBpmsJobEntityImplCountList() {
        return bpmsJobEntityImplCountList;
    }

    public List<DateTimeValues> getRetryPolicyJobEntityImplCountList() {
        return retryPolicyJobEntityImplCountList;
    }

    public List<Call> getCallList() {
        return callList;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public List<ErrorRs> getErrorList() {
        return errorList;
    }

    public List<ErrorRsGroup> getErrorRsGroupList() {
        return errorRsGroupList;
    }

    public long getTestStartTime() {
        return testStartTime;
    }

    public long getTestStopTime() {
        return testStopTime.get();
    }

    public long getTestStartTimeReal() {
        return testStartTimeReal;
    }

    public long getTestStopTimeReal() {
        return testStopTimeReal;
    }

    public long getPacing() {
        return pacing;
    }

    public long getResponseTimeMax_ms() {
        return responseTimeMax_ms;
    }

    public int getPacingType() {
        return pacingType;
    }

    public String getGrafanaHostsDetailUrl() {
        return grafanaHostsDetailUrl;
    }

    public String getGrafanaHostsDetailCpuUrl() {
        return grafanaHostsDetailCpuUrl;
    }

    public String getGrafanaTransportThreadPoolsUrl() {
        return grafanaTransportThreadPoolsUrl;
    }

    public String getGrafanaTsUrl() {
        return grafanaTsUrl;
    }

    public String getSplunkUrl() {
        return splunkUrl;
    }

    public String getCsmUrl() {
        return csmUrl;
    }

    public String getSqlSelect(int num) {
        return sqlSelect[num];
    }

    public AtomicInteger getNumberRequest() {
        return numberRequest;
    }

    /**
     * Количество активных потоков
     */
    public int getThreadCount() {
        return threadCount.get();
    }

    /**
     * Новый поток
     */
    public int startThread() {
        return threadCount.incrementAndGet();
    }

    /**
     * Завершен поток
     */
    public int stopThread() {
        return threadCount.decrementAndGet();
    }

    /**
     * Параметры теста
     *
     * @return
     */
    public String getParams() {
        StringBuilder res = new StringBuilder("\n<h3>Параметры<h3>\n" +
                "<table border=\"1\"><tbody>\n");
        res.append("<tr><td>Синхронный вызов сервиса</td><td>")
                .append(async ? "Нет" : "Да")
                .append("</td></tr>\n")
                .append("<td>Длительность теста (мин)</td><td>")
                .append(testDuration)
                .append("</td></tr>\n")
                .append("<tr><td>Задержка перед выполнением следующей операции (мс)</td><td>")
                .append(pacing)
                .append("</td></tr>\n")
                .append("<tr><td>Режим задержки:<br>0 - задержка от момента старта операции (без ожидания выполнения);<br>1 - задержка от момента старта операции (с учетом ожидания выполения);<br>2 - задержка от момента завершения выполнения операции;</td><td>")
                .append(pacingType)
                .append("</td></tr>\n")
                .append("<tr><td>Максимально допустимое значение Response time (мс)</td><td>")
                .append(responseTimeMax_ms)
                .append("</td></tr>\n")
                .append("<tr><td>Начальное количество VU</td><td>")
                .append(vuCountMin)
                .append("</td></tr>\n")
                .append("<tr><td>Периодичность увеличения VU (сек)</td><td>")
                .append(vuStepTime)
                .append("</td></tr>\n")
                .append("<tr><td>Количество VU в группе</td><td>")
                .append(vuStepCount)
                .append("</td></tr>\n")
                .append("<tr><td>Задержка между стартами VU в группе (сек)</td><td>")
                .append(vuStepTimeDelay)
                .append("</td></tr>\n")
                .append("<tr><td>Максимальное количество VU</td><td>")
                .append(vuCountMax)
                .append("</td></tr>\n")
                .append("<tr><td>Прерывать тест при большом количестве ошибок</td><td>")
                .append(stopTestOnError ? "Да" : "Нет")
                .append("</td></tr>\n");
        if (stopTestOnError) {
            res.append("<tr><td>Количество ошибок для прерывания теста</td><td>")
                    .append(countErrorForStopTest)
                    .append("</td></tr>\n");
        }
        res.append("</tbody></table>\n");
        return res.toString();
    }

    /**
     * Фиксация активных пользователей
     */
    public void vuListAdd() {
        vuListAdd(System.currentTimeMillis(), getVuCount());
    }

    /**
     * Фиксация активных пользователей
     *
     * @param count
     */
    public void vuListAdd(int count) {
        vuListAdd(System.currentTimeMillis(), count);
    }

    /**
     * Фиксация активных пользователей
     *
     * @param time
     * @param count
     */
    public void vuListAdd(long time, int count) {
        vuList.add(new DateTimeValues(time, count));
    }


    /**
     * Сохранение метрики вызова
     *
     * @param start
     */
    public void callListAdd(long start, int thread) {
        Long stop = null;
        if (async) { // асинхронный вызов, не ждем завершения выполнения
            callList.add(new Call(start)); // фиксируем вызов
            try {
                baseScript.start(apiNum);
            } catch (Exception e) {
//                errorListAdd(name, e, thread);
                errorListAdd(name, start, e, thread);
            }
        } else { // синхронный вызов, ждем завершения выполнения
//            vuListActiveCall(start, thread);
            try {
                baseScript.start(apiNum);
                stop = System.currentTimeMillis();
                callList.add(new Call(start, stop)); // фиксируем вызов

            } catch (Exception e) {
                callList.add(new Call(start)); // фиксируем вызов
//                errorListAdd(name, e, thread);
                errorListAdd(name, start, e, thread);
            }
        }
        if (!warming.get()) { // не сохраняем мерики во время прогрева
            if (influxDB != null) {
                executorService.submit(new RunnableSaveToInfluxDbCall(
                        start,
                        stop,
                        this,
                        thread));
            }
        }
    }


    /**
     * Добавляем ошибку в список
     *
     * @param name
     * @param error
     * @param thread
     */
    public void errorListAdd(String name, Exception error, int thread) {
        errorListAdd(name, System.currentTimeMillis(), error, thread);
    }

    /**
     * Добавляем ошибку в список
     *
     * @param name
     * @param time
     * @param error
     * @param thread
     */
    public void errorListAdd(String name, long time, Exception error, int thread) {
        if (!warming.get()) { // при прогреве ошибки не фиксируем
            errorList.add(new ErrorRs(time, error.getMessage()));
            if (isStopTestOnError() && getErrorCount() > getCountErrorForStopTest()) { //ToDo
                stop("Большое количество ошибок: " + getErrorCount());
            }
            if (influxDB != null) {
                executorService.submit(new RunnableSaveToInfluxDbError(
                        time,
                        this,
                        thread));
            }
        }
        LOG.error("{}: {} Ошибка: {} из {} (Threads: {}) \n{}",
                name,
                sdf1.format(time),
                getErrorCount(),
                getCountErrorForStopTest(),
                getThreadCount(),
                error);
    }

    /**
     * Количество VU на момент времени
     *
     * @param time
     * @return
     */
    public int getVuCount(long time) {
        int res = 0;
        for (int i = 0; i < vuList.size(); i++) {
            if (vuList.get(i).getTime() > time) {
                break;
            } else {
                res = vuList.get(i).getValue();
            }
        }
        return res;
    }

    /**
     * Количество активных VU
     *
     * @return
     */
    public int getVuCount() {
        return vuCount.get();
    }

    /**
     * Старт нового VU
     */
    public int startVU() {
        if (vuCount.get() < vuCountMax) {
            return vuCount.incrementAndGet();
        }
        return -1;
    }

    /**
     * Остановка VU
     */
    public int stopVU() {
        return vuCount.decrementAndGet();
    }


    /**
     * Стартовали все VU ?
     *
     * @return
     */
    public boolean isStartedAllVU() {
        return vuCount.get() < vuCountMax ? false : true;
    }


    /**
     * Настало время добавления VU
     *
     * @return
     */
    public boolean isTimeAddVU() {
        boolean r = false;
        if (System.currentTimeMillis() > nextTimeAddVU) {
            nextTimeAddVU = System.currentTimeMillis() + vuStepTime * 1000L; // время следующего увеличения количества VU
            r = true;
        }
        return r;
    }

    /**
     * Старт группы VU
     */
    public void startGroupVU() {
        // настало время увеличения количества VU
        if (isRunning() && allowedAddVU.get() && System.currentTimeMillis() < testStopTime.get()) {
            if (isTimeAddVU() || getVuCount() < vuCountMin) { // первоначальная инициализация или настало время увеличения количества VU
                if (!isStartedAllVU()) { // не все VU стартовали
                    int vu = getVuCount();
                    int step = (vu == 0 ? vuCountMin : Math.min(vuStepCount, vuCountMax - vu));
                    for (int u = 0; u < step; u++) {
                        if ((vu = startVU()) > -1) {
                            executorService.submit(new RunnableVU(vu, this)); // запускаем поток
                            if (vuStepTimeDelay > 0) { // фиксируем каждого пользователя
                                vuListAdd(); // фиксация активных VU
                                try {
                                    TimeUnit.MILLISECONDS.sleep(vuStepTimeDelay); // задержка перед стартом очередного пользователя
                                } catch (InterruptedException e) {
                                    LOG.error("", e);
                                }
                            }
                        }
                    }
                    LOG.info("{}: текущее количество VU {} ({})",
                            name,
                            vu,
                            vuCountMax);
                    if (vuStepTimeDelay == 0) { // фиксируем всю группу
                        vuListAdd(); // фиксация активных VU
                    }
                }
            }
        }
    }

    /**
     * Прерывать тест при большом количестве ошибок
     *
     * @return
     */
    public boolean isStopTestOnError() {
        return stopTestOnError;
    }

    /**
     * Количество ошибок для прерывания теста
     *
     * @return
     */
    public int getCountErrorForStopTest() {
        return countErrorForStopTest;
    }

    /**
     * Количество ошибок
     *
     * @return
     */
    public int getErrorCount() {
        return errorList.size();
    }


    /**
     * Перестаем подавать нагрузку
     */
    public void stop() {
        stop("");
    }

    /**
     * Перестаем подавать нагрузку
     *
     * @param message
     */
    public void stop(String message) {
        if (!warming.get()) { // при прогреве тест не прерываем
            if (running.get()) {
                LOG.warn("{}: {} - прерываем подачу нагрузки...", name, message);
            }
            running.set(false);
        }
    }

    /**
     * Идет прогрев ?
     *
     * @return
     */
    public boolean isWarming() {
        return warming.get();
    }

    /**
     * Подача нагрузки разрешена ?
     *
     * @return
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Разрешен или нет старт норвых VU
     *
     * @param allowed
     */
    public void setAllowedAddVU(boolean allowed) {
        allowedAddVU.set(allowed);
    }

    /**
     * Проверка корректности параметров
     *
     * @return
     */
    private boolean checkParam() {
        boolean r = true;
        if (vuStepTime > 0 && vuStepCount > 0) {
            if (vuCountMin + vuStepCount * ((testDuration * 60L) / vuStepTime) < vuCountMax) {
                LOG.error("\n{}: Внимание!!! Заданные параметры не позволяют выйти на планируемую максимальную нагрузку за отведенное время ({} < {})",
                        name,
                        vuCountMin + vuStepCount * ((testDuration * 60L) / vuStepTime - 1),
                        vuCountMax);
                r = false;
            }
        }
        if (vuCountMin > vuCountMax) {
            LOG.error("\n{}: Внимание!!! Минимальное значение VU ({}) больше максимального значения VU ({})",
                    name,
                    vuCountMin,
                    vuCountMax);
            r = false;

        }
        if ((vuStepTime == 0 || vuStepCount == 0) && vuCountMin < vuCountMax) {
            LOG.error("\n{}: Внимание!!! Минимальное значение VU ({}) меньше максимального значения VU ({}), при этом параметры шага - время или количество равны 0 ({} / {})",
                    name,
                    vuCountMin,
                    vuCountMax,
                    vuStepTime,
                    vuStepCount);
            r = false;

        }
        if (vuStepTime * 1000 < vuStepTimeDelay * vuCountMin) {
            LOG.error("\n{}: Внимание!!! {} начальных пользователей с задержкой {} мс не успеют стартовать за время между шагами {} мс",
                    name,
                    vuCountMin,
                    vuStepTimeDelay,
                    vuStepTime * 1000);
            r = false;
        }
        if (vuStepTime * 1000 < vuStepTimeDelay * vuStepCount) {
            LOG.error("\n{}: Внимание!!! {} пользователей с задержкой {} мс не успеют стартовать за время между шагами {} мс",
                    name,
                    vuStepCount,
                    vuStepTimeDelay,
                    vuStepTime * 1000);
            r = false;
        }
        if (pacingType == 0 && pacing == 0) {
            LOG.error("\n{}: Внимание!!! не допустимо сочетание значений параметров pacingType: {} pacing: {}",
                    name,
                    pacingType,
                    pacing);
            r = false;
        }
        return r;
    }


    /**
     * Нагрузка
     *
     * @param baseScript
     */
    public void start(ScriptRun baseScript) {
        this.baseScript = baseScript;
        start(true);  // прогрев
        start(false); // нагрузка
    }

    /**
     * Нагрузка
     *
     * @param warming
     */
    public void start(boolean warming) {
        this.running.set(true);
        this.warming.set(warming);
        vuList.clear();
        errorList.clear();
        bpmsJobEntityImplCountList.clear();
        retryPolicyJobEntityImplCountList.clear();
        threadCount = new AtomicInteger(0);
        vuCount = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(1);
//        ExecutorService executorService = Executors.newFixedThreadPool(maxCountVU + 1); // пул VU
        executorService = Executors.newCachedThreadPool(); // пул VU (расширяемый)
        ExecutorService executorServiceAwaitAndAddVU = Executors.newFixedThreadPool(1); // пул для задачи контроля выполнения
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();

        if (!warming) { // После прогрева нагрузка сервисов должна начаться одновременно
            if (!multiRun.isWarmingCompleted()) {
                LOG.info("{}: Ожидание завершения прогрева всех сервисов...", name);
                while (!multiRun.isWarmingCompleted()) { // ждем завершения прогрева всех сервисов
                }
            }
            LOG.info("{}: Прогрев всех сервисов завершен, подача нагрузки...", name);
        }

        int vuCountMinMem = vuCountMin;
        int vuCountMaxMem = vuCountMax;
        testStartTime = System.currentTimeMillis(); // время старта теста

        if (warming) {
            vuCountMin = 5;
            vuCountMax = 5;
            testStopTime.set(testStartTime + warmDuration * 1000L); // время завершения прогрева (параметр в сек)
        } else {
            testStopTime.set(testStartTime + testDuration * 60000L); // время завершения теста (параметр в мин)
        }
        nextTimeAddVU = testStartTime + vuStepTime * 1000L; // время следующего увеличения количества VU (при запуске необходимо инициировать стартовое количество)

        LOG.info("##### {}{}" +
                        "\ntestStartTime: {}" +
                        "\ntestStopTime: {}" +
                        "\nnextTimeAddVU: {}",
                name,
                (isWarming() ? " (Прогрев)" : ""),
                sdf1.format(testStartTime),
                sdf1.format(testStopTime.get()),
                sdf1.format(nextTimeAddVU));

        vuListAdd(testStartTime, 0); // игнорируем нулевой элемент при формировании графиков
        vuListAdd(testStartTime, 0); // стартовое количество VU
        executorServiceAwaitAndAddVU.submit(new RunnableAwaitAndAddVU(
                name + " RunnableAwaitAndAddVU",
                countDownLatch,
                this));

        if (!warming && dbService != null) {

            // опрашиваем размерность таблицы BpmsJobEntityImpl (тротлинг)
            executorService.submit(new RunnableDbSelectCount(
                    name,
                    "BpmsJobEntityImpl",
                    sqlSelectBuilder.getBpmsJobEntityImpl(processDefinitionKey),
                    5000,
                    this,
                    bpmsJobEntityImplCountList,
                    1000,
                    influxDB));

            // опрашиваем размерность таблицы RetryPolicyJobEntityImpl (ретраи)
            executorService.submit(new RunnableDbSelectCount(
                    name,
                    "RetryPolicyJobEntityImpl",
                    sqlSelectBuilder.getRetryPolicyJobEntityImpl(processDefinitionKey),
                    5000,
                    this,
                    retryPolicyJobEntityImplCountList,
                    100000,
                    influxDB));
        }

        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            LOG.error("{}\n", name, e);
        }
        testStopTime.set(System.currentTimeMillis());
        vuList.add(new DateTimeValues(testStopTime.get(), getVuCount())); // сбросим VU на конец теста
        LOG.info("{}: testStopTime: {}", name, sdf1.format(testStopTime.get()));

        executorServiceAwaitAndAddVU.shutdown();
        executorService.shutdown();

        testStartTimeReal = testStartTime;
        testStopTimeReal = testStopTime.get();
        // округлим до секунд период теста
        try {
            testStartTime = sdf2.parse(sdf2.format(testStartTime)).getTime(); // в меньшую сторону
        } catch (ParseException e) {
            LOG.error("Ошибка в формате даты", e);
        }
        testStopTime.set((long) (Math.ceil(testStopTime.get()) / 1000.00) * 1000); // в большую сторну

        if (warming) {
            vuCountMin = vuCountMinMem;
            vuCountMax = vuCountMaxMem;
            LOG.info("{}: Прогрев завершен...", name);
            this.warming.set(false);
        } else {
            // даем время завершиться начатым заданиям (кто не успел я не виноват)
            dataFromDB.waitCompleteProcess(
                    processDefinitionKey,
                    testStartTime,
                    testStopTime.get(),
                    bpmsJobEntityImplCountList);

            LOG.info("{}: Сбор статистики...", name);

            long startTime = testStartTime;
            long stopTime = testStopTime.get();

            // данные из БД БПМ за период
            getDataFromDB().getDataFromDbSelect(processDefinitionKey, startTime, stopTime);
            // статистику за весь период сохраним нулевым элементом
            getStatistics(startTime, stopTime);

            // сбор статистики после снятия нагрузки
            prevStartTimeStatistic = testStartTime;
            long statisticsStepTime = (long) Math.max((stopTime - startTime) / 600.00, 1000); // шаг вывода метрик
            statisticsStepTime = (long) Math.max(statisticsStepTime, pacing); // ToDo:
            statisticsStepTime = (long) (Math.ceil(statisticsStepTime / 1000.00) * 1000); // шаг кратен 1 сек (в большую сторону)
            while (startTime < stopTime) {
                startTime = startTime + statisticsStepTime;
                if (startTime < stopTime) {
                    getStatistics(startTime);
                }
            }
            LOG.info("{}: Завершен сбор статистики", name);

            // сохраняем результаты в HTML - файл
            report.saveReportHtml(this, pathReport);
        }
    }

    /**
     * Снятие метрик - сам процесс
     */
    private void getStatistics(long stopTime) {
        getStatistics(prevStartTimeStatistic, stopTime - 1);
    }

    public void getStatistics(long startTime, long stopTime) {
        LOG.debug("{}: Статистика {} - {}",
                name,
                sdf1.format(startTime),
                sdf1.format(stopTime));

        CallMetrics callMetrics = getMetricsForPeriod(startTime, stopTime);

        // статистика выполнения процессов в БПМ
        DBResponse dbResponse = dataFromDB.getStatisticsFromDb(
                processDefinitionKey,
                startTime,
                stopTime);

        if (sqlSelect == null) {
            sqlSelect = dbResponse.getSqlSelect();
        }

        // ошибки (при сборе статистики за весь период не фиксируем )
        LOG.debug("{}: группировка ошибок {} - {}", name, sdf1.format(startTime), sdf1.format(stopTime));
        int countError = 0;
        if (startTime != testStartTime || stopTime != testStopTime.get()) {
            for (int i = 0; i < errorList.size(); i++) {
                if (errorList.get(i).getTime() >= startTime && errorList.get(i).getTime() <= stopTime) {
                    countError++;
                }
            }
        }

        // добавляем полученные метрики в список
        Map<Metric, Number> map = new LinkedHashMap<>();
        map.put(Metric.DUR_MIN, callMetrics.getDurMin());
        map.put(Metric.DUR_AVG, callMetrics.getDurAvg());
        map.put(Metric.DUR_90, callMetrics.getDur90());
        map.put(Metric.DUR_MAX, callMetrics.getDurMax());
        map.put(Metric.TPS, callMetrics.getTps());
        map.put(Metric.TPS_RS, callMetrics.getTpsRs());
        map.put(Metric.COUNT_CALL, callMetrics.getCountCall());
        map.put(Metric.COUNT_CALL_RS, callMetrics.getCountCallRs());
        map.put(Metric.DB_COMPLETED, dbResponse.getIntValue(Metric.DB_COMPLETED));
        map.put(Metric.DB_RUNNING, dbResponse.getIntValue(Metric.DB_RUNNING));
        map.put(Metric.DB_FAILED, dbResponse.getIntValue(Metric.DB_FAILED));
        map.put(Metric.DB_LOST, callMetrics.getCountCall() - (dbResponse.getIntValue(new Metric[]{
                Metric.DB_COMPLETED,
                Metric.DB_RUNNING,
                Metric.DB_FAILED})));
        map.put(Metric.DB_DUR_MIN, dbResponse.getDoubleValue(Metric.DB_DUR_MIN));
        map.put(Metric.DB_DUR_AVG, dbResponse.getDoubleValue(Metric.DB_DUR_AVG));
        map.put(Metric.DB_DUR_90, dbResponse.getDoubleValue(Metric.DB_DUR_90));
        map.put(Metric.DB_DUR_MAX, dbResponse.getDoubleValue(Metric.DB_DUR_MAX));
        map.put(Metric.ERRORS, countError);

        metricsList.add(new DateTimeValues(stopTime, map));
        prevStartTimeStatistic = stopTime + 1;
    }

    /**
     * Метрики за период
     *
     * @param startTime
     * @param stopTime
     */
    public CallMetrics getMetricsForPeriod(long startTime, long stopTime) {
        LOG.debug("{}: метрики {} - {}", name, sdf1.format(startTime), sdf1.format(stopTime));
        int[] countCall = {0, 0};                       // 0-all, 1-Rs
        double[] dur = {999999999999999999L, 0L, 0L, 0L}; // 0-min, 1-avg, 2-90%, 3-max
        double[] tps = {0.00, 0.00};                    // 0-tps, 1-tpsRs
        callList.stream()
                .filter(e -> (e.getStartTime() >= startTime && e.getStartTime() <= stopTime))
                .sorted()
                .forEach(x -> {
                    countCall[0]++;
                    if (x.getDuration() != null) {
                        countCall[1]++;
                        dur[0] = Math.min(dur[0], x.getDuration()); // min
                        dur[1] = dur[1] + x.getDuration();          // avg
                        dur[3] = Math.max(dur[3], x.getDuration()); // max
                    }
                });

        if (dur[0] == 999999999999999999L) {
            dur[0] = 0L;
        }

        if (countCall[1] > 0) {
            dur[1] = dur[1] / countCall[1] * 1.00; // avg
            Percentile percentile90 = new Percentile();
            dur[2] = percentile90.evaluate(
                    callList
                            .stream()
                            .filter(x -> (x.getDuration() != null & x.getStartTime() >= startTime && x.getStartTime() <= stopTime))
                            .mapToDouble(Call::getDuration)
                            .toArray(), 90);
        } else {
            dur[1] = 0; // avg
        }

        tps[0] = countCall[0] / ((stopTime - (startTime)) / 1000.00);
        tps[1] = countCall[1] / ((stopTime - (startTime)) / 1000.00);
/*
        LOG.info("getDataForPeriod (stream): count: {}, countRs: {}, min: {}, avg: {}, 90%: {}, max: {}, tps: {}, tpsRs: {}",
                countCall[0],
                countCall[1],
                dur[0],
                dur[1],
                dur[2],
                dur[3],
                tps[0],
                tps[1]);
*/

    /*
        0  - durMin
        1  - durAvg
        2  - dur90
        3  - durMax
        4  - tps
        5  - tpsRs
        6  - countCall
        7  - countCallRs
     */
        return new CallMetrics(
                dur[0],
                dur[1],
                dur[2],
                dur[3],
                tps[0],
                tps[1],
                countCall[0],
                countCall[1]);
    }

    /**
     * Добавляем поток
     *
     * @param thread
     */
    public void vuListActiveAdd(int thread) {
//        int index = getVuListActiveIndex(thread);
//        if (index > -1){
//            vuListActive.get(index).setActive(true);
//        } else {
            vuListActive.add(new VU(thread));
//        }
    }


    /**
     * Удаляем поток
     *
     * @param thread
     */
    public void vuListActiveDel(int thread) {
        int index = getVuListActiveIndex(thread);
        if (index > -1){
            vuListActive.remove(index);
        }
    }

    /**
     * Очередной вызов из потока
     *
     * @param thread
     */
    public void vuListActiveCall(long start, int thread) {
        int index = getVuListActiveIndex(thread);
        if (index > -1) {
            vuListActive.get(index).setLastCallTime(start);
        }
    }

    /**
     * Разрешена активность данного VU
     *
     * @param thread
     * @return
     */
    public boolean isActiveVU(int thread) {
        int index = getVuListActiveIndex(thread);
        if (index > -1){
            return vuListActive.get(index).isActive();
        }
        return true; // если не найден, пусть работает
    }

    /**
     * Поиск потока в списке
     *
     * @param thread
     * @return
     */
    public int getVuListActiveIndex(int thread){
        for (int i = 0; i < vuListActive.size() ; i++) {
            if (vuListActive.get(i).getNum() == thread){
                return i;
            }
        }
        return -1;
    }

    /**
     * Количество активных потоков
     * @return
     */
    public int getVUActiveCount(){
        int count = 0;
        for (int i = 0; i < vuListActive.size(); i++) {
            if (vuListActive.get(i).isActive()){
                count++;
            }
        }
        return count;
    }

    /**
     * Уменьшение количества VU (-1)
     * не уменьшаем до 0
     */
    public void vuDec(){
        if (!isWarming()) {
            allowedAddVU.set(false);
            if (getVuCount() > 1 && getVUActiveCount() > 1 && isRunning() && System.currentTimeMillis() < testStopTime.get()) {
                for (int t = 0; t < vuListActive.size(); t++) {
                    if (vuListActive.get(t).isActive()) {
                        vuListActive.get(t).setActive(false);
                        break;
                    }
                }
            }
            String[] s = {""};
            vuListActive.forEach(x -> {
                s[0] = s[0] + x.getNum() + ": " + x.isActive() + "; ";
            });
            LOG.info("{}: {}", name, s[0]);
        }
    }

    /**
     * Увеличение количества VU (+1)
     */
    public void vuInc() {
        if (!isWarming()) {
            allowedAddVU.set(false);
            if (isRunning() && System.currentTimeMillis() < testStopTime.get()) {
                int vu = vuCount.incrementAndGet();
                executorService.submit(new RunnableVU(vu, this)); // запускаем поток
                vuListAdd(); // фиксация активных VU
                String[] s = {""};
                vuListActive.forEach(x -> {
                    s[0] = s[0] + x.getNum() + ": " + x.isActive() + "; ";
                });
                LOG.info("{}: {}", name, s[0]);
            }
        }
    }

    /**
     * Уменьшение длительности теста на step ms
     * @param step
     */
    public void durationDec(int step) {
        if (System.currentTimeMillis() < testStopTime.get() && !isWarming() && isRunning() && (testStopTime.get() - step) > System.currentTimeMillis()) {
            testStopTime.set(testStopTime.get() - step);
            LOG.info("\n### {}: длительность теста уменьшена до {}", name, sdf1.format(testStopTime.get()));
        }
    }

    /**
     * Увеличение длительности теста на step ms
     * @param step
     */
    public void durationInc(int step) {
        if (System.currentTimeMillis() < testStopTime.get() && !isWarming() && isRunning() ) {
            testStopTime.set(testStopTime.get() + step);
            LOG.info("\n### {}: длительность теста увеличена до {}", name, sdf1.format(testStopTime.get()));
        }
    }

}