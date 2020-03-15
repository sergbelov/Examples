package ru.utils.load.data;

/**
 * Список метрик
 * @author Belov Sergey
 */
public enum Metric {
    key, // универсальная метрика (для списка с одним параметром)
    DurMin,
    DurAvg,
    Dur90,
    DurMax,
    Tps,
    TpsRs,
    CountCall,
    CountCallRs,
    DbCompleted,
    DbRunning,
    DbFailed,
    DbLost,
    DbDurMin,
    DbDurAvg,
    DbDur90,
    DbDurMax,
    Errors;
}
