package ru.utils.load.utils;

import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.data.errors.ErrorRsGroup;
import ru.utils.load.data.errors.ErrorRs;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.graph.VarInList;
import ru.utils.load.data.metrics.CallMetrics;
import ru.utils.load.data.sql.DBResponse;
import ru.utils.load.runnable.RunnableAwaitAndAddVU;
import ru.utils.load.runnable.RunnableVU;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiRunService {
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private List<DateTimeValue> vuList = new CopyOnWriteArrayList<>(); // количество виртуальных пользователей на момент времени
    private List<Call> callList = new CopyOnWriteArrayList<>(); // список вызовов сервиса

    /*  список метрик:
        0  - durMin
        1  - durAvg
        2  - dur90
        3  - durMax
        4  - tps
        5  - tpsRs
        6  - countCall
        7  - countCallRs
        8  - dbCompleted
        9  - dbRunning
        10 - dbLost
        11 - dbDurMin
        12 - dbDurAvg
        13 - dbDur90
        14 - dbDurMax
        15 - errors
    */
    private List<DateTimeValue> metricsList = new ArrayList<>();

    private List<ErrorRs> errorList = new CopyOnWriteArrayList<>(); // ошибки при выполнении API
    private List<ErrorRsGroup> errorRsGroupList = new ArrayList<>(); // количество ошибок по типам

    private ScriptRun baseScript;
    private ExecutorService executorService;

    private AtomicInteger threadCount; // // счетчик потоков
    private AtomicInteger vuCount; // текущее количество VU

    private AtomicBoolean running = new AtomicBoolean(true); // тест продолжается
    private AtomicBoolean warming = new AtomicBoolean(true); // прогрев

    private MultiRun multiRun;
    private int apiNum;
    private String name;
    private long testStartTime;
    private long testStopTime;
    private long nextTimeAddVU;
    private Long prevStartTimeStatistic;

    // параметры теста
    private boolean async; // асинхронный вызов сервиса

    private int testDuration = 1; // длительность теста в минутах

    private int vuCountMin = 10;     // начальное количество виртуальных пользователей (VU)
    private int vuCountMax = 100;    // максимальное количество VU
    private int vuStepTime = 5;      // через какое время в секундах увеличиваем количество VU
    private long vuStepTimeDelay = 1;// задержка между стартами пользователей в группе (сек)
    private int vuStepCount = 5;     // на сколько увеличиваем количество VU

    private long pacing = 1000;    // задержка перед выполнением следующей итерации (ms)
    private int pacingType = 1;    // 0 - задержка от момента старта операции (без ожидания выполнения); 1 - задержка от момента старта операции (с учетом ожидания выполения); 2 - задержка от момента завершения выполнения операции;

    private int statisticsStepTime = 5;    // частота снятия метрик - через данное количество секунд

    private boolean stopTestOnError = false; // прерывать тест при большом количестве ошибок
    private int countErrorForStopTest = 100; // количество ошибок для прерывания теста

    private String grafanaHostsDetailUrl; // Графана - Хосты детализованно (URL)
    private String grafanaHostsDetailCpuUrl; // Графана - Хосты детализованно CPU (URL)
    private String grafanaTransportThreadPoolsUrl; //Графана - TransportThreadPools (URL)
    private String splunkUrl; // Спланк (URL)
    private String csmUrl; // CSM (URL)

    private String keyBpm;

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
            int statisticsStepTime,
            boolean stopTestOnError,
            int countErrorForStopTest,
            String grafanaHostsDetailUrl,
            String grafanaHostsDetailCpuUrl,
            String grafanaTransportThreadPoolsUrl,
            String splunkUrl,
            String csmUrl,
            String dbUrl,
            String dbUserName,
            String dbPassword,
            String keyBpm,
            String pathReport
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
        this.statisticsStepTime = statisticsStepTime;
        this.stopTestOnError = stopTestOnError;
        this.countErrorForStopTest = countErrorForStopTest;
        this.grafanaHostsDetailUrl = grafanaHostsDetailUrl;
        this.grafanaHostsDetailCpuUrl = grafanaHostsDetailCpuUrl;
        this.grafanaTransportThreadPoolsUrl = grafanaTransportThreadPoolsUrl;
        this.splunkUrl = splunkUrl;
        this.csmUrl = csmUrl;
        this.keyBpm = keyBpm;
        this.pathReport = pathReport;

        if (!checkParam()) { // ошибка в параметрах
            System.exit(1);
        }

        if (!dbUrl.isEmpty()) {
            dataFromDB.init( // подключаемся к БД
                    dbUrl,
                    dbUserName,
                    dbPassword);
        }

    }

    public void end() { dataFromDB.end(); }

    public MultiRunService() {
    }

    public int getApiNum() {
        return apiNum;
    }

    public String getName() {
        return name;
    }

    public boolean isAsync() { return async; }

    public DataFromDB getDataFromDB() {
        return dataFromDB;
    }

    public MultiRun getMultiRun() {
        return multiRun;
    }

    public List<DateTimeValue> getVuList() {
        return vuList;
    }

    public List<DateTimeValue> getMetricsList() {
        return metricsList;
    }

    public List<Call> getCallList() {
        return callList;
    }

    public String getKeyBpm() {
        return keyBpm;
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
        return testStopTime;
    }

    public long getPacing() {
        return pacing;
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

    public String getSplunkUrl() {
        return splunkUrl;
    }

    public String getCsmUrl() {
        return csmUrl;
    }

    public String getSqlSelect(int num) { return sqlSelect[num]; }

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
                .append(async ? "Нет" : "Да") // ToDo
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
                .append("<tr><td>Периодичность снятия метрик (сек)</td><td>")
                .append(statisticsStepTime)
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
        vuList.add(new DateTimeValue(time, count));
    }


    /**
     * Добавляем ошибку в список
     * @param name
     * @param error
     */
    public void errorListAdd(String name, Exception error){
        if (!warming.get()) { // при прогреве ошибки не фиксируем
            long time = System.currentTimeMillis();
            LOG.error("{}\n", name, error);
            errorList.add(new ErrorRs(time, error.getMessage()));
            if (isStopTestOnError() && getErrorCount() > getCountErrorForStopTest()) { //ToDo
                stop();
            }
        }
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
                res = vuList.get(i).getIntValue();
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
    public boolean startVU() {
        boolean r = false;
        if (vuCount.get() < vuCountMax) {
            vuCount.incrementAndGet();
            r = true;
        }
        return r;
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
        if (isRunning() && System.currentTimeMillis() < testStopTime) {
            if (isTimeAddVU() || getVuCount() < vuCountMin) { // первоначальная инициализация или настало время увеличения количества VU
                if (!isStartedAllVU()) { // не все VU стартовали
                    int step = (getVuCount() == 0 ? vuCountMin : vuStepCount);
                    for (int u = 0; u < step; u++) {
                        if (startVU()) {
                            executorService.submit(new RunnableVU(
                                    name + " RunnableVU" + getVuCount(),
                                    baseScript,
                                    callList,
                                    this,
                                    executorService));

                            if (vuStepTimeDelay > 0) { // фиксируем каждого пользователя
                                vuListAdd(); // фиксация активных VU

                                try {
                                    Thread.sleep(vuStepTimeDelay); // задержка перед стартом очередного пользователя
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    LOG.info("{}: текущее количество VU {} из {}",
                            name,
                            getVuCount(),
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
     * Перестаем подавать новую нагрузку
     */
    public void stop() {
        if (!warming.get()) { // при прогреве тест не прерываем
            if (running.get()) {
                LOG.warn("{}: прерываем подачу нагрузки...", name);
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
        if (vuStepTime < statisticsStepTime) {
            LOG.error("\n{}: Внимание!!! Пользователи добавляются чаще чем снимается статистика ({} / {})",
                    name,
                    vuStepTime,
                    statisticsStepTime);
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
        threadCount = new AtomicInteger(0);
        vuCount = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(1);
//        ExecutorService executorService = Executors.newFixedThreadPool(maxCountVU + 1); // пул VU
        executorService = Executors.newCachedThreadPool(); // пул VU (расширяемый)
        ExecutorService executorServiceAwaitAndAddVU = Executors.newFixedThreadPool(1); // пул для задачи контроля выполнения

        if (!warming) { // После прогрева нагрузка сервисов должна начаться одновременно
            if (!multiRun.isWarmingCompleted()) {
                LOG.info("{}: Ожидание завершения прогрева всех сервисов...", name);
                while (!multiRun.isWarmingCompleted()) { // ждем завершения прогрева всех сервисов
                }
            }
        }

        vuListAdd(testStartTime, 0); // игнорируем нулевой элемент при формировании графиков
        executorServiceAwaitAndAddVU.submit(new RunnableAwaitAndAddVU(
                name + " RunnableAwaitAndAddVU",
                countDownLatch,
                this));

        int vuCountMinMem = vuCountMin;
        int vuCountMaxMem = vuCountMax;
        testStartTime = System.currentTimeMillis(); // время старта теста
        if (warming) {
            LOG.info("{}: Прогрев...", name);
            vuCountMin = 5;
            vuCountMax = 5;
            testStopTime = testStartTime + statisticsStepTime * 3000L; // время завершения теста (прогрев)
        } else {
            testStopTime = testStartTime + testDuration * 60000L; // время завершения теста
        }
        nextTimeAddVU = testStartTime + vuStepTime * 1000L; // время следующего увеличения количества VU (при запуске необходимо инициировать стартовое количество)

        LOG.info("##### {}{}" +
                        "\ntestStartTime: {}" +
                        "\ntestStopTime: {}" +
                        "\nnextTimeAddVU: {}",
                name,
                (isWarming() ? " (Прогрев)" : ""),
                sdf1.format(testStartTime),
                sdf1.format(testStopTime),
                sdf1.format(nextTimeAddVU));

        vuListAdd(testStartTime, 0); // стартовое количество VU

        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            LOG.error("{}\n", name, e);
        }
//        while (getThreadCount() > 0){}

        executorServiceAwaitAndAddVU.shutdown();
        executorService.shutdown();

        if (warming) {
            vuCountMin = vuCountMinMem;
            vuCountMax = vuCountMaxMem;
            LOG.info("{}: Прогрев завершен...", name);
            this.warming.set(false);
        } else {
            LOG.info("{}: Пауза...", name);
            try { // даем время завершиться начатым заданиям
                Thread.sleep(statisticsStepTime * 1000);
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
            testStopTime = System.currentTimeMillis();
            vuList.add(new DateTimeValue(testStopTime, getVuCount())); // сбросим VU на конец теста
            try { // даем время завершиться начатым заданиям (кто не успел я не виноват)
//                Thread.sleep(Math.max(pacing * 10, 20000));
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
            LOG.info("{}: Сбор статистики...", name);

            long startTime = testStartTime;
            long stopTime = testStopTime; // + statisticsStepTime * 1000L;

            // данные из БД БПМ за период
            getDataFromDB().getDataFromDbSelect(keyBpm, startTime, stopTime);
            // статистику за весь период сохраним нулевым элементом
            getStatistics(startTime, stopTime);

            // сбор статистики после снятия нагрузки
            prevStartTimeStatistic = testStartTime;
            while (startTime <= stopTime) {
                startTime = startTime + statisticsStepTime * 1000L;
                getStatistics(startTime);
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
                keyBpm,
                startTime,
                stopTime);

        if (sqlSelect == null) {
            sqlSelect = dbResponse.getSqlSelect();
        }

        // ошибки (при сборе статистики за весь период не фиксируем )
        LOG.debug("{}: группировка ошибок {} - {}", name, sdf1.format(startTime), sdf1.format(stopTime) );
        int countError = 0;
        if (startTime != testStartTime || stopTime != testStopTime) {
            for (int i = 0; i < errorList.size(); i++) {
                if (errorList.get(i).getTime() >= startTime && errorList.get(i).getTime() <= stopTime) {
                    countError++;
                }
            }
        }

        /*  добавляем полученные метрики в список
            список метрик:
            0  - durMin
            1  - durAvg
            2  - dur90
            3  - durMax
            4  - tps
            5  - tpsRs
            6  - countCall
            7  - countCallRs
            8  - dbCompleted
            9  - dbRunning
            10 - dbLost
            11 - dbDurMin
            12 - dbDurAvg
            13 - dbDur90
            14 - dbDurMax
            15 - errors
        */
        metricsList.add(new DateTimeValue(
                stopTime,
                Arrays.asList(
                        (int) callMetrics.getDurMin(),
                        (int) callMetrics.getDurAvg(),
                        (int) callMetrics.getDur90(),
                        (int) callMetrics.getDurMax(),

                        callMetrics.getTps(),
                        callMetrics.getTpsRs(),

                        callMetrics.getCountCall(),
                        callMetrics.getCountCallRs(),

                        dbResponse.getIntValue(VarInList.DbCompleted),
                        dbResponse.getIntValue(VarInList.DbRunning),
                        callMetrics.getCountCall() - (dbResponse.getIntValue(VarInList.DbCompleted) + dbResponse.getIntValue(VarInList.DbRunning)),

                        dbResponse.getIntValue(VarInList.DbDurMin),
                        dbResponse.getIntValue(VarInList.DurAvg),
                        dbResponse.getIntValue(VarInList.Dur90),
                        dbResponse.getIntValue(VarInList.DurMax),

                        countError)));

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
        long[] dur = {999999999999999999L, 0L, 0L, 0L}; // 0-min, 1-avg, 2-90%, 3-max
        double[] tps = {0.00, 0.00};                    // 0-tps, 1-tpsRs
        callList.stream()
                .filter(e -> (e.getStartTime() >= startTime && e.getStartTime() <= stopTime))
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
            dur[1] = dur[1] / countCall[1]; // avg
            Percentile percentile90 = new Percentile();
            dur[2] = (long) percentile90.evaluate(
                    callList
                            .stream()
                            .filter(x -> (x.getDuration() != null & x.getStartTime() >= startTime && x.getStartTime() <= stopTime))
                            .mapToDouble(Call::getDuration)
                            .toArray(), 90);
        } else {
            dur[1] = 0; // avg
        }

        tps[0] = countCall[0] / ((stopTime - (startTime)) / 1000.00); // + ToDo
        tps[1] = countCall[1] / ((stopTime - (startTime)) / 1000.00); // + ToDo
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

}