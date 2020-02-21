package ru.utils.load.data;

/**
 * Информация по вызовам API
 */
public class Call implements Comparable<Call> {
    //    String rqUid;    // идентификатор
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

    @Override
    public int compareTo(Call o) {
        if (this == o) return 0;
        int compare = (int) (this.getStartTime() - o.getStartTime());
        if (compare != 0) return compare;
        return (int) (this.getStopTime() - o.getStopTime());
    }
}
