package ru.examples.htmlParserExample.data.awr;

import java.util.List;

public class AwrTable {
    private String name;
    private List<String> headers;
    private List<AwrTableRow> rows;

    public AwrTable() {
    }

    /**
     *
      * @param name
     * @param headers
     * @param rows
     */
    public AwrTable(
            String name,
            List<String> headers,
            List<AwrTableRow> rows) {
        this.name = name;
        this.headers = headers;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<AwrTableRow> getRows() {
        return rows;
    }
}
