package ru.utils.load.utils;

import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.data.errors.ErrorRsGroup;
import ru.utils.load.data.errors.ErrorRs;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.sql.DBResponse;
import ru.utils.load.runnable.CallableVU;
import ru.utils.load.runnable.RunnableAwaitAndAddVU;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiRunService {
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private List<DateTimeValue> vuList = new CopyOnWriteArrayList<>(); // количество виртуальных пользователей на момент времени
    private List<Call> callList = new ArrayList<>(); // список вызовов сервиса

    /*  список метрик:
        0  - durMin
        1  - durAvg
        2  - dur90
        3  - durMax
        4  - tpc
        5  - tpcRs
        6  - countCall
        7  - countCallComplete
        8  - dbComplete
        9  - dbRunning
        10 - errors
    */
    private List<DateTimeValue> metricsList = new ArrayList<>();

    private List<ErrorRs> errorList = new CopyOnWriteArrayList<>(); // ошибки при выполнении API
    private List<ErrorRsGroup> errorRsGroupList = new ArrayList<>(); // количество ошибок по типам

    private ScriptRun baseScript;
    private List<Future<List<Call>>> futureList = new ArrayList<>();
    private ExecutorService executorService;

    private AtomicInteger threadCount; // // счетчик потоков
    private AtomicInteger vuCount; // текущее количество VU

    private MultiRun multiRun;
    private int apiNum;
    private String name;
    private long testStartTime;
    private long testStopTime;
    private long nextTimeAddVU;
    private Long prevStartTimeStatistic;

    // параметры теста
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

    Boolean running = true; // тест продолжается
    boolean warming = true; // прогрев

    private String sqlSelect = null;
    private DataFromSQL dataFromSQL = new DataFromSQL(); // получение данных из БД БПМ
    private Report report = new Report();

    /**
     * инициализация параметров
     */
    public void init(
            MultiRun multiRun,
            int apiNum,
            String name,
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
            DataFromSQL dataFromSQL,
            String keyBpm,
            String pathReport
    ) {
        this.multiRun = multiRun;
        this.apiNum = apiNum;
        this.name = name;
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
        this.dataFromSQL = dataFromSQL;
        this.keyBpm = keyBpm;
        this.pathReport = pathReport;

        if (!checkParam()) { // ошибка в параметрах
            System.exit(1);
        }
    }

    public void end() {
//        dataFromSQL.end();
    }

    public MultiRunService() {
    }

    public int getApiNum() {
        return apiNum;
    }

    public String getName() {
        return name;
    }

    public DataFromSQL getDataFromSQL() {
        return dataFromSQL;
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

    public String getSqlSelect() {
        return sqlSelect;
    }

    /**
     * Количество активных потоков
     */
    public int getThreadCount() {
        synchronized (threadCount) {
            return threadCount.get();
        }
    }

    /**
     * Новый поток
     */
    public void threadInc() {
        synchronized (threadCount) {
            threadCount.incrementAndGet();
        }
    }

    /**
     * Завершен поток
     */
    public void threadDec() {
        synchronized (threadCount) {
            threadCount.decrementAndGet();
        }
    }

    /**
     * Параметры теста
     *
     * @return
     */
    public String getParams() {
        StringBuilder res = new StringBuilder("\n<h3>Параметры<h3>\n" +
                "<table border=\"1\"><tbody>\n");
        res.append("<tr><td>Длительность теста (мин)</td><td>")
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
                .append("<tr><td>Периодичность снятия метрик (сек):</td><td>")
                .append(statisticsStepTime)
                .append("</td></tr>\n")
                .append("<tr><td>Прерывать тест при большом количестве ошибок:</td><td>")
                .append(stopTestOnError ? "Да" : "Нет")
                .append("</td></tr>\n");
        if (stopTestOnError) {
            res.append("<tr><td>Количество ошибок для прерывания теста:</td><td>")
                    .append(countErrorForStopTest)
                    .append("</td></tr>\n");
        }
        res.append("</tbody></table>\n");
        return res.toString();
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
        synchronized (vuList) {
            vuList.add(new DateTimeValue(time, count));
        }
    }


    /**
     * Добавляем метрику ошибки
     *
     * @param time
     * @param text
     */
    public void errorListAdd(long time, String text) {
        if (!warming) { // при прогреве ошибки не фиксируем
            synchronized (errorList) {
                errorList.add(new ErrorRs(time, text));
            }
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
        synchronized (vuCount) {
            return vuCount.get();
        }
    }

    /**
     * Старт нового VU
     */
    public boolean startVU() {
        boolean r = false;
        if (vuCount.get() < vuCountMax) {
            synchronized (vuCount) {
                vuCount.incrementAndGet();
            }
            r = true;
        }
        return r;
    }

    /**
     * Остановка VU
     */
    public void stopVU() {
        synchronized (vuCount) {
            vuCount.decrementAndGet();
        }
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
                            futureList.add(executorService.submit(new CallableVU(
                                    name + " CallableVU" + getVuCount(),
                                    baseScript,
                                    executorService,
                                    this)));
                            if (vuStepTimeDelay > 0) { // фиксируем каждого пользователя
                                vuListAdd(getVuCount()); // фиксация активных VU
                            }
                            try {
                                Thread.sleep(vuStepTimeDelay); // задержка перед стартом очередного пользователя
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LOG.info("{}: текущее количество VU {} из {}",
                            name,
                            getVuCount(),
                            vuCountMax);
                    if (vuStepTimeDelay == 0) { // фиксируем всю группу
                        vuListAdd(getVuCount()); // фиксация активных VU
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
        if (!warming) { // при прогреве тест не прерываем
            if (running) {
                LOG.warn("{}: из-за большого количества ошибок прерываем подачу нагрузки...", name);
            }
            synchronized (running) {
                running = false;
            }
        }
    }

    /**
     * Идет прогрев ?
     *
     * @return
     */
    public boolean isWarming() {
        return warming;
    }

    /**
     * Подача нагрузки разрешена ?
     *
     * @return
     */
    public boolean isRunning() {
        return running;
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
     * Снятие метрик - сам процесс
     */
    private void getStatistics(long stopTime) {
        getStatistics(prevStartTimeStatistic + 1, stopTime);
    }

    public void getStatistics(long startTime, long stopTime) {
        LOG.debug("{}: Статистика {} - {}", name, sdf1.format(startTime), sdf1.format(stopTime));

        long durMin = 999999999999999999L;
        long durMax = 0L;
        long durAvg = 0L;
        int countCall = 0;
        int countCallComplete = 0;

        for (int i = 0; i < callList.size(); i++) {
            if (callList.get(i).getTimeBegin() >= startTime && callList.get(i).getTimeBegin() <= stopTime) {
                if (callList.get(i).getDuration() > 0) {
                    durMin = Math.min(durMin, callList.get(i).getDuration());
                    durMax = Math.max(durMax, callList.get(i).getDuration());
                    durAvg = durAvg + callList.get(i).getDuration();
                    countCallComplete++;
                }
                countCall++;
            }
        }

        double tpc = countCall / (statisticsStepTime * 1.00);
        double tpcRs = countCallComplete / (statisticsStepTime * 1.00);

        long dur90 = 0L;
        if (countCallComplete > 0) {
            Percentile percentile90 = new Percentile();
            durAvg = durAvg / countCallComplete;
            dur90 = (long) percentile90.evaluate(
                    callList
                            .stream()
                            .filter(x -> (x.getDuration() > 0 & x.getTimeBegin() >= startTime && x.getTimeBegin() <= stopTime))
                            .mapToDouble(Call::getDuration)
                            .toArray(), 90);
        }

        if (durMin == 999999999999999999L) {
            durMin = 0L;
        }

        // статистика выполнения процессов в БПМ
        DBResponse dbResponse = dataFromSQL.getStatisticsFromBpm(
                countCall, // для демо при отсутсвии БД
                keyBpm,
                startTime,
                stopTime);

        if (sqlSelect == null) {
            sqlSelect = dbResponse.getSqlSelect();
        }

        // ошибки (не фиксируем при сборе статистики за весь период)
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
            4  - tpc
            5  - tpcRs
            6  - countCall
            7  - countCallComplete
            8  - dbComplete
            9  - dbRunning
            10 - errors
        */
        metricsList.add(new DateTimeValue(
                stopTime,
                Arrays.asList(
                        (int) durMin,
                        (int) durAvg,
                        (int) dur90,
                        (int) durMax,

                        tpc,
                        tpcRs,

                        countCall,
                        countCallComplete,

                        dbResponse.getIntValue("COMPLETE"),
                        dbResponse.getIntValue("RUNNING"),

                        countError)));

        prevStartTimeStatistic = stopTime;
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
        this.running = true;
        this.warming = warming;
        vuList.clear();
        futureList.clear();
        errorList.clear();
        threadCount = new AtomicInteger(0);
        vuCount = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(1);
//        ExecutorService executorService = Executors.newFixedThreadPool(maxCountVU + 1); // пул VU
        executorService = Executors.newCachedThreadPool(); // пул VU (расширяемый)
        ExecutorService executorServiceAwaitAndAddVU = Executors.newFixedThreadPool(1); // пул для задачи контроля выполнения

        if (!warming) { // После прогрева нагрузка сервисов должна начаться одновременно
            if (!multiRun.isWarmingComplete()) {
                LOG.info("{}: Ожидание завершения прогрева всех сервисов...", name);
                while (!multiRun.isWarmingComplete()) { // ждем завершения прогрева всех сервисов
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

        LOG.info("##### {}" +
                        "\ntestStartTime: {}" +
                        "\ntestStopTime: {}" +
                        "\nnextTimeAddVU: {}",
                name,
                sdf1.format(testStartTime),
                sdf1.format(testStopTime),
                sdf1.format(nextTimeAddVU));

        vuListAdd(testStartTime, 0); // стартовое количество VU

/*
        // подаем нагрузку заданное время (возможно прерывание сбросом runnable)
        while (running && System.currentTimeMillis() < testStopTime) {
        }
*/

        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            LOG.error("{}\n", name, e);
        }

        // объединяем запросы всех VU
        LOG.info("{}: Объединение метрик по всем VU...", name);
        try {
            for (Future<List<Call>> future : futureList) {
                if (!warming) {
                    callList.addAll(future.get());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("{}\n", name, e);
        } catch (ExecutionException e) {
            LOG.error("{}\n", name, e);
        }
        executorServiceAwaitAndAddVU.shutdown();
        executorService.shutdown();
        LOG.info("{}: Завершено объединение метрик по всем VU", name);

        if (warming) {
            vuCountMin = vuCountMinMem;
            vuCountMax = vuCountMaxMem;
            LOG.info("{}: Прогрев завершен...", name);
            this.warming = false;
        } else {
            LOG.info("{}: Сбор статистики...", name);
            try { // даем время завершиться начатым заданиям
//                Thread.sleep(Math.max(pacing * 10, 20000));
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // сбор статистики после снятия нагрузки
            testStopTime = System.currentTimeMillis();
            long timeStart = testStartTime;
            long timeStop = testStopTime; // + statisticsStepTime * 1000L;

            // сбросим VU на конец теста
            vuList.add(new DateTimeValue(testStopTime, getVuCount()));

            // статистику за весь период сохраним нелевым элементом
            getStatistics(timeStart, timeStop);

            prevStartTimeStatistic = testStartTime; // для определения временного диапазона снятия метрик
            while (timeStart <= timeStop) {
                timeStart = timeStart + statisticsStepTime * 1000L;
                getStatistics(timeStart);
            }
            LOG.info("{}: Завершен сбор статистики", name);

            // сохраняем результаты в HTML - файл
            report.saveReportHtml(this, pathReport);
        }
    }

}