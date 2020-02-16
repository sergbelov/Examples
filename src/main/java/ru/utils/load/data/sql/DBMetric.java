package ru.utils.load.data.sql;

import ru.utils.load.data.graph.VarInList;

/**
 * Значение поля из sql select
 */
public class DBMetric {
    private String name;
    private Number value;

    public DBMetric(VarInList varInList, Number value) {
        this.name = varInList.name();
        this.value = value;
    }
    public DBMetric(String name, Number value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Number getValue() {
        return value;
    }
}
