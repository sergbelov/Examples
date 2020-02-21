package ru.utils.load.data;

import java.util.List;

public class StatData {
    long valMin;
    double valAvg;
    long val90;
    long valMax;
    List<Integer> countList;
    String resultStr;

    public StatData(
            long valMin,
            double valAvg,
            long val90,
            long valMax,
            List<Integer> countList,
            String resultStr) {
        this.valMin = valMin;
        this.valAvg = valAvg;
        this.val90 = val90;
        this.valMax = valMax;
        this.countList = countList;
        this.resultStr = resultStr;
    }

    public long getValMin() {
        return valMin;
    }

    public double getValAvg() {
        return valAvg;
    }

    public long getVal90() {
        return val90;
    }

    public long getValMax() {
        return valMax;
    }

    public List<Integer> getCountList() {
        return countList;
    }

    public int getCountListSize() {
        return countList.size();
    }

    public int getCountList(int index) {
        return countList.get(index);
    }

    public String getResultStr() {
        return resultStr;
    }
}
