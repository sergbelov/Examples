package ru.utils.load.data.metrics;

public class CallMetrics {
    /*
        0  - durMin
        1  - durAvg
        2  - dur90
        3  - durMax
        4  - tps
        5  - tpsRs
        6  - countCall
        7  - countCallRs
     */

    long durMin;
    long durAvg;
    long dur90;
    long durMax;

    double tps;
    double tpsRs;

    int countCall;
    int countCallRs;

    public CallMetrics(
            long durMin,
            long durAvg,
            long dur90,
            long durMax,
            double tps,
            double tpsRs,
            int countCall,
            int countCallRs){

        this.durMin = durMin;
        this.durAvg = durAvg;
        this.dur90 = dur90;
        this.durMax = durMax;
        this.tps = tps;
        this.tpsRs = tpsRs;
        this.countCall = countCall;
        this.countCallRs = countCallRs;
    }

    public long getDurMin() {
        return durMin;
    }

    public long getDurAvg() {
        return durAvg;
    }

    public long getDur90() {
        return dur90;
    }

    public long getDurMax() {
        return durMax;
    }

    public double getTps() {
        return tps;
    }

    public double getTpsRs() { return tpsRs; }

    public int getCountCall() {
        return countCall;
    }

    public int getCountCallRs() {
        return countCallRs;
    }

}
