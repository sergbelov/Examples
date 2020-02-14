package ru.utils.load.data.sql;

/**
 * Значение поля из sql select
 */
public class DBMetric {
    String name;
    Number value;

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
