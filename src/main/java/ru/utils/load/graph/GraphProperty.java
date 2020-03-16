package ru.utils.load.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.Metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Параметры графиков
 */
public class GraphProperty {
    private static final Logger LOG = LogManager.getLogger(GraphProperty.class);
    private List<GraphMetricGroup> graphMetricGroupList = new ArrayList<>();

    public GraphProperty() {
        // === Графики

        // 0 - VU (отдельный список)
        graphMetricGroupList.add(new GraphMetricGroup("Running Vusers",
                Arrays.asList(new GraphMetric(Metric.KEY, "", "#0000ff"))));

        // 1 - Response time (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Response time",
                Arrays.asList(
                        new GraphMetric(Metric.DUR_MIN, "минимальная длительность (мс)", "#00009f"),
                        new GraphMetric(Metric.DUR_AVG, "средняя длительность (мс)", "#9f9f00"),
                        new GraphMetric(Metric.DUR_90, "90 перцентиль (мс)", "#009f00"),
                        new GraphMetric(Metric.DUR_MAX, "максимальная длительность (мс)", "#ff0000"))));
        // 2 - Длительность выполнения (информация из БД)
        graphMetricGroupList.add(new GraphMetricGroup("Длительность выполнения (информация из БД)",
                Arrays.asList(
                        new GraphMetric(Metric.DB_DUR_MIN, "минимальная длительность (мс)", "#00009f"),
                        new GraphMetric(Metric.DB_DUR_AVG, "средняя длительность (мс)", "#9f9f00"),
                        new GraphMetric(Metric.DB_DUR_90, "90 перцентиль (мс)", "#009f00"),
                        new GraphMetric(Metric.DB_DUR_MAX, "максимальная длительность (мс)", "#ff0000"))));
        // 3 - TPS (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Количество запросов в секунду (tps)",
                Arrays.asList(
                        new GraphMetric(Metric.TPS, "tps - отправлено", "#00009f"),
                        new GraphMetric(Metric.TPS_RS, "tps - response", "#00af00"))));
        // 4 - Статистика из БД БПМ (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Статистика из БД БПМ",
                Arrays.asList(
                        new GraphMetric(Metric.COUNT_CALL, "отправлено запросов", "#00009f"),
                        new GraphMetric(Metric.DB_COMPLETED, "COMPLETED", "#009f00"),
                        new GraphMetric(Metric.DB_RUNNING, "RUNNING", "#ff9f00"),
                        new GraphMetric(Metric.DB_FAILED, "FAILED", "#ff9f00"),
                        new GraphMetric(Metric.DB_LOST, "потеряно", "#ff0000"))));
        // 5 - Ошибки (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Ошибки",
                Arrays.asList(new GraphMetric(Metric.ERRORS, "", "#ff0000"))));

        // 6 - Количество шагов завершенных в секунду
        graphMetricGroupList.add(new GraphMetricGroup("Количество шагов завершенных в секунду",
                Arrays.asList(new GraphMetric(Metric.TPS, "", "#004f00"))));

        // 7 - BpmsJobEntityImpl Count (отдельный список)
        graphMetricGroupList.add(new GraphMetricGroup("BpmsJobEntityImpl Count",
                Arrays.asList(new GraphMetric(Metric.KEY, "", "#902000"))));

        // 8 - RetryPolicyJobEntityImpl Count (отдельный список)
        graphMetricGroupList.add(new GraphMetricGroup("RetryPolicyJobEntityImpl Count",
                Arrays.asList(new GraphMetric(Metric.KEY, "", "#00009f"))));

//        String[] colors = {"#00009f", "#00af00", "#afaf00", "#ff0000", "#00afaf", "#af00af"};
    }

    public List<GraphMetricGroup> getGraphMetricGroupList() {
        return graphMetricGroupList;
    }

    public GraphMetricGroup getMetricViewGroup(String title) {
        for (int i = 0; i < graphMetricGroupList.size(); i++){
            if (graphMetricGroupList.get(i).getTitle().equalsIgnoreCase(title)){
                return getMetricViewGroup(i);
            }
        }
        return null;
    }

    public GraphMetricGroup getMetricViewGroup(int num) {
        return graphMetricGroupList.get(num);
    }

}
