package ru.examples.svgExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.files.FileUtils;

import java.text.DecimalFormat;

public class SvgGraphExample {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("###.#");
        int x = 1000;
        int y = 500;
        int xStart = x / 30;
        int yStart = xStart;
        int xMax = x + xStart;
        int yMax = y + yStart;
        int xMarginRight = x / 100;
        int yMarginBottom = x / 10;
        int xText = x / 200;
        int yText = x / 350;
        int fontSize = x / 100;
        int lineSize = Math.max(1, x / 1000);
        String background = "#dfdfdf";
        String color = "#009f9f";

//        int[] xValues = {1, 2, 3, 4, 5};
//        int[] yValues = {10, 30, 50, 20, 70};
        int[] xValues = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        int[] yValues = {10, 30, 50, 1000, 70, 90, 60, 80, 40, 100, 311, 50, 10, 20, 30, 111};

        StringBuilder result = new StringBuilder(
                "\t\t\t<svg viewBox=\"0 0 " + (xMax + xMarginRight) + " " + (yMax + yMarginBottom) + "\" class=\"chart\">\n" +
                "\t\t\t\t<text font-size=\"" + (fontSize * 2) + "\" x=\"" + (x / 2) + "\" y=\"" + (yStart - fontSize + yText) + "\">Отчет</text>\n" +
                "\t\t\t\t<rect stroke=\"#0f0f0f\" fill=\"" + background + "\" x=\"" + xStart + "\" y=\"" + yStart + "\" width=\"" + x + "\" height=\"" + y + "\"/>\n\n");

        double yValueMax = 0;
        // ось X
        double xRatio = x / (xValues.length * 1.00);
        double xCur = xStart;
        for (int i = 0; i < xValues.length; i++) {
            yValueMax = Math.max(yValueMax, yValues[i]);
            xCur = xCur + xRatio;
            result.append("\t\t\t\t<polyline fill=\"none\" stroke=\"#a0a0a0\" stroke-dasharray=\"" + xText + "\" stroke-width=\"" + lineSize + "\" points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n")
                    .append("\t\t\t\t<text font-size=\"" + fontSize + "\" writing-mode=\"tb\" x=\"" + (xCur - yText) + "\" y=\"" + (yMax + yText) + "\">" + xValues[i] + "</text>\n");
        }
        result.append("\n");

        // ось Y
        double scale = 20.00;
        double yRatio = y / (yValueMax * 1.00);
        double yValueRatio = yValueMax / scale;
        double yStep = y / scale;
        double yValue = 0.00;
        double yCur = yMax;
//        LOG.info("y:{}; yValueMax:{}; yRatio:{}; yValueRatio:{}; yStep:{}", y, yValueMax, yRatio, yValueRatio, yStep);
        while (yCur > yStart) {
            yCur = yCur - yStep;
            yValue = yValue + yValueRatio;
            result.append("\t\t\t\t<polyline fill=\"none\" stroke=\"#a0a0a0\" stroke-dasharray=\"" + xText + "\" stroke-width=\"" + lineSize + "\" points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n")
                    .append("\t\t\t\t<text font-size=\"" + fontSize + "\" x=\"0\" y=\"" + (yCur + yText) + "\">" + decimalFormat.format(yValue) + "</text>\n");
        }

        // рисуем график
        xCur = xStart;
        StringBuilder sb = new StringBuilder();
        result.append("\t\t\t\t<polyline fill=\"none\" stroke=\"" + color + "\" stroke-width=\"" + (lineSize * 2) + "\" points=\"" + xStart + "," + yMax + " \n");
        for (int i = 0; i < yValues.length; i++) {
            xCur = xCur + xRatio;
            result.append(xCur + "," + (yMax - Math.round(yValues[i] * yRatio)) + " \n");
            sb.append("\t\t\t\t<text font-size=\"" + fontSize + "\" font-weight=\"bold\" x=\"" + (xCur - xText) + "\" y=\"" + (yMax - Math.round(yValues[i] * yRatio) - yText) + "\">" + yValues[i] + "</text>\n");
        }
        result.append("\"/>\n\n");
        result.append(sb.toString());
        result.append("\t\t\t</svg>\n");

/*
                "\t\t\t\t<line stroke=\"#0000ff\" stroke-dasharray=\"5\" stroke-width=\"2\" x1=\"0\" y1=\"0\" x2=\"200\" y2=\"200\"/>\n" +

        "\t\t\t<svg viewBox=\"0 0 " + x + " " + y + "\" class=\"chart\">\n" +
                "\t\t\t\t<polyline fill=\"none\" stroke=\"#0074d9\" stroke-width=\"3\" points=\"0,120 20,60 40,80 60,20\"/>\n" +
                "\t\t\t\t<polyline fill=\"none\" stroke=\"#ff0000\" stroke-width=\"3\" points=\"0,220 20,160 40,180 60,120\"/>\n" +
                "\t\t\t</svg>\n" +
*/

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
        sbHtml.append(result.toString())
                .append("\t\t</div>\n" +
                        "\t</body>\n" +
                        "</html>");

        FileUtils fileUtils = new FileUtils();
        fileUtils.writeFile("GraphSVG.html", sbHtml.toString());
    }
}
