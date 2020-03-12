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
*/
public enum VarInList {

    DurMin {
        public int getIndex() {
            return 0;
        }
    },
    DurAvg {
        public int getIndex() {
            return 1;
        }
    },
    Dur90 {
        public int getIndex() {
            return 2;
        }
    },
    DurMax {
        public int getIndex() {
            return 3;
        }
    },
    Tps {
        public int getIndex() {
            return 4;
        }
    },
    TpsRs {
        public int getIndex() {
            return 5;
        }
    },
    CountCall {
        public int getIndex() {
            return 6;
        }
    },
    CountCallRs {
        public int getIndex() {
            return 7;
        }
    },
    DbCompleted {
        public int getIndex() {
            return 8;
        }
    },
    DbRunning {
        public int getIndex() {
            return 9;
        }
    },
    DbFailed {
        public int getIndex() {
            return 10;
        }
    },
    DbLost {
        public int getIndex() {
            return 11;
        }
    },
    DbDurMin {
        public int getIndex() {
            return 12;
        }
    },
    DbDurAvg {
        public int getIndex() {
            return 13;
        }
    },
    DbDur90 {
        public int getIndex() {
            return 14;
        }
    },
    DbDurMax {
        public int getIndex() {
            return 15;
        }
    },
    Errors {
        public int getIndex() {
            return 16;
        }
    };

    public abstract int getIndex();
}
