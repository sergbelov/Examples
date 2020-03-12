package ru.utils.load.data.sql;

import ru.utils.load.data.graph.VarInList;

import java.util.List;

/**
 * Результат запроса из БД
 */
public class DBResponse {
    private String[] sqlSelect; // sql select
    private List<DBMetric> dbMetricList; // список полученных значений

    public DBResponse(String[] sqlSelect, List<DBMetric> dbMetricList) {
        this.sqlSelect = sqlSelect;
        this.dbMetricList = dbMetricList;
    }

    public String[] getSqlSelect() {
        return sqlSelect;
    }
    public String getSqlSelect(int num) {
        return sqlSelect[num];
    }

    public List<DBMetric> getDbMetricList() {
        return dbMetricList;
    }


    public double getValue() {
        return getValue(0);
    }
    public double getValue(VarInList varInList) { return getValue(varInList.name()); }
    public double getValue(String name) { return getValue(getNumberByName(name)); }
    public double getValue(int num) {
        if (num == -1){ return 0;}
        return dbMetricList.get(num).getValue().doubleValue();
    }


    public int getIntValue() { return getIntValue(0); }
    public int getIntValue(VarInList varInList) { return getIntValue(varInList.name()); }
    public int getIntValue(VarInList[] varInListArray) {
        int res = 0;
        for (VarInList varInList: varInListArray) {
            res = res + getIntValue(varInList.name());
        }
        return res;
    }
    public int getIntValue(String name) { return getIntValue(getNumberByName(name)); }
    public int getIntValue(int num) {
        if (num == -1){ return 0;}
        return dbMetricList.get(num).getValue().intValue();
    }


    public long getLongValue() {
        return getLongValue(0);
    }
    public long getLongValue(VarInList varInList) { return getLongValue(varInList.name()); }
    public long getLongValue(VarInList[] varInListArray) {
        long res = 0;
        for (VarInList varInList: varInListArray) {
            res = res + getLongValue(varInList.name());
        }
        return res;
    }
    public long getLongValue(String name) { return getLongValue(getNumberByName(name)); }
    public long getLongValue(int num) {
        if (num == -1){ return 0;}
        return dbMetricList.get(num).getValue().longValue();
    }


    public float getFloatValue() {
        return getFloatValue(0);
    }
    public float getFloatValue(VarInList varInList) { return getFloatValue(varInList.name()); }
    public float getFloatValue(VarInList[] varInListArray) {
        float res = 0;
        for (VarInList varInList: varInListArray) {
            res = res + getFloatValue(varInList.name());
        }
        return res;
    }
    public float getFloatValue(String name) { return getFloatValue(getNumberByName(name)); }
    public float getFloatValue(int num) {
        if (num == -1){ return 0;}
        return dbMetricList.get(num).getValue().floatValue();
    }


    public double getDoubleValue() {
        return getDoubleValue(0);
    }
    public double getDoubleValue(VarInList varInList) { return getDoubleValue(varInList.name()); }
    public double getDoubleValue(VarInList[] varInListArray) {
        double res = 0;
        for (VarInList varInList: varInListArray) {
            res = res + getIntValue(varInList.name());
        }
        return res;
    }
    public double getDoubleValue(String name) { return getDoubleValue(getNumberByName(name)); }
    public double getDoubleValue(int num) {
        if (num == -1){ return 0;}
        return dbMetricList.get(num).getValue().doubleValue();
    }

    /**
     * Номер метрики в списке по наименованию
     * @param name
     * @return
     */
    private int getNumberByName(String name){
        int res = -1;
        for (int i = 0; i < dbMetricList.size(); i++){
            if (dbMetricList.get(i).getName().equalsIgnoreCase(name)){
                res = i;
                break;
            }
        }
        return res;
    }
}
