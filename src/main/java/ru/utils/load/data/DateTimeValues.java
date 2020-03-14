package ru.utils.load.data;

import java.util.*;

/**
 * Метрики на момент времени
 */
public class DateTimeValues {
    private long periodBegin;
    private long periodEnd;
    private Map<String, Number> values = new LinkedHashMap<>();

    public DateTimeValues(long time, Map<String, Number> values) {
        this.periodBegin = time;
        this.periodEnd = time;
        this.values = values;
    }

    public DateTimeValues(long periodBegin, long periodEnd, Map<String, Number> values) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.values = values;
    }

    public DateTimeValues(long periodEnd, Number value) {
        this.periodEnd = periodEnd;
        this.values.put("key", value);
    }

    public DateTimeValues(long periodBegin, long periodEnd, int value) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.values.put("key", value);
    }

    public void setValue(String key, Number value){
        values.put(key, value);
    }

    public void setValues(Map<String, Number> values){
        this.values = values;
    }

    public int size() {
        return values.size();
    }

    public long getTime() {
        return periodEnd;
    }

    public long getPeriodBegin() {
        return periodBegin;
    }

    public long getPeriodEnd() {
        return periodEnd;
    }

    public <T> T getValue(String key){
        return (T) values.get(key);
    }

    public Map<String, Number> getValues() {
        return values;
    }

    public <T> T getValueSum(String[] keys){
        Double valueSum = 0.00;
        for (String key : keys) {
            valueSum = valueSum + (Double) getValue(key);
        }
        return (T) valueSum;
    }

    public boolean compare(String key1, String key2){
        return getValue(key1) == getValue(key2);
    }
}
