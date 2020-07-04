package ru.examples.htmlParserExample.data.awr;

import java.util.ArrayList;
import java.util.List;

public class AwrTableRow {
    List<String> row = new ArrayList<>();

    public AwrTableRow() {
    }

    public AwrTableRow(List<String> row) {
        this.row = row;
    }

    public void add(String col){
        row.add(col);
    }

    public List<String> getRow() {
        return row;
    }
}
