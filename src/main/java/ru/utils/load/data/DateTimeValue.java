package ru.utils.load.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Метрика на момент времени
 */
public class DateTimeValue {
    long periodBegin;
    long periodEnd;
    List<Number> valueList = new ArrayList<>();

    public DateTimeValue(long time, List<Number> valueList) {
        this.periodBegin = time;
        this.periodEnd = time;
        this.valueList = valueList;
    }

    public DateTimeValue(long periodBegin, long periodEnd, List<Number> valueList) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.valueList = valueList;
    }

    public DateTimeValue(long periodEnd, Number value) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.valueList.add(value);
    }

    public DateTimeValue(long periodBegin, long periodEnd, int value) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.valueList.add(value);
    }

    public DateTimeValue(
            long periodBegin,
            long periodEnd,
            int value1,
            int value2,
            int value3) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        this.valueList.add(value1);
        this.valueList.add(value2);
        this.valueList.add(value3);
    }

    public int getValueSize() {
        return valueList.size();
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

    public double getValue() {
        return getValue(0);
    }

    public double getValue(int num) {
        return valueList.get(num).doubleValue();
    }

    public int getIntValue() {
        return getIntValue(0);
    }

    public int getIntValue(int num) {
        return valueList.get(num).intValue();
    }

    public double getLongValue() {
        return getLongValue(0);
    }

    public double getLongValue(int num) {
        return valueList.get(num).longValue();
    }

    public double getFloatValue() {
        return getFloatValue(0);
    }

    public double getFloatValue(int num) {
        return valueList.get(num).floatValue();
    }

    public double getDoubleValue() {
        return getDoubleValue(0);
    }

    public double getDoubleValue(int num) {
        return valueList.get(num).doubleValue();
    }
}
