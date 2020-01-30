package ru.utils.load.data;

public class ErrorRs {
    long time;
    String text;

    public ErrorRs(long time, String text) {
        this.time = time;
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }
}
