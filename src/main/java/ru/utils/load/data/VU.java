package ru.utils.load.data;

/**
 * Информация по потоку VU
 */
public class VU {
    final int num;   // номер потока
    boolean active;  // разрешена активность
    boolean stopped; // отсановлен

    public VU(int num) {
        this.num = num;
        this.active = true;
        this.stopped = false;
    }

    public int getNum() {
        return num;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stopped() {
        this.stopped = true;
    }

    public void activate(){
        this.active = true;
        this.stopped = false;
    }


}
