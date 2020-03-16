package ru.utils.load.data;

import java.util.*;

/**
 * Метрики на момент времени
 */
public class DateTimeValues implements Comparable<DateTimeValues>{
    private long periodBegin;
    private long periodEnd;
    private Map<Metric, Number> values = new LinkedHashMap<>();

    public DateTimeValues(long time) {
        this.periodBegin = time;
        this.periodEnd = time;
    }

    public DateTimeValues(long periodEnd, Number value) {
        this.periodEnd = periodEnd;
        this.values.put(Metric.KEY, value);
    }

    public DateTimeValues(long periodBegin, long periodEnd, Number value) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.values.put(Metric.KEY, value);
    }

    public DateTimeValues(long time, Metric metric, Number value) {
        this.periodBegin = time;
        this.periodEnd = time;
        values.put(metric, value);
    }

    public DateTimeValues(long periodBegin, long periodEnd, Metric metric, Number value) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        values.put(metric, value);
    }

    public DateTimeValues(long time, Map<Metric, Number> values) {
        this.periodBegin = time;
        this.periodEnd = time;
        this.values = values;
    }

    public DateTimeValues(long periodBegin, long periodEnd, Map<Metric, Number> values) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.values = values;
    }

    public void setValue(Metric metric, Number value){
        values.put(metric, value);
    }

    public void setValues(Map<Metric, Number> values){
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

    public <T> T getValue(){
        return (T) values.get(Metric.KEY);
    }

    public <T> T getValue(Metric metric){
        return (T) values.get(metric);
    }

    public int getIntValue(){ return values.get(Metric.KEY).intValue();}
    public long getLongValue(){ return values.get(Metric.KEY).longValue();}
    public double getDoubleValue(){ return values.get(Metric.KEY).doubleValue();}

    public int getIntValue(Metric metric){ return values.get(metric).intValue();}
    public long getLongValue(Metric metric){ return values.get(metric).longValue();}
    public double getDoubleValue(Metric metric){ return values.get(metric).doubleValue();}

    public int getIntValue(Metric[] metrics){
        int valueSum = 0;
        for (Metric metric : metrics) {
            Number number = getValue(metric);
            valueSum = valueSum + number.intValue();
        }
        return valueSum;
    }
    public long getLongValue(Metric[] metrics){
        long valueSum = 0;
        for (Metric metric : metrics) {
            Number number = getValue(metric);
            valueSum = valueSum + number.longValue();
        }
        return valueSum;
    }
    public double getDoubleValue(Metric[] metrics){
        double valueSum = 0.00;
        for (Metric metric : metrics) {
            Number number = getValue(metric);
            valueSum = valueSum + number.doubleValue();
        }
        return valueSum;
    }

    public Map<Metric, Number> getValues() {
        return values;
    }

    public boolean compare(Metric metric1, Metric metric2){
        return getDoubleValue(metric1) == getDoubleValue(metric2);
    }
    public boolean compare(Metric metric, Metric[] metricArray){
        return getDoubleValue(metric) == getDoubleValue(metricArray);
    }

    @Override
    public int compareTo(DateTimeValues o) {
        if (this == o) return 0;
        int compare = (int) (this.getPeriodBegin() - o.getPeriodBegin());
        if (compare != 0) return compare;
        return (int) (this.getPeriodEnd() - o.getPeriodEnd());
    }

}
