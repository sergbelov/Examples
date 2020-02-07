package ru.utils.load.data.errors;

import java.util.ArrayList;
import java.util.List;

public class ErrorsGroup {
    List<ErrorsGroupComment> groupCommentList = new ArrayList<>();

    public ErrorsGroup() {
        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"No resources to process message with messageId:",
                "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize="},
                "No resources to process"));

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"org.springframework.transaction.CannotCreateTransactionException: Could not open JDBC Connection for transaction; nested exception is java.sql.SQLTransientConnectionException: master - Connection is not available, request timed out after"},
                "Could not open JDBC Connection for transaction"));

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"No api [ru.sber.bpm.core.v1.api.BPMSCoreService] services available for route: [*]-[*]-[*] (zone-node-module).."},
                "No api [ru.sber.bpm.core.v1.api.BPMSCoreService] services available"));

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"processId = ", "(in bpmn model) java.lang.NullPointerException"},
                "processId = xxx (in bpmn model) java.lang.NullPointerException"));
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

