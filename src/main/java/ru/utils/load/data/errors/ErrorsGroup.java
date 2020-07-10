package ru.utils.load.data.errors;

import java.util.ArrayList;
import java.util.List;

public class ErrorsGroup {
    List<ErrorsGroupComment> groupCommentList = new ArrayList<>();

    public ErrorsGroup() {

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"org.springframework.transaction.CannotCreateTransactionException: Could not open JDBC Connection for transaction; nested exception is java.sql.SQLTransientConnectionException: master - Connection is not available, request timed out after"},
                "Could not open JDBC Connection for transaction"));

    }

    public int getCount() { return groupCommentList.size();}

    public ErrorsGroupComment getConditionsComment(int num){
        return groupCommentList.get(num);
    }

    public String[] getConditions(int num){
        return groupCommentList.get(num).getConditions();
    }

    public String getComment(int num){
        return groupCommentList.get(num).getComment();
    }
}

