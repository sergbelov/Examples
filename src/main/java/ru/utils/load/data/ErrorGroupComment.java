package ru.utils.load.data;

public class ErrorGroupComment {
    String comment;
    int count;

    public ErrorGroupComment(String comment) {
        this.comment = comment;
    }

    public ErrorGroupComment(String comment, int count) {
        this.comment = comment;
        this.count = count;
    }

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
