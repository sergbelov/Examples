package ru.utils.load.data.testplan;

public class TestPlan {
    int apiNum;                // Порядковый номер сервиса внутри сценария (класса)
    String name;               // Наименование сервиса
    String keyBpm;             // задача в БД БПМ
    boolean async;             // Асинхронный вызов
    int testDuration_min;      // Длительность теста в минутах
    int vuCountMin;            // Стартовое количество виртуальных пользователей
    int vuCountMax;            // Максимальное количество виртуальных пользователей
    int vuStepTime_sec;        // Временной шаг для увеличения VU (сек)
    int vuStepTimeDelay_ms;    // Задержка между стартами каждого VU (мс)
    int vuStepCount;           // Шаг увеличения VU
    long pacing_ms;            // Длительность операции (мс)
    int pacingType;            // 0 - задержка от момента старта операции (без ожидания выполнения); 1 - задержка от момента старта операции (с учетом ожидания выполения); 2 - задержка от момента завершения выполнения операции;
    long responseTimeMax_ms;   // Максимально допустимое значение Response time (мс)

    public TestPlan() {
    }

    public TestPlan(
            int apiNum,
            String name,
            String keyBpm,
            boolean async,
            int testDuration_min,
            int vuCountMin,
            int vuCountMax,
            int vuStepTime_sec,
            int vuStepTimeDelay_ms,
            int vuStepCount,
            long pacing_ms,
            int pacingType,
            long responseTimeMax_ms
    ) {

        this.apiNum = apiNum;
        this.name = name;
        this.keyBpm = keyBpm;
        this.async = async;
        this.testDuration_min = testDuration_min;
        this.vuCountMin = vuCountMin;
        this.vuCountMax = vuCountMax;
        this.vuStepTime_sec = vuStepTime_sec;
        this.vuStepTimeDelay_ms = vuStepTimeDelay_ms;
        this.vuStepCount = vuStepCount;
        this.pacing_ms = pacing_ms;
        this.pacingType = pacingType;
        this.responseTimeMax_ms = responseTimeMax_ms;
    }

    public int getApiNum() { return apiNum; }

    public String getName() {
        return name;
    }

    public String getKeyBpm() {
        return keyBpm;
    }

    public boolean isAsync() { return async; }

    public int getTestDuration_min() {
        return testDuration_min;
    }

    public int getVuCountMin() {
        return vuCountMin;
    }

    public int getVuCountMax() {
        return vuCountMax;
    }

    public int getVuStepTime_sec() {
        return vuStepTime_sec;
    }

    public int getVuStepTimeDelay_ms() {
        return vuStepTimeDelay_ms;
    }

    public int getVuStepCount() {
        return vuStepCount;
    }

    public long getPacing_ms() { return pacing_ms; }

    public int getPacingType() {
        return pacingType;
    }

    public long getResponseTimeMax_ms() { return responseTimeMax_ms; }
}
