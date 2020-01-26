package ru.examples.svgExample;

import com.graphbuilder.math.func.RandFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.files.FileUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SvgGraphExample {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {
        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        List<DateTimeValue> dateTimeValueList = new ArrayList<>();

// генерим список метрик
        int countX = 360;
        long startTime = System.currentTimeMillis();
        long curTime = startTime;
        long delay = 5000;
        LOG.info("startTime: {}", datetimeFormat.format(startTime));
        for (int i = 0; i < countX; i++) {
            curTime = curTime + delay;
            int value = (int) ((Math.random() * 1000) + 5);
            LOG.info("Заполняем список метрик {} из {}; {} - {}",
                    i + 1,
                    countX,
                    datetimeFormat.format(curTime),
                    value);

            dateTimeValueList.add(new DateTimeValue(
                    curTime,
                    value));
        }

        SvgGraphExample svgGraphExample = new SvgGraphExample();
        String graphLine = svgGraphExample.getSvgGraphLine(
                startTime,
                dateTimeValueList,
                false);

        // формируем HTML - файл
        StringBuilder sbHtml = new StringBuilder(
                "<html>\n" +
                        "\t<head>\n" +
                        "\t\t<meta charset=\"UTF-8\">\n" +
                        "\t\t<style>\n" +
                        "\t\t\tbody, html{width:100%; height:100%; margin:0; background:#fdfdfd}\n" +
                        "\t\t\t.graph{width:80%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:50px auto; border:1px solid #ccc; background:#fff}\n" +
                        "\t\t</style>\n" +
                        "\t</head>\n" +
                        "\t<body>\n" +
                        "\t\t<div class=\"graph\">\n");
        sbHtml.append(graphLine.toString())
                .append("\t\t</div>\n" +
                        "\t</body>\n" +
                        "</html>");

        FileUtils fileUtils = new FileUtils();
        fileUtils.writeFile("GraphSVG.html", sbHtml.toString());

    }

    public String getSvgGraphLine(
            long startTime,
            List<DateTimeValue> metricsList,
            boolean printMetrics) {

        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        DecimalFormat decimalFormat = new DecimalFormat("###.#");

        int xSize = Math.max(1000, metricsList.size());
        int ySize = 500;
        int xStart = xSize / 30;
        int yStart = xStart;
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
        String color = "#009f9f";

        // максималное значение Y
        double yValueMin = 999999999999999999L;
        double yValueMax = 0;
        for (int i = 0; i < metricsList.size(); i++) {
            yValueMin = Math.min(yValueMin, metricsList.get(i).getValue());
            yValueMax = Math.max(yValueMax, metricsList.get(i).getValue());
        }

        StringBuilder sbResult = new StringBuilder(
                "\t\t\t<svg viewBox=\"0 0 " + (xMax + xMarginRight) + " " + (yMax + yMarginBottom) + "\" class=\"chart\">\n" +
                        "\t\t\t\t<text " +
                        "font-size=\"" + (fontSize * 2) + "\" " +
                        "x=\"" + (xSize / 2) + "\" " +
                        "y=\"" + (yStart - fontSize + yText) + "\">" +
                        "Отчет</text>\n" +
                        "<!-- Область графика -->\n" +
                        "\t\t\t\t<rect " +
                        "stroke=\"#0f0f0f\" " +
                        "fill=\"" + background + "\" " +
                        "x=\"" + xStart + "\" " +
                        "y=\"" + yStart + "\" " +
                        "width=\"" + xSize + "\" " +
                        "height=\"" + ySize + "\"/>\n" +
                        "\n<!-- Ось Y -->\n");

        // ось Y
        double yScale = Math.min(20.00, yValueMax);
        double yRatio = ySize / (yValueMax * 1.00);
        double yRatioValue = yValueMax / yScale;
        double yStep = ySize / yScale;
        double yValue = 0.00;
        double yCur = yMax;
//        LOG.info("y:{}; yValueMax:{}; yRatio:{}; yValueRatio:{}; yStep:{}", ySize, yValueMax, yRatio, yRatioValue, yStep);

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
        sbResult.append("\n<!-- Ось X -->\n");
        int countX = metricsList.size();
        double xScale = Math.min(60.00, countX);
        double xRatio = xSize / (countX * 1.00);
        double xRatioValue = countX / (xScale+1);
        double xStep = xSize / (xScale+1);
        double elementNum = xRatioValue;
        double xCur = xStart;
//        LOG.info("xSize:{}; countX:{}, xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, countX, xScale, xRatio, xRatioValue, xStep);

        sbResult.append("\t\t\t\t<text " +
                "font-size=\"" + fontAxisSize + "\" " +
                "letter-spacing=\"0.5\" " +
                "writing-mode=\"tb\" " +
                "x=\"" + xCur + "\" " +
                "y=\"" + (yMax + yText) + "\">" +
                datetimeFormat.format(startTime) + "</text>\n");

        while (xCur < xMax) {
            xCur = xCur + xStep;
            sbResult.append("\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#a0a0a0\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n")
                    .append("\t\t\t\t<text " +
                            "font-size=\"" + fontAxisSize + "\" " +
                            "letter-spacing=\"0.5\" " +
                            "writing-mode=\"tb\" " +
                            "x=\"" + xCur + "\" " +
                            "y=\"" + (yMax + yText) + "\">" +
                            datetimeFormat.format(metricsList.get((int) elementNum).getTime()) + "</text>\n");

            elementNum = elementNum + xRatioValue;
            elementNum = Math.min(elementNum, countX-1);
        }

        // рисуем график
        StringBuilder sbSignature = new StringBuilder("<!-- Метрики на графике -->\n"); // значения метрик на графике
        xCur = xStart;
        sbResult.append("\n\n<!-- График -->\n\t\t\t\t<polyline " +
                "fill=\"none\" " +
                "stroke=\"" + color + "\" " +
                "stroke-width=\"" + (lineSize * 2) + "\" " +
                "points=\"" + xStart + "," + yMax + " \n");

        for (int i = 0; i < metricsList.size(); i++){
            xCur = xCur + xRatio;
            sbResult.append(xCur + "," + (yMax - Math.round(metricsList.get(i).getValue() * yRatio)) + " \n");

            // ToDo - отметить минимальные и максимальные значения
            if (metricsList.get(i).getValue() == yValueMin){
//                sbResult.append()
            }

            if (printMetrics) {
                sbSignature.append("\t\t\t\t<text " +
                        "font-size=\"" + fontSize + "\" " +
                        "fill=\"#000000\" " +
                        "font-weight=\"bold\" " +
                        "x=\"" + (xCur - xText) + "\" " +
                        "y=\"" + (yMax - Math.round(metricsList.get(i).getValue() * yRatio) - yText) + "\">" +
                        metricsList.get(i).getValue() + "</text>\n");
            }
        }
        sbResult.append("\"/>\n\n");
        sbResult.append(sbSignature.toString());

        sbResult.append("\t\t\t</svg>\n");
/*
            sbSignature.append("\t\t\t\t<text font-size=\"" + fontSize + "\" fill=\"#000000\" stroke=\"" + color + "\" stroke-width=\"1px\" font-weight=\"bold\" x=\"" + (xCur - xText) + "\" y=\"" + (yMax - Math.round(yValues[i] * yRatio) - yText) + "\">" + yValues[i] + "</text>\n");

                "\t\t\t\t<line stroke=\"#0000ff\" stroke-dasharray=\"5\" stroke-width=\"2\" x1=\"0\" y1=\"0\" x2=\"200\" y2=\"200\"/>\n" +

        "\t\t\t<svg viewBox=\"0 0 " + x + " " + y + "\" class=\"chart\">\n" +
                "\t\t\t\t<polyline fill=\"none\" stroke=\"#0074d9\" stroke-width=\"3\" points=\"0,120 20,60 40,80 60,20\"/>\n" +
                "\t\t\t\t<polyline fill=\"none\" stroke=\"#ff0000\" stroke-width=\"3\" points=\"0,220 20,160 40,180 60,120\"/>\n" +
                "\t\t\t</svg>\n" +
*/
        return sbResult.toString();
    }

    /**
     * Класс для хранения метрик
     */
    static class DateTimeValue {
        long time;
        int value;

        public DateTimeValue(int value) {
            this.time = System.currentTimeMillis();
            this.value = value;
        }

        public DateTimeValue(long time, int value) {
            this.time = time;
            this.value = value;
        }

        public long getTime() {
            return time;
        }

        public int getValue() {
            return value;
        }
    }
}
