package ru.utils.load.data.graph;

import ru.utils.load.data.Metric;

/**
 * Номер, наименование, цвет
 * отображаемой метрики из списка метрик (metricsList)
 */
public class GraphMetric {
    private Metric metric;
    private String title;
    private String color;

    public GraphMetric(Metric metric, String title, String color) {
        this.metric = metric;
        this.title = title;
        this.color = color;
    }

    public Metric getMetric() { return metric; }

    public String getTitle() {
        return title;
    }

    public String getColor() {
        return color;
    }
}
