package ru.utils.load.data;

/**
 * Список метрик
 * @author Belov Sergey
 */
public enum Metric {
    KEY, // универсальная метрика (для списка с одним параметром)
    DUR_MIN,
    DUR_AVG,
    DUR_90,
    DUR_MAX,
    TPS,
    TPS_RS,
    COUNT_CALL,
    COUNT_CALL_RS,
    DB_COMPLETED,
    DB_RUNNING,
    DB_FAILED,
    DB_LOST,
    DB_DUR_MIN,
    DB_DUR_AVG,
    DB_DUR_90,
    DB_DUR_MAX,
    ERRORS;
}
