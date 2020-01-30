package ru.utils.load.data;

/**
 * Информация по вызовам API
 */
public class Call {
    String rqUid;    // идентификатор
    long timeBegin;  // время вызова API
    long timeEnd;    // длительность выполнения API

    public Call(String rqUid, long timeBegin) {
        this.rqUid = rqUid;
        this.timeBegin = timeBegin;
        this.timeEnd = 0;
    }

    public String getRqUid() {
        return rqUid;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public long getDuration() {
        return timeEnd > 0 ? timeEnd - timeBegin : 0;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }
}
