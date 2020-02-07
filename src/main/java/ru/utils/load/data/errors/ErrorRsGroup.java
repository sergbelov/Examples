package ru.utils.load.data.errors;

public class ErrorRsGroup {
    String firstError;
    String comment;
    int count;

    public ErrorRsGroup(String firstError, String comment) {
        this.firstError = firstError;
        this.comment = comment;
    }

    public ErrorRsGroup(String firstError, String comment, int count) {
        this.firstError = firstError;
        this.comment = comment;
        this.count = count;
    }

    public String getFirstError() { return firstError;}

    public String getComment() {
        return comment;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incCount(){
        count++;
    }
}
