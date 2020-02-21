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

    double durMin;
    double durAvg;
    double dur90;
    double durMax;

    double tps;
    double tpsRs;

    int countCall;
    int countCallRs;

    public CallMetrics(
            double durMin,
            double durAvg,
            double dur90,
            double durMax,
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

    public double getDurMin() {
        return durMin;
    }

    public double getDurAvg() {
        return durAvg;
    }

    public double getDur90() {
        return dur90;
    }

    public double getDurMax() {
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
