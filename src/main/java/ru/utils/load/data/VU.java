package ru.utils.load.data;

/**
 * Информация по потоку VU
 */
public class VU {
    final int num;        // номер потока
    final long startTime; // время старта
    boolean active;       // активен или нет
    long lastCallTime;    // время последнего вызова


    public VU(int num){
        this.num = num;
        this.active = true;
        this.startTime = System.currentTimeMillis();
    }

    public int getNum() {
        return num;
    }

    public boolean isActive() {
        return active;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(long lastCallTime) {
        this.lastCallTime = lastCallTime;
    }
}
