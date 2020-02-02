package ru.utils.load.utils;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.data.ErrorGroupComment;
import ru.utils.load.data.ErrorRs;
import ru.utils.load.runnable.CallableVU;
import ru.utils.load.runnable.RunnableAwait;
import ru.utils.files.PropertiesService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class MultiRunService {
    private static final Logger LOG = LogManager.getLogger(MultiRunService.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private List<DateTimeValue> vuListWarming = new CopyOnWriteArrayList<>(); // количество виртуальных пользователей на момент времени (разогрев)
    private List<Call> callListWarming = new CopyOnWriteArrayList<>(); // список вызовов API (разогрев)

    private List<DateTimeValue> vuList = new CopyOnWriteArrayList<>(); // количество виртуальных пользователей на момент времени
    private List<Call> callList = new CopyOnWriteArrayList<>(); // список вызовов API
    private List<DateTimeValue> tpcList = new CopyOnWriteArrayList<>(); // TPC
    private List<DateTimeValue> durationList = new CopyOnWriteArrayList<>(); // Длительность выполнения (min, avg, prc90, max)
    private List<DateTimeValue> bpmProcessStatisticList = new CopyOnWriteArrayList<>(); // статистика обработки задач
    private List<ErrorRs> errorList = new CopyOnWriteArrayList<>(); // ошибки при выполнении API
    private List<DateTimeValue> errorGroupList = new CopyOnWriteArrayList<>(); // ошибки при выполнении API сгруппированы
    private List<ErrorGroupComment> errorGroupCommentList = new CopyOnWriteArrayList<>(); // количество ошибок по типам
    private List<String> reportList = new CopyOnWriteArrayList<>(); // текстовый отчет

    private String name;
    private long testStartTime;
    private long testStopTime;
    private long nextTimeAddVU;
    private long nextTimeStatiscitcs;
    private Long prevStartTimeStatistic;


    // параметры теста
    private int testDuration = 1; // длительность теста в минутах

    private int minCountVU = 10;   // начальное количество виртуальных пользователей (VU)
    private int maxCountVU = 100;   // максимальное количество VU
    private int stepTimeVU = 5;    // через какое время в секундах увеличиваем количество VU
    private int stepCountVU = 10;  // на сколько увеличиваем количество VU

    private double pacing = 1;     // задержка перед выполнением следующей итерации (сек)
    private int pacingType = 1;    // 0 - задержка от момента старта операции (без ожидания выполнения); 1 - задержка от момента старта операции (с учетом ожидания выполения); 2 - задержка от момента завершения выполнения операции;

    private int stepTimeStatistics = 5;    // частота снятия метрик - через данное количество секунд
    private boolean statisticsOnLine = true; // снятие метрик во время подачи нагрузки

    private int countVU = 0; // текущее количество VU

    private boolean stopTestOnError = false; // прерывать тест при большом количестве ошибок
    private int countErrorForStopTest = 100; // количество ошибок для прерывания теста

    private String grafanaHostsDetailUrl; // Графана - Хосты детализованно (URL)
    private String splunkUrl; // Спланк (URL)

    boolean running = true; // тест продолжается

    private static final String PROPERTIES_FILE = "load.properties";
    private static PropertiesService propertiesService = new PropertiesService(new LinkedHashMap<String, String>() {{
        put("TEST_DURATION", "1");
        put("MIN_COINT_VU", "10");
        put("MAX_COUNT_VU", "100");
        put("STEP_TIME_VU", "5");
        put("STEP_COUNT_VU", "10");
        put("PACING", "1");
        put("PACING_TYPE", "1");
        put("STEP_TIME_STATISTICS", "5");
        put("STATISTICS_ONLINE", "true");
        put("STOP_TEST_ON_ERROR", "true");
        put("COUNT_ERROR_FOR_STOP_TEST", "100");

        put("DB_URL", "");
        put("DB_USER_NAME", "");
        put("DB_USER_PASSWORD", "");

        put("GRAFANA_HOSTS_DETAIL", "");
        put("SPLUNK", "");
    }});

    private DataFromSQL dataFromSQL = new DataFromSQL(); // получение данных из БД
    private Report report = new Report();

    /**
     * инициализация параметров
     */
    public void init(String name) {
        this.name = name;
        propertiesService.readProperties(PROPERTIES_FILE);

        testDuration = propertiesService.getInt("TEST_DURATION");
        minCountVU = propertiesService.getInt("MIN_COINT_VU");
        maxCountVU = propertiesService.getInt("MAX_COUNT_VU");
        stepTimeVU = propertiesService.getInt("STEP_TIME_VU");
        stepCountVU = propertiesService.getInt("STEP_COUNT_VU");
        pacing = propertiesService.getDouble("PACING");
        pacingType = propertiesService.getInt("PACING_TYPE");
        stepTimeStatistics = propertiesService.getInt("STEP_TIME_STATISTICS");
        statisticsOnLine = propertiesService.getBoolean("STATISTICS_ONLINE");
        stopTestOnError = propertiesService.getBoolean("STOP_TEST_ON_ERROR");
        countErrorForStopTest = propertiesService.getInt("COUNT_ERROR_FOR_STOP_TEST");
        grafanaHostsDetailUrl = propertiesService.getString("GRAFANA_HOSTS_DETAIL");
        splunkUrl = propertiesService.getString("SPLUNK");

        if (!checkParam()) { // ошибка в параметрах
            System.exit(1);
        }

        dataFromSQL.init(
                testStartTime,
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

    public long getTestStartTime() {
        return testStartTime;
    }

    public long getTestStopTime() {
        return testStopTime;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public int getMinCountVU() {
        return minCountVU;
    }

    public int getMaxCountVU() {
        return maxCountVU;
    }

    public int getStepTimeVU() {
        return stepTimeVU;
    }

    public int getStepCountVU() {
        return stepCountVU;
    }

    public double getPacing() {
        return pacing;
    }

    public int getPacingType() {
        return pacingType;
    }

    public int getCountVU() {
        return countVU;
    }


    /**
     * Параметры теста
     *
     * @return
     */
    public String getParams() {
        StringBuilder res = new StringBuilder("\n<br><table border=\"1\"><tbody>\n");
        res.append("<tr><td>Длительность теста (мин):</td><td>")
                .append(testDuration)
                .append("</td></tr>\n")
                .append("<tr><td>Задержка перед выполнением следующей операции (сек):</td><td>")
                .append(pacing)
                .append("</td></tr>\n")
                .append("<tr><td>Режим задержки (сек):<br>0 - задержка от момента старта операции (без ожидания выполнения);<br>1 - задержка от момента старта операции (с учетом ожидания выполения);<br>2 - задержка от момента завершения выполнения операции;</td><td>")
                .append(pacingType)
                .append("</td></tr>\n")
                .append("<tr><td>Периодичность снятия метрик (сек):</td><td>")
                .append(stepTimeStatistics)
                .append("</td></tr>\n")
                .append("<tr><td>Снятие метрик во время подачи нагрузки:</td><td>")
                .append(statisticsOnLine ? "Да" : "Нет")
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
     * Добавляем метрику вызова
     *
     * @param call
     */
    public void callListAdd(Call call) {
        synchronized (callList) {
            callList.add(call);
        }
    }

    /**
     * Добавляем метрику ошибки
     *
     * @param time
     * @param text
     */
    public void errorListAdd(long time, String text) {
        synchronized (errorList) {
            errorList.add(new ErrorRs(time, text));
        }
    }

    /**
     * Настало время снятия статистики
     *
     * @return
     */
    public boolean isTimeStatistics() {
        boolean r = false;
        if (System.currentTimeMillis() > nextTimeStatiscitcs) {
            nextTimeStatiscitcs = System.currentTimeMillis() + stepTimeStatistics * 1000L; // время следующего снятия статистики
            r = true;
        }
        return r;
    }

    /**
     * Стартовали все VU ?
     *
     * @return
     */
    public boolean isStartedAllVU() {
        return countVU < maxCountVU ? false : true;
    }

    /**
     * Старт нового VU
     */
    public boolean startVU() {
        boolean r = false;
        if (countVU < maxCountVU) {
            countVU++;
            r = true;
        }
        return r;
    }

    /**
     * Настало время добавления VU
     *
     * @return
     */
    public boolean isTimeAddVU() {
        boolean r = false;
        if (System.currentTimeMillis() > nextTimeAddVU) {
            nextTimeAddVU = System.currentTimeMillis() + stepTimeVU * 1000L; // время следующего увеличения количества VU
            r = true;
        }
        return r;
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
        if (running) {
            LOG.warn("Из-за большого количества ошибок прерываем подачу нагрузки...");
        }
        running = false;
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
        if (stepTimeVU > 0 && stepCountVU > 0) {
            if (minCountVU + stepCountVU * ((testDuration * 60L) / stepTimeVU - 1) < maxCountVU) {
                LOG.error("\n\nВнимание!!! Заданные параметры не позволяют выйти на планируемую максимальную нагрузку за отведенное время ({} < {})",
                        minCountVU + stepCountVU * ((testDuration * 60L) / stepTimeVU - 1),
                        maxCountVU);
                r = false;
            }

        } else if (minCountVU > maxCountVU) {
            LOG.error("\n\nВнимание!!! Минимальное значение VU ({}) больше максимального значения VU ({})", minCountVU, maxCountVU);
            r = false;

        } else if ((stepTimeVU == 0 || stepCountVU == 0) && minCountVU < maxCountVU) {
            LOG.error("\n\nВнимание!!! Минимальное значение VU ({}) меньше максимального значения VU ({}), при этом параметры шага - время или количество равны 0 ({} / {})", minCountVU, maxCountVU, stepTimeVU, stepCountVU);
            r = false;
        }

        return r;
    }

    /**
     * Сохраняем длительность выполнения для вызова с rqUid из параметра
     *
     * @param rqUid
     * @param timeEnd
     */
    public void setTimeEndInCall(
            String rqUid,
            long timeEnd) {

        LOG.trace("Старт update {}", rqUid);
        boolean find = false;
        for (int i = 0; i < callList.size(); i++) {
//            synchronized (callList) {
            if (callList.get(i).getRqUid().equals(rqUid)) {
                find = true;
                if (callList.get(i).getTimeEnd() == 0) {
                    synchronized (callList) {
                        callList.get(i).setTimeEnd(timeEnd);
                    }
                } else {
                    LOG.error("Попытка изменить зафиксированную запись {}",
                            callList.get(i).getRqUid(),
                            callList.get(i).getTimeBegin(),
                            callList.get(i).getTimeEnd());
                }
                break;
            }
//            }
        }
        if (find) {
            LOG.trace("update Ok {}", rqUid);
        } else {
            LOG.warn("update Error {}", rqUid);
        }
    }


    /**
     * Снятие метрик - сам процесс
     */
    private void getStatistics(long stopTime) {
        getStatistics(prevStartTimeStatistic + 1, stopTime);
    }

    public void getStatistics(long startTime, long stopTime) {
//        long startTime = prevStartTimeStatistic + 1;

        LOG.info("Статистика {} - {}", sdf1.format(startTime), sdf1.format(stopTime));

        long minDuraton = 999999999999999999L;
        long maxDuration = 0L;
        long avgDuration = 0L;
        int countCallAll = 0;
        int countCallAvg = 0;
        int countCallTpc = 0;

        synchronized (callList) {
            for (int i = 0; i < callList.size(); i++) {
                countCallAll++;
                if (callList.get(i).getTimeBegin() >= startTime && callList.get(i).getTimeBegin() <= stopTime) {
                    if (callList.get(i).getDuration() > 0) {
                        minDuraton = Math.min(minDuraton, callList.get(i).getDuration());
                        maxDuration = Math.max(maxDuration, callList.get(i).getDuration());
                        avgDuration = avgDuration + callList.get(i).getDuration();
                        countCallAvg++;
                    }
                    countCallTpc++;
                }
            }
        }

        double tpc = countCallTpc / (stepTimeStatistics * 1.00);
        synchronized (tpcList) {
            tpcList.add(new DateTimeValue(stopTime, tpc)); // TPC
        }

        long percentileValue = 0L;
        if (countCallAvg > 0) {
            Percentile percentile = new Percentile();
            avgDuration = avgDuration / countCallAvg;
            synchronized (callList) {
                percentileValue = (long) percentile.evaluate(
                        callList
                                .stream()
                                .filter(x -> (x.getDuration() > 0 & x.getTimeBegin() >= startTime && x.getTimeBegin() <= stopTime))
                                .mapToDouble(Call::getDuration)
                                .toArray(), 90);
            }
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

        // статистика выполнения процессов
        dataFromSQL.getStatisticsFromBpm(
                stopTime,
                callList,
                bpmProcessStatisticList);

        // ошибки
        int countError = 0;
        int countErrorAll = 0;
        synchronized (errorList) {
            for (int i = 0; i < errorList.size(); i++) {
                countErrorAll++;
                if (errorList.get(i).getTime() >= startTime && errorList.get(i).getTime() <= stopTime) {
                    countError++;
                }
            }
        }
        synchronized (errorGroupList) {
//            if (countError > 0) {
            errorGroupList.add(new DateTimeValue(stopTime, countError)); // количество ошибок
//            }
        }

/*
        long duration = (stopTime - testStartTime) / 1000; // длительность теста
        StringBuffer stringBuffer = new StringBuffer("\nРезультат выполнения:");
        stringBuffer.append("\nПериод:                            ")
                .append(sdf1.format(startTime))
                .append(" - ")
                .append(sdf1.format(stopTime));
        stringBuffer.append("\nДлительность теста (сек):          ").append(duration);
        stringBuffer.append("\nКоличество VU:                     ").append(countVU);
        stringBuffer.append("\nКоличество вызовов с начала теста: ").append(countCallAll);
        stringBuffer.append("\nКоличество вызовов за период:      ").append(countCallTpc);
        stringBuffer.append("\nМинимальная длительность (мс):     ").append(minDuraton);
        stringBuffer.append("\nМаксимальная длительность (мс):    ").append(maxDuration);
        stringBuffer.append("\nСредняя длительность (мс):         ").append(avgDuration);
        stringBuffer.append("\nПерцентиль 90 (мс):                ").append(percentileValue);
        stringBuffer.append("\nTPC:                               ").append(tpc);
        stringBuffer.append("\nОшибки с начала теста:             ").append(countErrorAll);
        stringBuffer.append("\nОшибки за период:                  ").append(countError);

        stringBuffer.append("\n");
        synchronized (reportList) {
            reportList.add(stringBuffer.toString());
        }
//        LOG.info("{}", stringBuffer.toString());
        System.out.println(stringBuffer.toString()); // выводить в консоль всегда

 */
        synchronized (prevStartTimeStatistic) {
            prevStartTimeStatistic = stopTime;
        }
    }

    /**
     * Снятие метрик - с ожиданием или нет
     *
     * @param executorService
     * @param wait
     */
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


    /**
     * Нагрузка
     * @param baseScript
     */
    public void start(ScriptRun baseScript) {
        start(baseScript, true);  // разогрев
        start(baseScript, false); // нагрузка
    }

    /**
     * Нагрузка
     * @param baseScript
     * @param isWarming
     */
    public void start(ScriptRun baseScript, boolean isWarming) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
//        ExecutorService executorService = Executors.newFixedThreadPool(maxCountVU + 1); // пул VU
        ExecutorService executorService = Executors.newCachedThreadPool(); // пул VU (расширяемый)
        ExecutorService executorServiceAwait = Executors.newFixedThreadPool(1); // пул для задачи контроля выполнения
        executorServiceAwait.submit(new RunnableAwait(
                countDownLatch,
                executorService,
                this));

        int memMinCountVU = minCountVU;
        int memMaxCountVU = maxCountVU;
        testStartTime = System.currentTimeMillis(); // время старта теста
        if (isWarming) {
            LOG.info("Разогрев...");
            minCountVU = 5;
            maxCountVU = 5;
            testStopTime = testStartTime + stepTimeStatistics * 3000L; // время завершения теста (разогрев)
        } else {
            testStopTime = testStartTime + testDuration * 60000L; // время завершения теста
        }
        prevStartTimeStatistic = testStartTime; // для определения временного диапазона снятия метрик
        nextTimeAddVU = testStartTime + stepTimeVU * 1000L; // время следующего увеличения количества VU (при запуске необходимо инициировать стартовое количество)
        nextTimeStatiscitcs = testStartTime + stepTimeStatistics * 1000L; // время следующего снятия статистики
        countVU = 0; // текущее количество VU

        LOG.info("#####" +
                        "\ntestStartTime: {}" +
                        "\ntestStopTime: {}" +
                        "\nnextTimeAddVU: {}" +
                        "\nnextTimeStatiscitcs: {}",
                sdf1.format(testStartTime),
                sdf1.format(testStopTime),
                sdf1.format(nextTimeAddVU),
                sdf1.format(nextTimeStatiscitcs));

        vuList.clear();
        vuList.add(new DateTimeValue(testStartTime, minCountVU)); // стартовое количество VU

        List<Future<List<Call>>> futureList = new ArrayList<>();

        // подаем нагрузку заданное время (возможно прерывание сбросом runnable)
        while (running && System.currentTimeMillis() < testStopTime) {

            // настало время снимать статистику
            if (statisticsOnLine && isTimeStatistics()) {
                if (getCountVU() > 0) {
                    getStatistics(executorService, false);
                }
            }

            // настало время увеличения количества VU
            if (isTimeAddVU() || getCountVU() < getMinCountVU()) { // настало время увеличения пользователей (или первоначальная инициализация)
                if (!isStartedAllVU()) { // не все VU стартовали
                    int step = (getCountVU() == 0 ? getMinCountVU() : getStepCountVU());
                    for (int u = 0; u < step; u++) {
                        if (startVU()) {
/*
                            executorService.submit(new RunnableForMultiLoad(
                                    getCountVU(),
                                    baseScript,
                                    executorService,
                                    this));
*/

/*
                            Future<List<Call>> futureCall = executorService.submit(new CallableVU(
                                    getCountVU(),
                                    baseScript,
                                    executorService,
                                    this));
                            futureList.add(futureCall);
*/
                            futureList.add(executorService.submit(new CallableVU(
                                    getCountVU(),
                                    baseScript,
                                    executorService,
                                    this)));
                        }
                    }
                }
                LOG.info("Текущее количество виртуальных пользователей {} из {}",
                        getCountVU(),
                        getMaxCountVU());

                vuList.add(new DateTimeValue(System.currentTimeMillis(), getCountVU())); // фиксация активных VU
            }
        }

        executorServiceAwait.shutdown();
        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            LOG.error("", e);
        }

/*
        LOG.warn("Не прерывайте работы программы, пауза {} сек...", stepTimeStatistics);
        try {
            Thread.sleep(stepTimeStatistics * 1000L);
        } catch (
                InterruptedException e) {
            LOG.error("", e);
        }
*/

        // объединяем запросы всех VU
        try {
/*
            for (int f = 0; f < futureList.size(); f++) {
                if (isWarming) {
                    callListWarming.addAll(futureList.get(f).get());
                } else {
                    callList.addAll(futureList.get(f).get());
                }
            }
*/
            for (Future<List<Call>> future : futureList){
                if (isWarming) {
                    callListWarming.addAll(future.get());
                } else {
                    callList.addAll(future.get());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("", e);
        } catch (ExecutionException e) {
            LOG.error("", e);
        }
        executorService.shutdown();

        // сбросим VU на конец теста
        vuList.add(new DateTimeValue(System.currentTimeMillis(), 0)); // фиксация активных VU


        if (isWarming) {
            minCountVU = memMinCountVU;
            maxCountVU = memMaxCountVU;
            LOG.info("Разогрев завершен...");
        } else {
            if (statisticsOnLine) {
                // статистика на конец теста
                getStatistics(executorService, true);
            } else {
                // сбор статистики после снятия нагрузки
                long timeStart = testStartTime;
                long timeStop = testStopTime + stepTimeStatistics * 1000L;
                while (timeStart <= timeStop) {
                    timeStart = timeStart + stepTimeStatistics * 1000L;
                    getStatistics(timeStart);
                }
            }

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
//        saveReportHtml(stringBuilder);

//        testStartTime = testStartTime + stepTimeStatistics * 2000; //ToDo:
            report.saveReportHtml(this);
        }
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

    public String getGrafanaHostsDetailUrl() {
        return grafanaHostsDetailUrl;
    }

    public String getSplunkUrl() {
        return splunkUrl;
    }
}