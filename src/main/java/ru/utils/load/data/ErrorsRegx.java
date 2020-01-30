package ru.utils.load.data;

import java.util.ArrayList;
import java.util.List;

public class ErrorsRegx {
    List<RegxComment> regxCommentList = new ArrayList<>();

    public ErrorsRegx() {
        regxCommentList.add(new RegxComment(
                new String[]{"No resources to process message with messageId:",
                "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize="},
                "No resources to process"
        ));
    }

    public int getCount() { return regxCommentList.size();}

    public RegxComment getRegxComment(int num){
        return regxCommentList.get(num);
    }

    public String[] getRegx(int num){
        return regxCommentList.get(num).getConditions();
    }

    public String getComment(int num){
        return regxCommentList.get(num).getComment();
    }
}

class RegxComment {
    String[] conditions;
    String comment;

    public RegxComment(String[] conditions, String comment) {
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
