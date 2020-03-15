package ru.utils.load.data.graph;

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
    10 - dbFailed
    11 - dbLost
    12 - dbDurMin
    13 - dbDurAvg
    14 - dbDur90
    15 - dbDurMax
    16 - errors
//    17 - VU
//    18 - countBpmsJobEntityImpl
//    19 - countRetryPolicyJobEntityImpl
*/
public enum Metric {
    key,
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
