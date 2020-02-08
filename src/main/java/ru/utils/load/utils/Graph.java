package ru.utils.load.utils;

import ru.utils.load.data.DateTimeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.metrics.MetricView;
import ru.utils.load.data.metrics.MetricViewGroup;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private static final Logger LOG = LogManager.getLogger(Graph.class);

    private final DecimalFormat decimalFormat = new DecimalFormat("###.##");

    private final DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    public Graph() {
    }


    /**
     * Линейный график (несколько показателей)
     * @param multiRunService
     * @param metricViewGroup
     * @param metricsList
     * @param step
     * @param printMetrics
     * @return
     */
    public String getSvgGraphLine(
            MultiRunService multiRunService,
            String title,
            List<DateTimeValue> metricsList,
            boolean step,
            boolean printMetrics) {

        // !!! первую запись в metricsList игнорируем

        MetricViewGroup metricViewGroup = multiRunService.getMultiRun().getMetricViewGroup(title);
        if (metricViewGroup == null){
            LOG.error("Не найден MetricViewGroup для {}", title);
        }

        LOG.info("Формирование графика {}", metricViewGroup.getTitle());

        long startTime = multiRunService.getTestStartTime();

        int xSize = Math.max(1200, metricsList.size()-1);
        int ySize = 600;
        int xStart = xSize / 30;
        int yStart = xSize / 20;
        int xMax = xSize + xStart;
        int yMax = ySize + yStart;
        int xMarginRight = xSize / 100;
        int yMarginBottom = xSize / 10;
        int xText = xSize / 200;
        int yText = xSize / 350;
        int fontSize = xSize / 110;
        int fontAxisSize = xSize / 100;
        int lineSize = Math.max(1, xSize / 1000);
        String background = "#dfdfdf";

        // максимальное/минимальное значение Y и X
        long xValueMax = 0L;
        double yValueMin = 999999999999999999.99;
        double yValueMax = 0.00;

        for (int i = 1; i < metricsList.size(); i++) {
            for (int e = 0; e < metricsList.get(i).getValueSize(); e++) {
                int numberViewMetric = getNumberViewMetric(e, metricViewGroup.getMetricViewList());
                if (numberViewMetric > -1){
                    yValueMin = Math.min(yValueMin, metricsList.get(i).getValue(e));
                    yValueMax = Math.max(yValueMax, metricsList.get(i).getValue(e));
                }
            }
            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
        xValueMax = xValueMax - startTime;

        StringBuilder sbResult = new StringBuilder("<!--" + metricViewGroup.getTitle() + "-->\n" +
                "\t\t\t<svg viewBox=\"0 0 " + (xMax + xMarginRight) + " " + (yMax + yMarginBottom) + "\" class=\"chart\">\n" +
                "\t\t\t\t<text " +
                "font-size=\"" + (fontSize * 2) + "\" " +
                "x=\"" + (xSize / 2 - (metricViewGroup.getTitle().length() * xText) / 2) + "\" " +
                "y=\"" + (yStart - fontSize * 2) + "\">" +
                "" + metricViewGroup.getTitle() + "</text>\n" +
                "<!-- Область графика -->\n" +
                "\t\t\t\t<rect " +
                "stroke=\"#0f0f0f\" " +
                "fill=\"" + background + "\" " +
                "x=\"" + xStart + "\" " +
                "y=\"" + yStart + "\" " +
                "width=\"" + xSize + "\" " +
                "height=\"" + ySize + "\"/>\n" +
                "<!-- Описание -->\n");

        // описание графиков
        double yCur = fontSize / 2 + 2;
        for (int i = 0; i < metricViewGroup.getMetricCount(); i++) {
            if (!metricViewGroup.getMetricView(i).getTitle().isEmpty()) {
                sbResult.append(
                    "\t\t\t\t<polyline fill=\"none\" stroke=\"" + metricViewGroup.getMetricView(i).getColor() + "\" stroke-width=\"4\" points=\"" + xStart + "," + yCur + " " + xStart * 3 + "," + yCur + "\"/>\n" +
                    "\t\t\t\t<text font-size=\"10\" font-weight=\"bold\" x=\"" + ((xStart * 3) + 10) + "\" y=\"" + yCur + "\">" + metricViewGroup.getMetricView(i).getTitle() + "</text>\n");
                yCur = yCur + fontSize;
            }
        }

        // ось Y
        sbResult.append("<!-- Ось Y -->\n");
        if (yValueMax > 1) {
            yValueMax = (int) (Math.ceil(yValueMax / 5.00) * 5); // максимальное значение на графике - ближайшее большее кратное 5
        }
        int kfY = 40;
        double yScale = Math.max(Math.min(kfY, yValueMax), 10);
        if (yValueMax > 1) {
            while (yValueMax % yScale != 0) {
                yScale--;
            }
        }
        double yRatio = ySize / (yValueMax * 1.00);
        double yRatioValue = yValueMax / (yScale * 1.00);
        double yStep = ySize / (yScale * 1.00);
        double yValue = 0.00;
        yCur = yMax;
//        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}; yCur:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep, yCur);

        while (yCur > yStart) {
            yCur = yCur - yStep;
            yValue = yValue + yRatioValue;
            sbResult.append("\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#a0a0a0\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n")
                    .append("\t\t\t\t<text " +
                            "font-size=\"" + fontAxisSize + "\" " +
                            "x=\"0\" " +
                            "y=\"" + (yCur + yText) + "\">" +
                            decimalFormat.format(yValue) + "</text>\n");
        }


        // ось X
        sbResult.append("<!-- Ось X -->\n");
        xValueMax = (long) (Math.ceil(xValueMax / 5000.00) * 5000); // максимальное значение на графике - ближайшее большее кратное 5 сек
        int kfX = 60;
        double xScale = Math.min(kfX, xValueMax);
        while (xValueMax % xScale != 0) {
            xScale--;
        }
        xScale = Math.min(xScale, metricsList.size()-1);
        double xRatio = xSize / (xValueMax * 1.00);
        double xRatioValue = xValueMax / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = startTime;
//        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, xStart, xScale, xRatio, xRatioValue, xStep);

        while ((int) xCur <= xMax) {
//            LOG.info("xMax: {}, xCur: {}", xMax, xCur);
            if (xCur > xStart) {
                sbResult.append("\t\t\t\t<polyline " +
                        "fill=\"none\" " +
                        "stroke=\"#a0a0a0\" " +
                        "stroke-dasharray=\"" + xText + "\" " +
                        "stroke-width=\"" + lineSize + "\" " +
                        "points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n");
            }
            sbResult.append("\t\t\t\t<text " +
                    "font-size=\"" + fontAxisSize + "\" " +
                    "letter-spacing=\"0.5\" " +
                    "writing-mode=\"tb\" " +
                    "x=\"" + xCur + "\" " +
                    "y=\"" + (yMax + yText) + "\">" +
                    datetimeFormat.format(xValue) + "</text>\n");

            xCur = xCur + xStep;
            xValue = xValue + (long) xRatioValue;
        }

        // рисуем график
        xCur = xStart;
        StringBuilder sbSignature = new StringBuilder("<!-- Метрики на графике -->\n"); // значения метрик на графике
        StringBuilder sbSignatureTitle = new StringBuilder("<!-- Всплывающие надписи -->\n"); // значения метрик на графике

        StringBuilder[] sbGraph = new StringBuilder[metricViewGroup.getMetricCount()]; // графики
        for (int e = 0; e < metricsList.get(0).getValueSize(); e++) {
            int numberViewMetric = getNumberViewMetric(e, metricViewGroup.getMetricViewList());
            if (numberViewMetric > -1){
                String curColor = metricViewGroup.getMetricView(numberViewMetric).getColor();
                sbGraph[numberViewMetric] = new StringBuilder();
                sbGraph[numberViewMetric].append("<!-- График" + (numberViewMetric + 1) + " -->\n" +
                        "\t\t\t\t<polyline " +
                        "fill=\"none\" " +
                        "stroke=\"" + curColor + "\" " +
                        "stroke-width=\"" + (lineSize * 2) + "\" " +
                        "points=\"" + xCur + "," + yMax + " \n");
            }
        }

        for (int i = 1; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - startTime) * xRatio + xStart;
            // ступеньки
            if (step && i > 0) {
                for (int e = 0; e < metricsList.get(0).getValueSize(); e++) {
                    int numberViewMetric = getNumberViewMetric(e, metricViewGroup.getMetricViewList());
                    if (numberViewMetric > -1) {
                        sbGraph[numberViewMetric].append(xCur + "," + (yMax - Math.round(metricsList.get(i - 1).getValue(e) * yRatio)) + " \n");
                    }
                }
            }

            List<Double> yPrevList = new ArrayList<>();
            for (int e = 0; e < metricsList.get(0).getValueSize(); e++) {
                int numberViewMetric = getNumberViewMetric(e, metricViewGroup.getMetricViewList());
                if (numberViewMetric > -1) {
                    String curColor = metricViewGroup.getMetricView(numberViewMetric).getColor();
                    double y = yMax - Math.round(metricsList.get(i).getValue(e) * yRatio);
                    // график
                    sbGraph[numberViewMetric].append(xCur + "," + y + " \n");

                    // значение отличается от предыдущего
                    if (i == 1 || metricsList.get(i - 1).getValue(e) != metricsList.get(i).getValue(e)) {
                        // значение метрики
                        if (printMetrics) {
                            // надписи не пересекаются
                            boolean print = true;
                            for (int p = 0; p < yPrevList.size(); p++) {
                                if (Math.abs(y - yPrevList.get(p)) < yText * 4) {
                                    print = false;
                                    break;
                                }
                            }
                            if (print) {
                                sbSignature.append("\t\t\t\t<text " +
                                        "font-size=\"" + fontSize + "\" " +
                                        "fill=\"#000000\" " +
//                                    "font-weight=\"bold\" " +
                                        "x=\"" + (xCur - xText) + "\" " +
                                        "y=\"" + (y - yText) + "\">" +
                                        decimalFormat.format(metricsList.get(i).getValue(e)) + "</text>\n");
                                yPrevList.add(y);
                            }
                        }
                    }
                    // точка с всплывающим описанием
                    sbSignatureTitle.append("<g> " +
                            "<circle stroke=\"" + curColor + "\" cx=\"" + xCur + "\" cy=\"" + y + "\" r=\"" + (lineSize * 2) + "\"/> " +
                            "<title>Время: " +
                            sdf1.format(metricsList.get(i).getTime()) + "; VU: " +
                            multiRunService.getVuCount(metricsList.get(i).getTime()) + "; Значение: " +
                            decimalFormat.format(metricsList.get(i).getValue(e)) + "</title> " +
                            "</g>\n");
                }
            }
        }
        for (int i = 0; i < metricViewGroup.getMetricCount(); i++) {
            sbGraph[i].append("\"/>\n");
            sbResult.append(sbGraph[i].toString());
        }
        sbResult.append(sbSignature.toString());
        sbResult.append(sbSignatureTitle.toString());

        sbResult.append("\t\t\t</svg>\n");
        return sbResult.toString();
    }

    /**
     * Нужно отображать текущую метрику ?
     * @param num
     * @return
     */
    private int getNumberViewMetric(int num, List<MetricView> metricViewList){
        int res = -1;
        for (int i = 0; i < metricViewList.size(); i++){
            if (metricViewList.get(i).getNumInList() == num) {
                res = i;
                break;
            }
        }
        return res;
    }
}
