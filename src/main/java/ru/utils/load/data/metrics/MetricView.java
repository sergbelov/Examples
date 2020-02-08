package ru.utils.load.data.metrics;

/**
 * Номер, наименование, цвет
 * отображаемой метрики из списка метрик (metricsList)
 */
public class MetricView {
    int numInList;
    String title;
    String color;

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
