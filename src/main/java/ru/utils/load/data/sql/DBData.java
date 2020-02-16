package ru.utils.load.data.sql;

public class DBData {
    private long startTime;
    private long stopTime;
    private String processState;

    public DBData(long startTime, long stopTime, String processState) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.processState = processState;
    }

    public DBData(long startTime, String processState) {
        this.startTime = startTime;
        this.stopTime = 0;
        this.processState = processState;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public String getProcessState() {
        return processState;
    }

    public Long getDuration(){
        return stopTime > 0 ? stopTime - startTime : null;
    }
}
