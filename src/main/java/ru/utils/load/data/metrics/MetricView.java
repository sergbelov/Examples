package ru.utils.load.data.metrics;

import ru.utils.load.data.graph.Metric;

/**
 * Номер, наименование, цвет
 * отображаемой метрики из списка метрик (metricsList)
 */
public class MetricView {
    private Metric metric;
    private String title;
    private String color;

    public MetricView(Metric metric, String title, String color) {
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
