package ru.utils.load.utils;

import ru.utils.load.data.DateTimeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.graph.VarInList;
import ru.utils.load.data.metrics.MetricView;
import ru.utils.load.data.metrics.MetricViewGroup;

import java.text.*;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private static final Logger LOG = LogManager.getLogger(Graph.class);
    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat sdf4 = new SimpleDateFormat("HH:mm:ss.SSS");
    private final DateFormat sdf5 = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat sdf6 = new SimpleDateFormat("yyyyMMdd");

    public Graph() {
    }


    /**
     * Линейный график (несколько показателей)
     * !!! нулевой элемент в metricsList игнорируем (он содержит информацию за весь период)
     *
     * @param title
     * @param multiRunService
     * @param metricsList
     * @return
     */
    public String getSvgGraphLine(
            String title,
            MultiRunService multiRunService,
            List<DateTimeValue> metricsList) {
        return getSvgGraphLine(
                title,
                multiRunService,
                metricsList,
                true,
                false,
                false);
    }
    /**
     * Линейный график (несколько показателей)
     * !!! нулевой элемент в metricsList игнорируем (он содержит информацию за весь период)
     *
     * @param multiRunService
     * @param title
     * @param metricsList
     * @param step
     * @param printMetrics
     * @return
     */
    public String getSvgGraphLine(
            String title,
            MultiRunService multiRunService,
            List<DateTimeValue> metricsList,
            boolean yStartFrom0,
            boolean step,
            boolean printMetrics) {

/*
        if (title.equals("Ошибки")){
            LOG.info("Отлаживаем...");
            for (int i = 0; i < metricsList.size(); i++){
                LOG.info("{}: {} {}", multiRunService.getName(), sdf1.format(metricsList.get(i).getTime()), metricsList.get(i).getValue(VarInList.Errors));
            }
        }
*/

        MetricViewGroup metricViewGroup = multiRunService
                .getMultiRun()
                .getGraphProperty()
                .getMetricViewGroup(title);
        if (metricViewGroup == null) {
            LOG.error("Не найден MetricViewGroup для {}", title);
        }

        LOG.info("{}: Формирование графика {}", multiRunService.getName(), metricViewGroup.getTitle());

        long xValueMin = 0L;
        try {
            xValueMin = sdf2.parse(sdf2.format(multiRunService.getTestStartTime())).getTime();
        } catch (ParseException e) {
            LOG.error("Ошибка в формате даты", e);
        }
        long xValueMax = (long) (Math.ceil(multiRunService.getTestStopTime() / 1000.00) * 1000);
        LOG.debug("{}: xValueMin: {} {}, xValueMax: {} {}",
                multiRunService.getName(),
                sdf1.format(multiRunService.getTestStartTime()),
                sdf1.format(xValueMin),
                sdf1.format(multiRunService.getTestStopTime()),
                sdf1.format(xValueMax));

        int xSize = Math.max(10000, metricsList.size() - 1);
        int ySize = (int) (xSize / 2.8);
        int xStart = xSize / 30;
        int yStart = xSize / 20;
        int xMax = xSize + xStart;
        int yMax = ySize + yStart;
        int xMarginRight = xSize / 300;
        int yMarginBottom = xSize / 12;
        int xText = xSize / 500;
        int yText = xSize / 400;
        int fontSize = xSize / 120;
        int fontSizeX = xSize / 156;
        int fontAxisSize = xSize / 110;
        int lineSize = Math.max(1, xSize / 5000);
        String background = "#f0f0f0"; //"#dfdfdf";

        // максимальное/минимальное значение Y и X
        double yValueMin = 99999999999D;
        double yValueMax = 0.00;
        for (int i = 1; i < metricsList.size(); i++) {
            for (MetricView metricView : metricViewGroup.getMetricViewList()) {
//                if (!Double.isNaN(metricsList.get(i).getDoubleValue(metricView.getNumInList()))) {
//                LOG.trace("{}", metricsList.get(i).getDoubleValue(metricView.getNumInList()));
                    yValueMin = Math.min(yValueMin, metricsList.get(i).getDoubleValue(metricView.getNumInList()));
                    yValueMax = Math.max(yValueMax, metricsList.get(i).getDoubleValue(metricView.getNumInList()));
//                }
            }
//            xValueMin = Math.min(xValueMin, metricsList.get(i).getTime());
//            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
//        LOG.info("{}: {}, {}, {}", multiRunService.getName(), title, yValueMin, yValueMax);
        if (yValueMax == 0 || xValueMax == 0){
            return "";
        }

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
        double yCur = fontSize / 1.5;
        for (int i = 0; i < metricViewGroup.getMetricsCount(); i++) {
            if (!metricViewGroup.getMetricView(i).getTitle().isEmpty()) {
                sbResult.append(
                        "\t\t\t\t<polyline fill=\"none\" stroke=\"" + metricViewGroup.getMetricView(i).getColor() + "\" stroke-width=\"" + (lineSize * 4) + "\" points=\"" + xStart + "," + yCur + " " + xStart * 3 + "," + yCur + "\"/>\n" +
                        "\t\t\t\t<text font-size=\"" + fontSize + "\" font-weight=\"bold\" x=\"" + ((xStart * 3) + 10) + "\" y=\"" + yCur + "\">" + metricViewGroup.getMetricView(i).getTitle() + "</text>\n");
                yCur = yCur + fontSize;
            }
        }

        // ось Y
        sbResult.append("<!-- Ось Y -->\n");
        if (yStartFrom0) { yValueMin = 0L; } // начальное значение по оси Y = 0 или минимальному значению из списка
        yValueMin = (int) yValueMin;
        if (yValueMax > 1) {
            yValueMax = (int) (Math.ceil(yValueMax / 1.00) * 1);
        }
        int kfY = 40;
        double yValueRange = yValueMax - yValueMin;
        double yScale = Math.max(Math.min(kfY, yValueRange), 10);
        if (yValueRange > 10) {
            while (true) {
                yScale = Math.max(Math.min(kfY, yValueRange), 10);
                while (yValueRange % yScale != 0) {
                    yScale--;
                }
                if (yScale == yValueRange || yScale > 10){
                    break;
                } else {
                    yValueMax++;
                    yValueRange = yValueMax - yValueMin;
                }
//                LOG.info("{}: {} {}, {}", multiRunService.getName(), yValueMin, yValueMax, yScale);
            }
        }
        double yRatio = ySize / (yValueRange * 1.00);
        double yRatioValue = yValueRange / (yScale * 1.00);
        double yStep = ySize / (yScale * 1.00);
        double yValue = yValueMin;
        yCur = yMax;
//        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}; yCur:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep, yCur);
        while (yValue <= yValueMax) {
            sbResult.append("\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#a0a0a0\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n");
            sbResult.append("\t\t\t\t<text " +
                    "font-size=\"" + fontSize + "\" " +
                    "x=\"0\" " +
                    "y=\"" + (yCur + yText) + "\">" +
                    decimalFormat.format(yValue) + "</text>\n");
            yCur = yCur - yStep;
            yValue = yValue + yRatioValue;
        }

        // ось X
        sbResult.append("<!-- Ось X -->\n");
        long xValueRange = xValueMax - xValueMin;
        double xScale;
        int kfX = 60;
        while (true) {
            xScale = Math.min(kfX, xValueRange);
            while ((xValueRange / xScale) % 1000 != 0) {
                xScale--;
            }
            if (xScale == xValueRange/1000 || xScale > 20){
                break;
            } else {
                xValueMax = xValueMax + 1000;
                xValueRange = xValueMax - xValueMin;
            }
//            LOG.info("{}: {} {}, {}", multiRunService.getName(), xValueMin, xValueMax, xScale);
        }
        double xRatio = xSize / (xValueRange * 1.00);
        double xRatioValue = xValueRange / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = xValueMin;
//        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, xStart, xScale, xRatio, xRatioValue, xStep);
        long xValueMem = 0;
        if (xStep > 0) {
            while (xValue <= xValueMax) {
//            LOG.info("xMax: {}, xCur: {}", xMax, xCur);
                if (xCur > xStart) {
                    sbResult.append("\t\t\t\t<polyline " +
                            "fill=\"none\" " +
                            "stroke=\"#a0a0a0\" " +
                            "stroke-dasharray=\"" + yText + "\" " +
                            "stroke-width=\"" + lineSize + "\" " +
                            "points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n");
                }
                sbResult.append("\t\t\t\t<text " +
                        "font-size=\"");
                if (!sdf6.format(xValueMem).equals(sdf6.format(xValue))) { // шрифт для полной даты
                    sbResult.append(fontSizeX);
                } else {
                    sbResult.append(fontSizeX + fontSizeX / 10);
                }
                sbResult.append("\" " +
                        "font-family=\"Courier New\" " +
                        "letter-spacing=\"0\" " + // 0.5
                        "writing-mode=\"tb\" " +
                        "x=\"" + xCur + "\" " +
                        "y=\"" + (yMax + yText) + "\">");
                if (!sdf6.format(xValueMem).equals(sdf6.format(xValue))) { // полную даты выводим 1 раз
                    sbResult.append(sdf2.format(xValue)).append("</text>\n");
                    xValueMem = xValue;
                } else {
                    sbResult.append(sdf5.format(xValue)).append("</text>\n");
                }
                xCur = xCur + xStep;
                xValue = xValue + (long) xRatioValue;
            }
        }

        // рисуем график
        xCur = xStart;
        StringBuilder sbSignature = new StringBuilder("<!-- Метрики на графике -->\n"); // значения метрик на графике
        StringBuilder sbSignatureTitle = new StringBuilder("<!-- Всплывающие надписи -->\n"); // значения метрик на графике

        StringBuilder[] sbGraph = new StringBuilder[metricViewGroup.getMetricsCount()]; // графики
        for (int m = 0; m < metricViewGroup.getMetricsCount(); m++) { // перебираем метрики для отображения
            String curColor = metricViewGroup.getMetricView(m).getColor();
            sbGraph[m] = new StringBuilder();
            sbGraph[m].append("<!-- График" + (m + 1) + " -->\n" +
                    "\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"" + curColor + "\" " +
                    "stroke-width=\"" + (lineSize * 2) + "\" " +
                    "points=\"\n");
//                    "points=\"" + xCur + "," + yMax + " \n");
        }

        for (int i = 1; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - xValueMin) * xRatio + xStart;
            // ступеньки
            if (step && i > 0) {
                for (int m = 0; m < metricViewGroup.getMetricsCount(); m++) { // перебираем метрики для отображения
                    int numInList = metricViewGroup
                            .getMetricViewList()
                            .get(m)
                            .getNumInList();
                    sbGraph[m].append(xCur + "," + (yMax - Math.round((metricsList.get(i - 1).getValue(numInList) - yValueMin) * yRatio)) + " \n");
                }
            }

            List<Double> yPrevList = new ArrayList<>();
            for (int m = 0; m < metricViewGroup.getMetricsCount(); m++) { // перебираем метрики для отображения
                int numInList = metricViewGroup // номер метрики в общем вписке
                        .getMetricViewList()
                        .get(m)
                        .getNumInList();
                String curColor = metricViewGroup.getMetricView(m).getColor();
                double y = yMax - Math.round((metricsList.get(i).getValue(numInList) - yValueMin) * yRatio);
                // график
                sbGraph[m].append(xCur + "," + y + " \n");
                // значение отличается от предыдущего
                if (i == 1 || metricsList.get(i - 1).getValue(numInList) != metricsList.get(i).getValue(numInList)) {
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
                                    decimalFormat.format(metricsList.get(i).getValue(numInList)) + "</text>\n");
                            yPrevList.add(y);
                        }
                    }
                }
                // точка с всплывающим описанием
                sbSignatureTitle.append("<g> " +
                        "<circle stroke=\"" + curColor + "\" cx=\"" + xCur + "\" cy=\"" + y + "\" r=\"" + (lineSize * 5) + "\"/> " +
                        "<title>");
                if (!metricViewGroup.getMetricView(m).getTitle().isEmpty()) {
                    sbSignatureTitle.append(metricViewGroup.getMetricView(m).getTitle() + "; ");
                }
                sbSignatureTitle.append("время: " + sdf1.format(metricsList.get(i).getTime()) + "; " +
                        "VU: " + multiRunService.getVuCount(metricsList.get(i).getTime()) + "; " +
                        "значение: " + decimalFormat.format(metricsList.get(i).getValue(numInList)) + "</title> " +
                        "</g>\n");
            }
        }
        for (int i = 0; i < metricViewGroup.getMetricsCount(); i++) {
            sbGraph[i].append("\"/>\n");
            sbResult.append(sbGraph[i].toString());
        }
        sbResult.append(sbSignature.toString());
        sbResult.append(sbSignatureTitle.toString());

        sbResult.append("\t\t\t</svg>\n");
        return sbResult.toString();
    }

}
