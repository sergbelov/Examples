package ru.examples.svgExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.charts.XSSFScatterChartData;
import ru.utils.files.FileUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SvgGraphExample {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {

        List<DateTimeValue> metricsList = new ArrayList<>();
        List<DateTimeValue> vuList = new ArrayList<>();
        List<Process> processesList = new ArrayList<>();

        SvgGraphExample svgGraphExample = new SvgGraphExample();

        long startTime = System.currentTimeMillis();

        svgGraphExample.fullVU(vuList);
        svgGraphExample.fullMetricsList(metricsList);
        svgGraphExample.fullProcess(processesList);

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

        String graph = svgGraphExample.getSvgGraphLine(
                startTime,
                vuList,
                true,
                false);
        sbHtml.append(graph.toString());

        graph = svgGraphExample.getSvgGraphLine(
                startTime,
                metricsList,
                false,
                true);
        sbHtml.append(graph.toString());

        graph = svgGraphExample.getSvgGraphBar(
                startTime,
                processesList,
                true);
        sbHtml.append(graph.toString());

        sbHtml.append("\t\t</div>\n" +
                        "\t</body>\n" +
                        "</html>");

        FileUtils fileUtils = new FileUtils();
        fileUtils.writeFile("GraphSVG.html", sbHtml.toString());

    }


    /**
     * Линейный график
     *
     * @param startTime
     * @param metricsList
     * @param step
     * @param printMetrics
     * @return
     */
    public String getSvgGraphLine(
            long startTime,
            List<DateTimeValue> metricsList,
            boolean step,
            boolean printMetrics) {

        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        DecimalFormat decimalFormat = new DecimalFormat("###.#");

        int xSize = Math.max(1200, metricsList.size());
        int ySize = 600;
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

        // максимальное/минимальное значение Y и X
        long xValueMax = 0L;
        double yValueMin = 999999999999999999L;
        double yValueMax = 0;
        for (int i = 0; i < metricsList.size(); i++) {
            yValueMin = Math.min(yValueMin, metricsList.get(i).getValue());
            yValueMax = Math.max(yValueMax, metricsList.get(i).getValue());
            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
        xValueMax = xValueMax - startTime;

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
//        yValueMax = (yValueMax / 10) * 10 + 10;
/*
        while (yValueMax % 5 != 0){
            yValueMax++;
        }
*/
        double yScale = Math.min(20.00, yValueMax);
        double yRatio = ySize / (yValueMax * 1.00);
        double yRatioValue = yValueMax / yScale;
        double yStep = ySize / yScale;
        double yValue = 0.00;
        double yCur = yMax;
        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep);

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
        double xScale = Math.min(60.00, xValueMax);
        xScale = Math.min(xScale, metricsList.size());
        double xRatio = xSize / (xValueMax * 1.00);
        double xRatioValue = xValueMax / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = startTime;
        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, xStart, xScale, xRatio, xRatioValue, xStep);

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
        sbResult.append("\n\n<!-- График -->\n\t\t\t\t<polyline " +
                "fill=\"none\" " +
                "stroke=\"" + color + "\" " +
                "stroke-width=\"" + (lineSize * 2) + "\" " +
                "points=\"" + xCur + "," + yMax + " \n");

        for (int i = 0; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - startTime) * xRatio + xStart;
            if (step && i > 0) {
                sbResult.append(xCur + "," + (yMax - Math.round(metricsList.get(i - 1).getValue() * yRatio)) + " \n");
            }
            sbResult.append(xCur + "," + (yMax - Math.round(metricsList.get(i).getValue() * yRatio)) + " \n");

            // ToDo - отметить минимальные и максимальные значения
            if (metricsList.get(i).getValue() == yValueMin) {
//                sbResult.append()
            }

            if (printMetrics && (i == 0 || metricsList.get(i - 1).getValue() != metricsList.get(i).getValue())) {
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
     * График в виде столбцов
     * @param startTime
     * @param metricsList
     * @param printMetrics
     * @return
     */
    public String getSvgGraphBar(
            long startTime,
            List<Process> metricsList,
            boolean printMetrics) {

        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        DecimalFormat decimalFormat = new DecimalFormat("###.#");

        int xSize = Math.max(1200, metricsList.size());
        int ySize = 600;
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

        // максимальное/минимальное значение Y и X
        long xValueMax = 0L;
        double yValueMin = 999999999999999999L;
        double yValueMax = 0;
        for (int i = 0; i < metricsList.size(); i++) {
            yValueMin = Math.min(yValueMin, metricsList.get(i).getValue1());
            yValueMax = Math.max(yValueMax, metricsList.get(i).getValue1());
            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
        xValueMax = xValueMax - startTime;

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
        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep);

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
        double xScale = Math.min(60.00, xValueMax);
        xScale = Math.min(xScale, metricsList.size());
        double xRatio = xSize / (xValueMax * 1.00);
        double xRatioValue = xValueMax / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = startTime;
        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, xStart, xScale, xRatio, xRatioValue, xStep);

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
        sbResult.append("\n\n<!-- График -->\n");

        for (int i = 0; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - startTime) * xRatio + xStart;
            LOG.info("xCur: {}", xCur);
//            sbResult.append(xCur + "," + (yMax - Math.round(processList.get(i).getValue1() * yRatio)) + " \n");
            sbResult.append("<rect " +
            "stroke=\"#0f0f0f\" " +
//                    "fill=\"" + background + "\" " +
                    "fill-opacity=\"0\"" +
                    "x=\"" + xCur + "\" " +
                    "y=\"" + (yMax - Math.round(metricsList.get(i).getValue1() * yRatio)) + "\" " +
                    "width=\"" + xSize + "\" " +
                    "height=\"" + ySize + "\"/>");

            // ToDo - отметить минимальные и максимальные значения
//            if (metricsList.get(i).getValue() == yValueMin) {
////                sbResult.append()
//            }

/*            if (printMetrics && (i == 0 || metricsList.get(i - 1).getValue() != metricsList.get(i).getValue())) {
                sbSignature.append("\t\t\t\t<text " +
                        "font-size=\"" + fontSize + "\" " +
                        "fill=\"#000000\" " +
                        "font-weight=\"bold\" " +
                        "x=\"" + (xCur - xText) + "\" " +
                        "y=\"" + (yMax - Math.round(metricsList.get(i).getValue() * yRatio) - yText) + "\">" +
                        metricsList.get(i).getValue() + "</text>\n");
            }*/
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
     * генерим список метрик
     * @param dateTimeValueList
     */
    private void fullMetricsList(List<DateTimeValue> dateTimeValueList){
        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        int countX = 100;
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
    }

    /**
     * активацмя виртуальных пользователей
     * @param dateTimeValueList
     */
    private void fullVU(List<DateTimeValue> dateTimeValueList){
        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        int countVU = 10;
        int countMaxVU = 50;
        int countStepVU = 10;
        int tesDuration = 10; // длительность теста (мин)
        long startTime = System.currentTimeMillis();
        long stopTime = startTime + tesDuration * 60000L; // ремя завершения теста
        long curTime = startTime;
        long delay = 5000;

        LOG.info("startTime: {} - {}", datetimeFormat.format(startTime), countVU);
        dateTimeValueList.add(new DateTimeValue(
                curTime,
                countVU));

        while (curTime < stopTime){
            curTime = curTime + delay;
            if (countVU < countMaxVU) {
                countVU = countVU + countStepVU;
            }
            countVU = Math.min(countVU, countMaxVU);

            LOG.info("VU {} - {}",
                    datetimeFormat.format(curTime),
                    countVU);

            dateTimeValueList.add(new DateTimeValue(
                    curTime,
                    countVU));
        }
    }

    /**
     *
     * @param processesList
     */
    private void fullProcess(List<Process> processesList) {
        DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        int countX = 10;
        long startTime = System.currentTimeMillis();
        long curTime = startTime;
        long delay = 5000;
        LOG.info("startTime: {}", datetimeFormat.format(startTime));
        for (int i = 0; i < countX; i++) {
            curTime = curTime + delay;
            int value1 = (int) ((Math.random() * 1000) + 5);
            int value2 = (int) ((Math.random() * value1) + 1);
            int value3 = (int) ((Math.random() * value2) + 1);

            LOG.info("Заполняем список метрик {} из {}; {} - {}, {}, {}",
                    i + 1,
                    countX,
                    datetimeFormat.format(curTime),
                    value1,
                    value2,
                    value3);

            processesList.add(new Process(
                    curTime,
                    value1,
                    value2,
                    value3));
        }
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

    static class Process {
        long time;
        int value1;
        int value2;
        int value3;

        public Process(int value1, int value2, int value3) {
            this.time = System.currentTimeMillis();
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public Process(long time, int value1, int value2, int value3) {
            this.time = time;
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public long getTime() {
            return time;
        }

        public int getValue1() {
            return value1;
        }

        public int getValue2() { return value2; }

        public int getValue3() { return value3; }
    }

}
