package ru.utils.load.data.metrics;

import ru.utils.load.data.graph.VarInList;

/**
 * Номер, наименование, цвет
 * отображаемой метрики из списка метрик (metricsList)
 */
public class MetricView {
    private int numInList;
    private String title;
    private String color;

    public MetricView(VarInList varInList, String title, String color) {
        this.numInList = varInList.getIndex();
        this.title = title;
        this.color = color;
    }

    public MetricView(int numInList, String title, String color) {
        this.numInList = numInList;
        this.title = title;
        this.color = color;
    }

    public int getNumInList() {
        return numInList;
    }

    public String getTitle() {
        return title;
    }

    public String getColor() {
        return color;
    }
}
