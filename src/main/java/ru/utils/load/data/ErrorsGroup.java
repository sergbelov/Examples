package ru.utils.load.data;

import java.util.ArrayList;
import java.util.List;

public class ErrorsGroup {
    List<GroupComment> groupCommentList = new ArrayList<>();

    public ErrorsGroup() {
        groupCommentList.add(new GroupComment(
                new String[]{"No resources to process message with messageId:",
                "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize="},
                "No resources to process"));

        groupCommentList.add(new GroupComment(
                new String[]{"org.springframework.transaction.CannotCreateTransactionException: Could not open JDBC Connection for transaction; nested exception is java.sql.SQLTransientConnectionException: master - Connection is not available, request timed out after"},
                "Could not open JDBC Connection for transaction"));
    }

    public int getCount() { return groupCommentList.size();}

    public GroupComment getRegxComment(int num){
        return groupCommentList.get(num);
    }

    public String[] getRegx(int num){
        return groupCommentList.get(num).getConditions();
    }

    public String getComment(int num){
        return groupCommentList.get(num).getComment();
    }
}

class GroupComment {
    String[] conditions;
    String comment;

    public GroupComment(String[] conditions, String comment) {
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
