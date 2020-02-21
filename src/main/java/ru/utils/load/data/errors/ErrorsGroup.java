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

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"### Error getting a new connection. Cause: java.sql.SQLTransientConnectionException: master - Connection is not available, request timed out after",
                        "### Cause: java.sql.SQLTransientConnectionException: master - Connection is not available, request timed out after"},
                "### Error getting a new connection. Cause: java.sql.SQLTransientConnectionException: master - Connection is not available, request timed out"));

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"Send exception for message.id:",
                        "The server disconnected before a response was received."},
                "Send exception for message.id: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx The server disconnected before a response was received."));

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"Send exception for message.id:",
                        "ru.sber.bpm.core.v1.api.BPMSCoreService-6:"},
                "Send exception for message.id: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx Expiring 93 record(s) for ru.sber.bpm.core.v1.api.BPMSCoreService-6: xxxxx ms has passed since batch creation plus linger time"));

        groupCommentList.add(new ErrorsGroupComment(
                new String[]{"Request initialization timeout.message.id:",
                        "Request from:",
                        "to: [UNKNOWN] with request.init.timeout:"
                },
                "Request initialization timeout.message.id:  to: [UNKNOWN] with request.init.timeout:"));
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

