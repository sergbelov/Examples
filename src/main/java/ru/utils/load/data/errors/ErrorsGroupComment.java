package ru.utils.load.data.errors;

public class ErrorsGroupComment {
    String[] conditions;
    String comment;

    public ErrorsGroupComment(String[] conditions, String comment) {
        this.conditions = conditions;
        this.comment = comment;
    }

    public String[] getConditions() {
        return conditions;
    }

    public String getComment() {
        return comment;
    }
}