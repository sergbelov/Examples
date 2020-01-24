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
        int xStart = 25;
        int yStart = 25;
        int xMax = x + xStart;
        int yMax = y + yStart;
        String background = "#dfdfdf";
        String color = "#009f9f";

//        int[] xValues = {1, 2, 3, 4, 5};
//        int[] yValues = {10, 30, 50, 20, 70};
        int[] xValues = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        int[] yValues = {10, 30, 50, 1000, 70, 90, 60, 80, 40, 100, 311, 50, 10, 20, 30, 111};

        StringBuilder result = new StringBuilder(
                "\t\t\t<svg viewBox=\"0 0 " + (xMax+10) + " " + (yMax+100) + "\" class=\"chart\">\n" +
                "\t\t\t\t<text font-size=\"20\" x=\"" + (x/2) +"\" y=\"15\">Отчет</text>\n" +
                "\t\t\t\t<rect stroke=\"#0f0f0f\" fill=\"" + background + "\" x=\"" + xStart + "\" y=\"" + yStart + "\" width=\"" + x + "\" height=\"" + y + "\"/>\n\n");

        int yValueMax = 0;
        // ось X
        int xRatio = x / xValues.length;
        int xCur = xStart;
        for (int i = 0; i < xValues.length; i++) {
            yValueMax = Math.max(yValueMax, yValues[i]);
            xCur = xCur + xRatio;
            result.append("\t\t\t\t<polyline fill=\"none\" stroke=\"#a0a0a0\" stroke-dasharray=\"5\" stroke-width=\"1\" points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax +"\"/>\n")
                  .append("\t\t\t\t<text font-size=\"11\" writing-mode=\"tb\" x=\"" + (xCur-2) +"\" y=\"" + (yMax+5) + "\">" + xValues[i] + "</text>\n");
        }
        result.append("\n");

        // ось Y
        double yRatio = y / (yValueMax * 1.00);
        int yStep = y / 20;
        int yCur = yMax;
        double yValueRatio = yValueMax / 20.00;
        double yValue = 0.00;
        while (yCur >= yStart){
            result.append("\t\t\t\t<polyline fill=\"none\" stroke=\"#a0a0a0\" stroke-dasharray=\"5\" stroke-width=\"1\" points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n")
                  .append("\t\t\t\t<text font-size=\"10\" x=\"0\" y=\"" + (yCur+4) + "\">" + decimalFormat.format(yValue) + "</text>\n");
            yValue = yValue + yValueRatio;
            yCur = yCur - yStep;
        }

        // рисуем график
        xCur = xStart;
        StringBuilder sb = new StringBuilder();
        result.append("\t\t\t\t<polyline fill=\"none\" stroke=\"" + color + "\" stroke-width=\"3\" points=\"" + xStart + "," + yMax +" \n");
        for (int i = 0; i < yValues.length; i++){
            xCur = xCur + xRatio;
            result.append(xCur + "," + (yMax - Math.round(yValues[i] * yRatio))  + " \n");
            sb.append("\t\t\t\t<text font-size=\"11\" font-weight=\"bold\" x=\"" + (xCur-4) + "\" y=\"" + (yMax - Math.round(yValues[i] * yRatio) - 3) + "\">" + yValues[i] + "</text>\n");
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

        FileUtils fileUtils = new FileUtils();
        fileUtils.writeFile("GraphSVG.html", result.toString());
    }
}
