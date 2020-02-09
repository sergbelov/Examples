package ru.utils.load.data.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.metrics.MetricView;
import ru.utils.load.data.metrics.MetricViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GraphProperty {
    private static final Logger LOG = LogManager.getLogger(GraphProperty.class);
    private List<MetricViewGroup> metricViewGroupList = new ArrayList<>();

    public GraphProperty() {
        // === Графики

        // 0 - VU
        metricViewGroupList.add(new MetricViewGroup("Running Vusers",
                Arrays.asList(new MetricView(0, "", "#0000ff"))));

        // 1 - Длительность выполнения
        metricViewGroupList.add(new MetricViewGroup("Длительность выполнения",
                Arrays.asList(
                        new MetricView(VarInList.DurMin.getNum(), "минимальная длительность (мс)", "#00009f"),
                        new MetricView(VarInList.DurAvg.getNum(), "средняя длительность (мс)", "#00af00"),
                        new MetricView(VarInList.Dur90.getNum(), "перцентиль 90% (мс)", "#a0a000"),
                        new MetricView(VarInList.DurMax.getNum(), "максимальная длительность (мс)", "#ff0000"))));
        // 2 - TPC
        metricViewGroupList.add(new MetricViewGroup("Количество операций в секунду (TPC)",
                Arrays.asList(
                        new MetricView(VarInList.Tpc.getNum(), "TPC - отправлено", "#00009f"),
                        new MetricView(VarInList.TpcRs.getNum(), "TPC - выполнено", "#00af00"))));
        // 3 - Статистика из БД БПМ
        metricViewGroupList.add(new MetricViewGroup("Статистика из БД БПМ",
                Arrays.asList(
                        new MetricView(VarInList.CountCall.getNum(), "отправлено запросов", "#00009f"),
                        new MetricView(VarInList.DbComplete.getNum(), "COMPLETE", "#00af00"),
                        new MetricView(VarInList.DbRunning.getNum(), "RUNNING", "#a0a000"))));
        // 4 - Ошибки
        metricViewGroupList.add(new MetricViewGroup("Ошибки",
                Arrays.asList(new MetricView(VarInList.Errors.getNum(), "", "#ff0000"))));

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
