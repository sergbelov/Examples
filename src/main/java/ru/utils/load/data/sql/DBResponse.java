package ru.utils.load.data.sql;

import java.util.List;

/**
 * Результат GROUP запроса из БД
 */
public class DBResponse {
    String sqlSelect;        // sql select
    List<DBData> dbDataList; // список полученных значений

    public DBResponse(String sqlSelect, List<DBData> dbDataList) {
        this.sqlSelect = sqlSelect;
        this.dbDataList = dbDataList;
    }

    public String getSqlSelect() {
        return sqlSelect;
    }

    public List<DBData> getDbDataList() {
        return dbDataList;
    }


    public double getValue() {
        return getValue(0);
    }
    public double getValue(String name) {
        return getValue(getNumberByName(name));
    }
    public double getValue(int num) {
        if (num == -1){ return 0;}
        return dbDataList.get(num).getValue().doubleValue();
    }


    public int getIntValue() {
        return getIntValue(0);
    }
    public int getIntValue(String name) {
        return getIntValue(getNumberByName(name));
    }
    public int getIntValue(int num) {
        if (num == -1){ return 0;}
        return dbDataList.get(num).getValue().intValue();
    }


    public long getLongValue() {
        return getLongValue(0);
    }
    public long getLongValue(String name) {
        return getLongValue(getNumberByName(name));
    }
    public long getLongValue(int num) {
        if (num == -1){ return 0;}
        return dbDataList.get(num).getValue().longValue();
    }


    public float getFloatValue() {
        return getFloatValue(0);
    }
    public float getFloatValue(String name) {
        return getFloatValue(getNumberByName(name));
    }
    public float getFloatValue(int num) {
        if (num == -1){ return 0;}
        return dbDataList.get(num).getValue().floatValue();
    }


    public double getDoubleValue() {
        return getDoubleValue(0);
    }
    public double getDoubleValue(String name) {
        return getDoubleValue(getNumberByName(name));
    }
    public double getDoubleValue(int num) {
        if (num == -1){ return 0;}
        return dbDataList.get(num).getValue().doubleValue();
    }

    /**
     * Номер метрики в списке по наименованию
     * @param name
     * @return
     */
    private int getNumberByName(String name){
        int res = -1;
        for (int i = 0; i < dbDataList.size(); i++){
            if (dbDataList.get(i).getName().equalsIgnoreCase(name)){
                res = i;
                break;
            }
        }
        return res;
    }
}
