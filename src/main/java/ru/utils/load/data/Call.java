package ru.utils.load.data;

/**
 * Информация по вызовам API
 */
public class Call {
    String rqUid;    // идентификатор
    long startTime;  // время вызова API
    long stopTime;    // время отклика

    public Call(long startTime) {
        this.startTime = startTime;
        this.stopTime = 0;
    }

    public Call(long startTime, long stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public Long getDuration() {
        return stopTime > 0 ? stopTime - startTime : null;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }
}
