package ru.utils.load.data;

/**
 * Информация по вызовам API
 */
public class Call {
    String rqUid;    // идентификатор
    long timeBegin;  // время вызова API
    long timeEnd;    // время отклика

    public Call(long timeBegin) {
        this.timeBegin = timeBegin;
        this.timeEnd = 0;
    }

    public Call(long timeBegin, long timeEnd) {
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
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
