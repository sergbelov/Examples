package ru.utils.load.data.testplan;

public class TestPlan {
    int apiNum;                // Порядковый номер сервиса внутри сценария (класса)
    String name;               // Наименование сервиса
    int testDuration;          // Длительность теста в минутах
    int vuCountMin;            // Стартовое количество виртуальных пользователей
    int vuCountMax;            // Максимальное количество виртуальных пользователей
    int vuStepTime;            // Временной шаг для увеличения VU (сек)
    int vuStepTimeDelay;       // Задержка между стартами каждого VU (сек)
    int vuStepCount;           // Шаг увеличения VU
    long pacing;               // Длительность операции (мс)
    int pacingType;            // 0 - задержка от момента старта операции (без ожидания выполнения); 1 - задержка от момента старта операции (с учетом ожидания выполения); 2 - задержка от момента завершения выполнения операции;
    int statisticsStepTime;    // Шаг между снятиями статистики
    String keyBpm;             // задача в БД БПМ

    public TestPlan() {
    }

    public TestPlan(
            int apiNum,
            String name,
            int testDuration,
            int vuCountMin,
            int vuCountMax,
            int vuStepTime,
            int vuStepTimeDelay,
            int vuStepCount,
            long pacing,
            int pacingType,
            int statisticsStepTime,
            String keyBpm) {
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
        this.keyBpm = keyBpm;
    }

    public int getApiNum() { return apiNum; }

    public String getName() {
        return name;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public int getVuCountMin() {
        return vuCountMin;
    }

    public int getVuCountMax() {
        return vuCountMax;
    }

    public int getVuStepTime() {
        return vuStepTime;
    }

    public int getVuStepTimeDelay() {
        return vuStepTimeDelay;
    }

    public int getVuStepCount() {
        return vuStepCount;
    }

    public long getPacing() { return pacing; }

    public int getPacingType() {
        return pacingType;
    }

    public int getStatisticsStepTime() {
        return statisticsStepTime;
    }

    public String getKeyBpm() {
        return keyBpm;
    }
}