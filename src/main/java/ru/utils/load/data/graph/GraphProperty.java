package ru.utils.load.data.graph;

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
                Arrays.asList(new GraphMetric(Metric.key, "", "#0000ff"))));

        // 1 - Response time (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Response time",
                Arrays.asList(
                        new GraphMetric(Metric.DurMin, "минимальная длительность (мс)", "#00009f"),
                        new GraphMetric(Metric.DurAvg, "средняя длительность (мс)", "#9f9f00"),
                        new GraphMetric(Metric.Dur90, "90 перцентиль (мс)", "#009f00"),
                        new GraphMetric(Metric.DurMax, "максимальная длительность (мс)", "#ff0000"))));
        // 2 - Длительность выполнения (информация из БД)
        graphMetricGroupList.add(new GraphMetricGroup("Длительность выполнения (информация из БД)",
                Arrays.asList(
                        new GraphMetric(Metric.DbDurMin, "минимальная длительность (мс)", "#00009f"),
                        new GraphMetric(Metric.DbDurAvg, "средняя длительность (мс)", "#9f9f00"),
                        new GraphMetric(Metric.DbDur90, "90 перцентиль (мс)", "#009f00"),
                        new GraphMetric(Metric.DbDurMax, "максимальная длительность (мс)", "#ff0000"))));
        // 3 - TPS (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Количество запросов в секунду (tps)",
                Arrays.asList(
                        new GraphMetric(Metric.Tps, "tps - отправлено", "#00009f"),
                        new GraphMetric(Metric.TpsRs, "tps - response", "#00af00"))));
        // 4 - Статистика из БД БПМ (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Статистика из БД БПМ",
                Arrays.asList(
                        new GraphMetric(Metric.CountCall, "отправлено запросов", "#00009f"),
                        new GraphMetric(Metric.DbCompleted, "COMPLETED", "#009f00"),
                        new GraphMetric(Metric.DbRunning, "RUNNING", "#ff9f00"),
                        new GraphMetric(Metric.DbFailed, "FAILED", "#ff9f00"),
                        new GraphMetric(Metric.DbLost, "потеряно", "#ff0000"))));
        // 5 - Ошибки (в списке metricsList)
        graphMetricGroupList.add(new GraphMetricGroup("Ошибки",
                Arrays.asList(new GraphMetric(Metric.Errors, "", "#ff0000"))));

        // 6 - Количество шагов завершенных в секунду
        graphMetricGroupList.add(new GraphMetricGroup("Количество шагов завершенных в секунду",
                Arrays.asList(new GraphMetric(Metric.Tps, "", "#004f00"))));

        // 7 - BpmsJobEntityImpl Count (отдельный список)
        graphMetricGroupList.add(new GraphMetricGroup("BpmsJobEntityImpl Count",
                Arrays.asList(new GraphMetric(Metric.key, "", "#902000"))));

        // 8 - RetryPolicyJobEntityImpl Count (отдельный список)
        graphMetricGroupList.add(new GraphMetricGroup("RetryPolicyJobEntityImpl Count",
                Arrays.asList(new GraphMetric(Metric.key, "", "#00009f"))));

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
