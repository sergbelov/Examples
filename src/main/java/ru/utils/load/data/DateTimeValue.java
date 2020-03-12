package ru.utils.load.data;

import ru.utils.load.data.graph.VarInList;

import java.util.ArrayList;
import java.util.List;

/**
 * Метрика на момент времени
 */
public class DateTimeValue {
    private long periodBegin;
    private long periodEnd;
    private List<Number> valueList = new ArrayList<>();

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
//        this.periodBegin = periodBegin;
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

    public double getValue() { return getValue(0); }
    public double getValue(VarInList varInList) { return getValue(varInList.getIndex()); }
    public double getValue(int num) { return valueList.get(num).doubleValue(); }

    public boolean compareMetrics(VarInList varInList1, VarInList varInList2){
        return getIntValue(varInList1) == getIntValue(varInList2);
    }
    public boolean compareMetrics(VarInList varInList1, VarInList[] varInListArray2){
        return getIntValue(varInList1) == getIntValue(varInListArray2);
    }

    public int getIntValue() { return getIntValue(0); }
    public int getIntValue(VarInList varInList) { return getIntValue(varInList.getIndex()); }
    public int getIntValue(VarInList[] varInListArray) {
        int res = 0;
        for (VarInList varInList : varInListArray) {
            res = res + getIntValue(varInList.getIndex());
        }
        return res;
    }
    public int getIntValue(int num) { return valueList.get(num).intValue(); }

    public long getLongValue() { return getLongValue(0); }
    public long getLongValue(VarInList varInList) { return getLongValue(varInList.getIndex()); }
    public long getLongValue(VarInList[] varInListArray) {
        long res = 0;
        for (VarInList varInList : varInListArray) {
            res = res + getLongValue(varInList.getIndex());
        }
        return res;
    }
    public long getLongValue(int num) { return valueList.get(num).longValue(); }

    public float getFloatValue() { return getFloatValue(0); }
    public float getFloatValue(VarInList varInList) { return getFloatValue(varInList.getIndex()); }
    public float getFloatValue(VarInList[] varInListArray) {
        float res = 0;
        for (VarInList varInList : varInListArray) {
            res = res + getFloatValue(varInList.getIndex());
        }
        return res;
    }
    public float getFloatValue(int num) { return valueList.get(num).floatValue(); }

    public double getDoubleValue() { return getDoubleValue(0); }
    public double getDoubleValue(VarInList varInList) { return getDoubleValue(varInList.getIndex()); }
    public double getDoubleValue(VarInList[] varInListArray) {
        double res = 0;
        for (VarInList varInList : varInListArray) {
            res = res + getDoubleValue(varInList.getIndex());
        }
        return res;
    }
    public double getDoubleValue(int num) { return valueList.get(num).doubleValue(); }
}
