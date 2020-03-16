package ru.utils.load.graph;

import java.util.List;

/**
 * Группа метрик для отображения на одном графике
 */
public class GraphMetricGroup {
    String title;
    List<GraphMetric> graphMetricList;

    public GraphMetricGroup(String title, List<GraphMetric> graphMetricList) {
        this.title = title;
        this.graphMetricList = graphMetricList;
    }

    public String getTitle() {
        return title;
    }

    public List<GraphMetric> getGraphMetricList() {
        return graphMetricList;
    }

    public GraphMetric getMetricView(int num){
        return graphMetricList.get(num);
    }

    public int getMetricsCount(){
        return graphMetricList.size();
    }
}
