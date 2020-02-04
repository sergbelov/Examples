package ru.utils.load.utils;

import ru.utils.load.data.DateTimeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     *
     * @param titleGraph
     * @param titleLines
     * @param startTime
     * @param metricsList
     * @param step
     * @param printMetrics
     * @return
     */
    public String getSvgGraphLine(
            String titleGraph,
            String[] titleLines,
            long startTime,
            List<DateTimeValue> metricsList,
            boolean step,
            boolean printMetrics) {
        return getSvgGraphLine(
                titleGraph,
                titleLines,
                startTime,
                metricsList,
                step,
                printMetrics,
                "#00009f");
    }

    /**
     * Линейный график (несколько показателей)
     *
     * @param titleGraph
     * @param titleLines
     * @param startTime
     * @param metricsList
     * @param step
     * @param printMetrics
     * @param color
     * @return
     */
    public String getSvgGraphLine(
            String titleGraph,
            String[] titleLines,
            long startTime,
            List<DateTimeValue> metricsList,
            boolean step,
            boolean printMetrics,
            String color) {

        LOG.info("Формирование графика {}", titleGraph);
        int xSize = Math.max(1200, metricsList.size());
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
        String[] colors = {"#00009f", "#00af00", "#afaf00", "#ff0000", "#00afaf", "#af00af"};

        // максимальное/минимальное значение Y и X
        long xValueMax = 0L;
        double yValueMin = 999999999999999999.99;
        double yValueMax = 0.00;
        for (int i = 0; i < metricsList.size(); i++) {
            for (int e = 0; e < metricsList.get(i).getValueSize(); e++) {
                yValueMin = Math.min(yValueMin, metricsList.get(i).getValue(e));
                yValueMax = Math.max(yValueMax, metricsList.get(i).getValue(e));
            }
            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
        xValueMax = xValueMax - startTime;

        StringBuilder sbResult = new StringBuilder("<!--" + titleGraph + "-->\n" +
                "\t\t\t<svg viewBox=\"0 0 " + (xMax + xMarginRight) + " " + (yMax + yMarginBottom) + "\" class=\"chart\">\n" +
                "\t\t\t\t<text " +
                "font-size=\"" + (fontSize * 2) + "\" " +
                "x=\"" + (xSize / 2) + "\" " +
                "y=\"" + (yStart - fontSize * 2) + "\">" +
                "" + titleGraph + "</text>\n" +
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
        if (titleLines.length > 1) {
            for (int e = 0; e < titleLines.length; e++) {
                sbResult.append(
                        "\t\t\t\t<polyline fill=\"none\" stroke=\"" + colors[e] + "\" stroke-width=\"4\" points=\"" + xStart + "," + yCur + " " + xStart * 3 + "," + yCur + "\"/>\n" +
                        "\t\t\t\t<text font-size=\"10\" font-weight=\"bold\" x=\"" + ((xStart * 3) + 10) + "\" y=\"" + yCur + "\">" + titleLines[e] + "</text>\n");
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
//        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep);

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
        xScale = Math.min(xScale, metricsList.size());
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

        StringBuilder[] sbGraph = new StringBuilder[metricsList.get(0).getValueSize()]; // графики
        for (int e = 0; e < metricsList.get(0).getValueSize(); e++) {
            String curColor = metricsList.get(0).getValueSize() > 1 ? colors[e] : color;
            sbGraph[e] = new StringBuilder();
            sbGraph[e].append("<!-- График" + (e + 1) + " -->\n" +
                    "\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"" + curColor + "\" " +
                    "stroke-width=\"" + (lineSize * 2) + "\" " +
                    "points=\"" + xCur + "," + yMax + " \n");
        }

        for (int i = 0; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - startTime) * xRatio + xStart;

            // ступеньки
            if (step && i > 0) {
                for (int e = 0; e < metricsList.get(i).getValueSize(); e++) {
                    sbGraph[e].append(xCur + "," + (yMax - Math.round(metricsList.get(i - 1).getValue(e) * yRatio)) + " \n");
                }
            }

            List<Double> yPrevList = new ArrayList<>();
            for (int e = 0; e < metricsList.get(i).getValueSize(); e++) {
                String curColor = metricsList.get(0).getValueSize() > 1 ? colors[e] : color;
                double y = yMax - Math.round(metricsList.get(i).getValue(e) * yRatio);
                // график
                sbGraph[e].append(xCur + "," + y + " \n");

                // значение отличается от предыдущего
                if (i == 0 || metricsList.get(i - 1).getValue(e) != metricsList.get(i).getValue(e)) {
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
                        "<title>" + decimalFormat.format(metricsList.get(i).getValue(e)) + "</title> " +
                        "</g>\n");
            }
        }
        for (int e = 0; e < metricsList.get(0).getValueSize(); e++) {
            sbGraph[e].append("\"/>\n");
            sbResult.append(sbGraph[e].toString());
        }
        sbResult.append(sbSignature.toString());
        sbResult.append(sbSignatureTitle.toString());

        sbResult.append("\t\t\t</svg>\n");
        return sbResult.toString();
    }
}
