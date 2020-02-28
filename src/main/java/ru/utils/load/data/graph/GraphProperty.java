package ru.utils.load.data.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.metrics.MetricView;
import ru.utils.load.data.metrics.MetricViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Параметры графиков
 */
public class GraphProperty {
    private static final Logger LOG = LogManager.getLogger(GraphProperty.class);
    private List<MetricViewGroup> metricViewGroupList = new ArrayList<>();

    public GraphProperty() {
        // === Графики

        // 0 - VU (отдельный список)
        metricViewGroupList.add(new MetricViewGroup("Running Vusers",
                Arrays.asList(new MetricView(0, "", "#0000ff"))));

        // 1 - Response time (в списке metricsList)
        metricViewGroupList.add(new MetricViewGroup("Response time",
                Arrays.asList(
                        new MetricView(VarInList.DurMin, "минимальная длительность (мс)", "#00009f"),
                        new MetricView(VarInList.DurAvg, "средняя длительность (мс)", "#9f9f00"),
                        new MetricView(VarInList.Dur90, "90 перцентиль (мс)", "#009f00"),
                        new MetricView(VarInList.DurMax, "максимальная длительность (мс)", "#ff0000"))));
        // 2 - Длительность выполнения (информация из БД)
        metricViewGroupList.add(new MetricViewGroup("Длительность выполнения (информация из БД)",
                Arrays.asList(
                        new MetricView(VarInList.DbDurMin, "минимальная длительность (мс)", "#00009f"),
                        new MetricView(VarInList.DbDurAvg, "средняя длительность (мс)", "#9f9f00"),
                        new MetricView(VarInList.DbDur90, "90 перцентиль (мс)", "#009f00"),
                        new MetricView(VarInList.DbDurMax, "максимальная длительность (мс)", "#ff0000"))));
        // 3 - TPS (в списке metricsList)
        metricViewGroupList.add(new MetricViewGroup("Количество запросов в секунду (tps)",
                Arrays.asList(
                        new MetricView(VarInList.Tps, "tps - отправлено", "#00009f"),
                        new MetricView(VarInList.TpsRs, "tps - response", "#00af00"))));
        // 4 - Статистика из БД БПМ (в списке metricsList)
        metricViewGroupList.add(new MetricViewGroup("Статистика из БД БПМ",
                Arrays.asList(
                        new MetricView(VarInList.CountCall, "отправлено запросов", "#00009f"),
                        new MetricView(VarInList.DbCompleted, "COMPLETED", "#009f00"),
                        new MetricView(VarInList.DbRunning, "RUNNING", "#ff9f00"),
                        new MetricView(VarInList.DbLost, "потеряно", "#ff0000"))));
        // 5 - Ошибки (в списке metricsList)
        metricViewGroupList.add(new MetricViewGroup("Ошибки",
                Arrays.asList(new MetricView(VarInList.Errors, "", "#ff0000"))));

        // 6 - Количество шагов завершенных в секунду
        metricViewGroupList.add(new MetricViewGroup("Количество шагов завершенных в секунду",
                Arrays.asList(new MetricView(0, "", "#004f00"))));

        // 7 - BpmsJobEntityImpl Count
        metricViewGroupList.add(new MetricViewGroup("BpmsJobEntityImpl Count",
                Arrays.asList(new MetricView(0, "", "#902000"))));

//        String[] colors = {"#00009f", "#00af00", "#afaf00", "#ff0000", "#00afaf", "#af00af"};
    }

    public List<MetricViewGroup> getMetricViewGroupList() {
        return  metricViewGroupList;
    }

    public MetricViewGroup getMetricViewGroup(String title) {
        for (int i = 0; i < metricViewGroupList.size(); i++){
            if (metricViewGroupList.get(i).getTitle().equalsIgnoreCase(title)){
                return getMetricViewGroup(i);
            }
        }
        return null;
    }

    public MetricViewGroup getMetricViewGroup(int num) {
        return  metricViewGroupList.get(num);
    }

}
