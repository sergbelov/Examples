package ru.utils.load.data.sql;

/**
 * Значение поля из sql select
 */
public class DBData {
    String name;
    Number value;

    public DBData(String name, Number value) {
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
