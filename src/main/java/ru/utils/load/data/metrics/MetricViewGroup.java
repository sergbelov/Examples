package ru.utils.load.data.metrics;

import java.util.List;

/**
 * Группа метрик для отображения на одном графике
 */
public class MetricViewGroup {
    String title;
    List<MetricView> metricViewList;

    public MetricViewGroup(String title, List<MetricView> metricViewList) {
        this.title = title;
        this.metricViewList = metricViewList;
    }

    public String getTitle() {
        return title;
    }

    public List<MetricView> getMetricViewList() {
        return metricViewList;
    }

    public MetricView getMetricView(int num){
        return metricViewList.get(num);
    }

    public int getMetricsCount(){
        return metricViewList.size();
    }
}
