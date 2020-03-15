package ru.utils.load.data.sql;

import ru.utils.load.data.Metric;

/**
 * Значение поля из sql select
 */
public class DBMetric {
    private String name;
    private Number value;

    public DBMetric(Metric metric, Number value) {
        this.name = metric.name();
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
