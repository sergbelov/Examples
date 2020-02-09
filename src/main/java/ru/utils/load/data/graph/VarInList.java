package ru.utils.load.data.graph;

/*  список метрик:
0  - durMin
1  - durAvg
2  - dur90
3  - durMax
4  - tps
5  - tpsRs
6  - countCall
7  - countCallComplete
8  - dbComplete
9  - dbRunning
10 - errors
*/
public enum VarInList {

    DurMin {
        public int getNum() {
            return 0;
        }
    },
    DurAvg {
        public int getNum() {
            return 1;
        }
    },
    Dur90 {
        public int getNum() {
            return 2;
        }
    },
    DurMax {
        public int getNum() {
            return 3;
        }
    },
    Tps {
        public int getNum() {
            return 4;
        }
    },
    TpsRs {
        public int getNum() {
            return 5;
        }
    },
    CountCall {
        public int getNum() {
            return 6;
        }
    },
    CountCallCompete {
        public int getNum() {
            return 7;
        }
    },
    DbComplete {
        public int getNum() {
            return 8;
        }
    },
    DbRunning {
        public int getNum() {
            return 9;
        }
    },
    Errors {
        public int getNum() {
            return 10;
        }
    };

    public abstract int getNum();
}
