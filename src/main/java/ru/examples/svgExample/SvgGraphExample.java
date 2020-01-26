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

        List<DateTimeValue> dateTimeValueList = new ArrayList<>();

// генерим список метрик
        int countX = 100;
        long startTime = System.currentTimeMillis();
        long delay = 10000;
        for (int i = 0; i < countX; i++){
            LOG.info("Заполняем список метрик {} из {}", i+1, countX);
            dateTimeValueList.add(new DateTimeValue(
                    startTime,
                    (int) ((Math.random() * 1000) + 5)));
            startTime = startTime + delay;
        }

        SvgGraphExample svgGraphExample = new SvgGraphExample();
        String graphLine = svgGraphExample.getSvgGraphLine(dateTimeValueList);

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

    public String getSvgGraphLine(List<DateTimeValue> metricsList) {
        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        DecimalFormat decimalFormat = new DecimalFormat("###.#");

        int xSize = 1000;
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
        boolean printValue = false;

        // максималное значение Y
        double yValueMax = 0;
        for (int i = 0; i < metricsList.size(); i++) {
            yValueMax = Math.max(yValueMax, metricsList.get(i).getValue());
        }

        StringBuilder result = new StringBuilder(
                "\t\t\t<svg viewBox=\"0 0 " + (xMax + xMarginRight) + " " + (yMax + yMarginBottom) + "\" class=\"chart\">\n" +
                "\t\t\t\t<text " +
                        "font-size=\"" + (fontSize * 2) + "\" " +
                        "x=\"" + (xSize / 2) + "\" " +
                        "y=\"" + (yStart - fontSize + yText) + "\">" +
                        "Отчет</text>\n" +
                "\t\t\t\t<rect " +
                        "stroke=\"#0f0f0f\" " +
                        "fill=\"" + background + "\" " +
                        "x=\"" + xStart + "\" " +
                        "y=\"" + yStart + "\" " +
                        "width=\"" + xSize + "\" " +
                        "height=\"" + ySize + "\"/>\n\n");

        // ось X
        double scaleX = Math.min(60.00, metricsList.size());
        double xRatio = xSize / (metricsList.size() * 1.00);
        double xValueRatio = metricsList.size() / scaleX;
        double xStep = xSize / scaleX;
        double xValuesElementNum = 0;
        double xCur = xStart;
//        LOG.info("x:{}; xValueMax:{}; xRatio:{}; xValueRatio:{}; xStep:{}", x, xValueMax, xRatio, xValueRatio, xStep);
        while (xCur < xMax) {
            xCur = xCur + xStep;
            xValuesElementNum = xValuesElementNum + xValueRatio;
            xValuesElementNum = Math.min(xValuesElementNum, metricsList.size() -1);
            result.append("\t\t\t\t<polyline " +
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
                            datetimeFormat.format(metricsList.get((int) xValuesElementNum).getTime()) + "</text>\n");
        }
        result.append("\n");

        // ось Y
        double scaleY = Math.min(20.00, yValueMax);
        double yRatio = ySize / (yValueMax * 1.00);
        double yValueRatio = yValueMax / scaleY;
        double yStep = ySize / scaleY;
        double yValue = 0.00;
        double yCur = yMax;
//        LOG.info("y:{}; yValueMax:{}; yRatio:{}; yValueRatio:{}; yStep:{}", y, yValueMax, yRatio, yValueRatio, yStep);
        while (yCur > yStart) {
            yCur = yCur - yStep;
            yValue = yValue + yValueRatio;
            result.append("\t\t\t\t<polyline " +
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

        // рисуем график
        xCur = xStart;
        StringBuilder sbSignature = new StringBuilder();
        result.append("\n\t\t\t\t<polyline " +
                "fill=\"none\" " +
                "stroke=\"" + color + "\" " +
                "stroke-width=\"" + (lineSize * 2) + "\" " +
                "points=\"" + xStart + "," + yMax + " \n");
        for (int i = 0; i < metricsList.size(); i++) {
            xCur = xCur + xRatio;
            result.append(xCur + "," + (yMax - Math.round(metricsList.get(i).getValue() * yRatio)) + " \n");
            if (printValue) {
                sbSignature.append("\t\t\t\t<text " +
                        "font-size=\"" + fontSize + "\" " +
                        "fill=\"#000000\" " +
                        "font-weight=\"bold\" " +
                        "x=\"" + (xCur - xText) + "\" " +
                        "y=\"" + (yMax - Math.round(metricsList.get(i).getValue() * yRatio) - yText) + "\">" +
                        metricsList.get(i).getValue() + "</text>\n");
            }
        }
        result.append("\"/>\n\n");
        result.append(sbSignature.toString());
        result.append("\t\t\t</svg>\n");
/*
            sbSignature.append("\t\t\t\t<text font-size=\"" + fontSize + "\" fill=\"#000000\" stroke=\"" + color + "\" stroke-width=\"1px\" font-weight=\"bold\" x=\"" + (xCur - xText) + "\" y=\"" + (yMax - Math.round(yValues[i] * yRatio) - yText) + "\">" + yValues[i] + "</text>\n");

                "\t\t\t\t<line stroke=\"#0000ff\" stroke-dasharray=\"5\" stroke-width=\"2\" x1=\"0\" y1=\"0\" x2=\"200\" y2=\"200\"/>\n" +

        "\t\t\t<svg viewBox=\"0 0 " + x + " " + y + "\" class=\"chart\">\n" +
                "\t\t\t\t<polyline fill=\"none\" stroke=\"#0074d9\" stroke-width=\"3\" points=\"0,120 20,60 40,80 60,20\"/>\n" +
                "\t\t\t\t<polyline fill=\"none\" stroke=\"#ff0000\" stroke-width=\"3\" points=\"0,220 20,160 40,180 60,120\"/>\n" +
                "\t\t\t</svg>\n" +
*/

        return result.toString();
    }

    /**
     * Класс для хранения метрик
     */
    static class DateTimeValue {
        long time;
        int value;

        public  DateTimeValue(int value){
            this.time = System.currentTimeMillis();
            this.value = value;
        }
        public DateTimeValue(long time, int value){
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
