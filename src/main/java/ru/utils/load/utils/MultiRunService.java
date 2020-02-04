package ru.utils.load.utils;

import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.data.ErrorGroupComment;
import ru.utils.load.data.ErrorRs;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.files.PropertiesService;
import ru.utils.load.runnable.CallableVU;
import ru.utils.load.runnable.RunnableAwaitAndAddVU;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiRunService {
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private List<DateTimeValue> vuList = new CopyOnWriteArrayList<>(); // количество виртуальных пользователей на момент времени
    private List<Call> callList = new ArrayList<>(); // список вызовов API
    private List<DateTimeValue> tpcList = new ArrayList<>(); // TPC
    private List<DateTimeValue> durationList = new ArrayList<>(); // Длительность выполнения (min, avg, prc90, max)
    private List<DateTimeValue> bpmProcessStatisticList = new ArrayList<>(); // статистика обработки задач в БПМ
    private List<ErrorRs> errorList = new CopyOnWriteArrayList<>(); // ошибки при выполнении API
    private List<DateTimeValue> errorGroupList = new ArrayList<>(); // ошибки при выполнении API сгруппированы
    private List<ErrorGroupComment> errorGroupCommentList = new ArrayList<>(); // количество ошибок по типам

    private List<String> reportList = new ArrayList<>(); // текстовый отчет (для отладки)

    private ScriptRun baseScript;
    private List<Future<List<Call>>> futureList = new ArrayList<>();
    private ExecutorService executorService;

    private AtomicInteger threadCount; // // счетчик потоков
    private AtomicInteger vuCount; // текущее количество VU

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
    private String splunkUrl; // Спланк (URL)
    private String csmUrl; // CSM (URL)

    boolean running = true; // тест продолжается
    boolean warning = true; // разогрев

    private static final String PROPERTIES_FILE = "load.properties";
    private static PropertiesService propertiesService = new PropertiesService(new LinkedHashMap<String, String>() {{
        put("TEST_DURATION", "1");
        put("VU_COINT_MIN", "10");
        put("VU_COUNT_MAX", "100");
        put("VU_STEP_TIME", "5");
        put("VU_STEP_TIME_DELAY", "1");
        put("VU_STEP_COUNT", "10");
        put("PACING", "1000");
        put("PACING_TYPE", "1");
        put("STATISTICS_STEP_TIME", "5");
        put("STOP_TEST_ON_ERROR", "true");
        put("COUNT_ERROR_FOR_STOP_TEST", "100");

        put("DB_URL", "");
        put("DB_USER_NAME", "");
        put("DB_USER_PASSWORD", "");

        put("GRAFANA_HOSTS_DETAIL", "");
        put("SPLUNK", "");
        put("CSM", "");
    }});

    private DataFromSQL dataFromSQL = new DataFromSQL(); // получение данных из БД БПМ
    private Report report = new Report();

    /**
     * инициализация параметров
     */
    public void init(String name) {
        this.name = name;
        propertiesService.readProperties(PROPERTIES_FILE);

        testDuration = propertiesService.getInt("TEST_DURATION");
        vuCountMin = propertiesService.getInt("VU_COINT_MIN");
        vuCountMax = propertiesService.getInt("VU_COUNT_MAX");
        vuStepTime = propertiesService.getInt("VU_STEP_TIME");
        vuStepTimeDelay = (long) (propertiesService.getDouble("VU_STEP_TIME_DELAY") * 1000);
        vuStepCount = propertiesService.getInt("VU_STEP_COUNT");
        pacing = propertiesService.getLong("PACING");
        pacingType = propertiesService.getInt("PACING_TYPE");
        statisticsStepTime = propertiesService.getInt("STATISTICS_STEP_TIME");
        stopTestOnError = propertiesService.getBoolean("STOP_TEST_ON_ERROR");
        countErrorForStopTest = propertiesService.getInt("COUNT_ERROR_FOR_STOP_TEST");
        grafanaHostsDetailUrl = propertiesService.getString("GRAFANA_HOSTS_DETAIL");
        splunkUrl = propertiesService.getString("SPLUNK");
        csmUrl = propertiesService.getString("CSM");

        if (!checkParam()) { // ошибка в параметрах
            System.exit(1);
        }

        dataFromSQL.init( // подключаемся к БД
                propertiesService.getString("DB_URL"),
                propertiesService.getString("DB_USER_NAME"),
                propertiesService.getStringDecode("DB_USER_PASSWORD"));
    }

    public void end() {
        dataFromSQL.end();
    }

    public MultiRunService() {
    }

    public String getName() {
        return name;
    }

    public DataFromSQL getDataFromSQL() {
        return dataFromSQL;
    }

    public List<DateTimeValue> getVuList() {
        return vuList;
    }

    public List<Call> getCallList() {
        return callList;
    }

    public List<DateTimeValue> getTpcList() {
        return tpcList;
    }

    public List<DateTimeValue> getDurationList() {
        return durationList;
    }

    public List<DateTimeValue> getBpmProcessStatisticList() {
        return bpmProcessStatisticList;
    }

    public List<ErrorRs> getErrorList() {
        return errorList;
    }

    public List<DateTimeValue> getErrorGroupList() {
        return errorGroupList;
    }

    public List<ErrorGroupComment> getErrorGroupCommentList() {
        return errorGroupCommentList;
    }

    public List<String> getReportList() {
        return reportList;
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

    public String getSplunkUrl() {
        return splunkUrl;
    }

    public String getCsmUrl() {
        return csmUrl;
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
        res.append("<tr><td>Длительность теста (мин):</td><td>")
                .append(testDuration)
                .append("</td></tr>\n")
                .append("<tr><td>Задержка перед выполнением следующей операции (мс):</td><td>")
                .append(pacing)
                .append("</td></tr>\n")
                .append("<tr><td>Режим задержки:<br>0 - задержка от момента старта операции (без ожидания выполнения);<br>1 - задержка от момента старта операции (с учетом ожидания выполения);<br>2 - задержка от момента завершения выполнения операции;</td><td>")
                .append(pacingType)
                .append("</td></tr>\n")
                .append("<tr><td>Периодичность снятия метрик (сек):</td><td>")
                .append(statisticsStepTime)
                .append("</td></tr>\n")
                .append("<tr><td>Прерывать тест при большом количестве ошибок:</td><td>")
                .append(stopTestOnError ? "Да" : "Нет")
                .append("</td></tr>\n")
                .append("<tr><td>Количество ошибок для прерывания теста:</td><td>")
                .append(countErrorForStopTest)
                .append("</td></tr>\n</tbody></table>\n");
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
        if (!warning) { // при разогреве ошибки не фиксируем
            synchronized (errorList) {
                errorList.add(new ErrorRs(time, text));
            }
        }
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
                                    getVuCount(),
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
                    LOG.info("Текущее количество виртуальных пользователей {} из {}",
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
     * Количество ошибок в последней группе
     *
     * @return
     */
    public int getErrorCountInLastGroup() {
        return errorGroupList.get(errorGroupList.size() - 1).getIntValue();
    }

    /**
     * Перестаем подавать новую нагрузку
     */
    public void stop() {
        if (!warning) { // разогрев не прерываем
            if (running) {
                LOG.warn("Из-за большого количества ошибок прерываем подачу нагрузки...");
            }
            running = false;
        }
    }

    /**
     * Подача нагрузки разрешена
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
            if (vuCountMin + vuStepCount * ((testDuration * 60L) / vuStepTime - 1) < vuCountMax) {
                LOG.error("\n\nВнимание!!! Заданные параметры не позволяют выйти на планируемую максимальную нагрузку за отведенное время ({} < {})",
                        vuCountMin + vuStepCount * ((testDuration * 60L) / vuStepTime - 1),
                        vuCountMax);
                r = false;
            }
        }
        if (vuCountMin > vuCountMax) {
            LOG.error("\n\nВнимание!!! Минимальное значение VU ({}) больше максимального значения VU ({})",
                    vuCountMin,
                    vuCountMax);
            r = false;

        }
        if ((vuStepTime == 0 || vuStepCount == 0) && vuCountMin < vuCountMax) {
            LOG.error("\n\nВнимание!!! Минимальное значение VU ({}) меньше максимального значения VU ({}), при этом параметры шага - время или количество равны 0 ({} / {})",
                    vuCountMin,
                    vuCountMax,
                    vuStepTime,
                    vuStepCount);
            r = false;

        }
        if (vuStepTime < statisticsStepTime) {
            LOG.error("\n\nВнимание!!! Пользователи добавляются чаще чем снимается статистика ({} / {})",
                    vuStepTime,
                    statisticsStepTime);
            r = false;
        }
        if (vuStepTime * 1000 < vuStepTimeDelay * vuStepCount) {
            LOG.error("\n\nВнимание!!! Заданное количество пользователей {} с задержкой {} мс не успеет стартовать за время между шагами {} мс",
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
        LOG.debug("Статистика {} - {}", sdf1.format(startTime), sdf1.format(stopTime));

        long minDuraton = 999999999999999999L;
        long maxDuration = 0L;
        long avgDuration = 0L;
        int countCallAll = 0;
        int countCallComplete = 0;
        int countCallTpc = 0;

        for (int i = 0; i < callList.size(); i++) {
            countCallAll++;
            if (callList.get(i).getTimeBegin() >= startTime && callList.get(i).getTimeBegin() <= stopTime) {
                if (callList.get(i).getDuration() > 0) {
                    minDuraton = Math.min(minDuraton, callList.get(i).getDuration());
                    maxDuration = Math.max(maxDuration, callList.get(i).getDuration());
                    avgDuration = avgDuration + callList.get(i).getDuration();
                    countCallComplete++;
                }
                countCallTpc++;
            }
        }

        double tpc = countCallTpc / (statisticsStepTime * 1.00);
        double tpcComplete = countCallComplete / (statisticsStepTime * 1.00);
        tpcList.add(new DateTimeValue(stopTime, Arrays.asList(tpc, tpcComplete))); // TPC

        long percentileValue = 0L;
        if (countCallComplete > 0) {
            Percentile percentile = new Percentile();
            avgDuration = avgDuration / countCallComplete;
            percentileValue = (long) percentile.evaluate(
                    callList
                            .stream()
                            .filter(x -> (x.getDuration() > 0 & x.getTimeBegin() >= startTime && x.getTimeBegin() <= stopTime))
                            .mapToDouble(Call::getDuration)
                            .toArray(), 90);
        }

        if (minDuraton == 999999999999999999L) {
            minDuraton = 0L;
        }

        durationList.add(new DateTimeValue(
                stopTime,
                Arrays.asList(
                        (int) minDuraton,
                        (int) avgDuration,
                        (int) percentileValue,
                        (int) maxDuration)));

        // статистика выполнения процессов в БПМ
        dataFromSQL.getStatisticsFromBpm(
                startTime,
                stopTime,
                callList,
                bpmProcessStatisticList);

        // ошибки
        int countError = 0;
        int countErrorAll = 0;
        for (int i = 0; i < errorList.size(); i++) {
            countErrorAll++;
            if (errorList.get(i).getTime() >= startTime && errorList.get(i).getTime() <= stopTime) {
                countError++;
            }
        }
        errorGroupList.add(new DateTimeValue(stopTime, countError)); // количество ошибок
        prevStartTimeStatistic = stopTime;
    }

/*
    public void getStatistics(
            ExecutorService executorService,
            boolean wait) {

        if (wait) { // ожидаем
            getStatistics(System.currentTimeMillis());
        } else {
            executorService.submit(new Runnable() { // без ожидания выполнения (используем пул для фонов)
                @Override
                public void run() {
                    getStatistics(System.currentTimeMillis());
                }
            });
        }
    }
*/


    /**
     * Нагрузка
     *
     * @param baseScript
     */
    public void start(ScriptRun baseScript) {
        this.baseScript = baseScript;
        start(true);  // разогрев
        start(false); // нагрузка
    }

    /**
     * Нагрузка
     *
     * @param isWarming
     */
    public void start(boolean isWarming) {
        this.running = true;
        this.warning = isWarming;
        vuList.clear();
        futureList.clear();
        errorList.clear();
        threadCount = new AtomicInteger(0);
        vuCount = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(1);
//        ExecutorService executorService = Executors.newFixedThreadPool(maxCountVU + 1); // пул VU
        executorService = Executors.newCachedThreadPool(); // пул VU (расширяемый)
        ExecutorService executorServiceAwaitAndAddVU = Executors.newFixedThreadPool(1); // пул для задачи контроля выполнения
        executorServiceAwaitAndAddVU.submit(new RunnableAwaitAndAddVU(
                countDownLatch,
                this));

        int vuCountMinMem = vuCountMin;
        int vuCountMaxMem = vuCountMax;
        testStartTime = System.currentTimeMillis(); // время старта теста
        if (isWarming) {
            LOG.info("Разогрев...");
            vuCountMin = 5;
            vuCountMax = 5;
            testStopTime = testStartTime + statisticsStepTime * 3000L; // время завершения теста (разогрев)
        } else {
            testStopTime = testStartTime + testDuration * 60000L; // время завершения теста
        }
        prevStartTimeStatistic = testStartTime; // для определения временного диапазона снятия метрик
        nextTimeAddVU = testStartTime + vuStepTime * 1000L; // время следующего увеличения количества VU (при запуске необходимо инициировать стартовое количество)

        LOG.info("#####" +
                        "\ntestStartTime: {}" +
                        "\ntestStopTime: {}" +
                        "\nnextTimeAddVU: {}",
                sdf1.format(testStartTime),
                sdf1.format(testStopTime),
                sdf1.format(nextTimeAddVU));

//        vuList.add(new DateTimeValue(testStartTime, vuCountMin)); // стартовое количество VU
        vuListAdd(testStartTime, 0); // стартовое количество VU

/*
        // подаем нагрузку заданное время (возможно прерывание сбросом runnable)
        while (running && System.currentTimeMillis() < testStopTime) {
        }
*/

        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            LOG.error("", e);
        }

        // объединяем запросы всех VU
        LOG.info("Объединение метрик по всем VU....");
        try {
            for (Future<List<Call>> future : futureList) {
                if (!isWarming) {
                    callList.addAll(future.get());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("", e);
        } catch (ExecutionException e) {
            LOG.error("", e);
        }
        executorServiceAwaitAndAddVU.shutdown();
        executorService.shutdown();
        LOG.info("Завершено объединение метрик по всем VU");

        if (isWarming) {
            vuCountMin = vuCountMinMem;
            vuCountMax = vuCountMaxMem;
            LOG.info("Разогрев завершен...");
        } else {
            LOG.info("Сбор статистики...");
//            LOG.warn("Не прерывайте работы программы, пауза {} сек...", 10);
            try { // даем время завершиться начатым заданиям
                Thread.sleep(Math.max(pacing * 10, 10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // сбор статистики после снятия нагрузки
            testStopTime = System.currentTimeMillis();
            long timeStart = testStartTime;
            long timeStop = testStopTime; // + statisticsStepTime * 1000L;

            // сбросим VU на конец теста
            vuList.add(new DateTimeValue(testStopTime, getVuCount())); // фиксация активных VU

            while (timeStart <= timeStop) {
                timeStart = timeStart + statisticsStepTime * 1000L;
                getStatistics(timeStart);
            }
            LOG.info("Завершен сбор статистики");

/*
        StringBuilder stringBuilder = new StringBuilder();
        for (int r = 0; r < reportList.size(); r++) {
            stringBuilder.append(reportList.get(r).toString());
        }
*/

/*
        for (int i = 0; i < vuList.size(); i++) {
            stringBuilder.append(sdf1.format(vuList.get(i).getTime()))
                    .append(", ")
                    .append(vuList.get(i).getTime())
                    .append(", ")
                    .append(vuList.get(i).getValue())
                    .append("\n");

        }
*/

/*
        for (int i = 0; i < tpcList.size(); i++) {
            stringBuilder.append(tpcList.get(i).getTime())
                    .append(" - ")
                    .append(tpcList.get(i).getDoubleValue())
                    .append("\n");

        }
*/

/*
        for (int i = 0; i < callList.size(); i++){
            LOG.info("{} {} {} {}",
                    i,
                    callList.get(i).getRqUid(),
                    callList.get(i).getTimeBegin(),
                    callList.get(i).getDuration());
        }
*/

/*
        for (int i = 0; i < bpmProcessStatisticList.size(); i++) {
            stringBuilder.append(sdf1.format(bpmProcessStatisticList.get(i).getPeriodBegin()))
                    .append(" - ")
                    .append(sdf1.format(bpmProcessStatisticList.get(i).getPeriodEnd()))
                    .append(" sent: ")
                    .append(bpmProcessStatisticList.get(i).getValue(0))
                    .append(" complete: ")
                    .append(bpmProcessStatisticList.get(i).getValue(1))
                    .append(" running: ")
                    .append(bpmProcessStatisticList.get(i).getValue(2))
                    .append("\n");
        }
*/

            // сохраняем результаты в HTML - файл
//            saveReportHtml(stringBuilder);
            report.saveReportHtml(this);
        }
    }

}