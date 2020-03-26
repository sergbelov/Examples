package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.rewrite.MapRewritePolicy;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.utils.files.FileUtils;
import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValues;
import ru.utils.load.data.StatData;
import ru.utils.load.data.errors.ErrorRsGroup;
import ru.utils.load.data.errors.ErrorRs;
import ru.utils.load.data.errors.ErrorsGroup;
import ru.utils.load.data.Metric;
import ru.utils.load.graph.Graph;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.text.*;
import java.util.List;
import java.util.Map;

public class Report {
    private static final Logger LOG = LogManager.getLogger(Report.class);
    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat sdf4 = new SimpleDateFormat("yyyyMMdd");

    private MultiRunService multiRunService;
    private String pathReport;
    private Graph graph = new Graph();
    private FileUtils fileUtils = new FileUtils();
    private ErrorsGroup errorsGroup = new ErrorsGroup(); // типы ошибок (для группировки)
    private int countActiveHost = 0;
    private double tpsMax = 0.00;
    private int vuCountMax = 0;

    /**
     * Сохраняем отчет в HTML - файл
     *
     * @param multiRunService
     */
    public void createReportHtml(
            MultiRunService multiRunService,
            String pathReport) {
        createReportHtml(multiRunService, pathReport, false, false);
    }

    /**
     * Сохраняем отчет в HTML - файл
     *
     * @param multiRunService
     */
    public void createReportHtml(
            MultiRunService multiRunService,
            String pathReport,
            boolean yStartFrom0,
            boolean printMetrics) {

        this.multiRunService = multiRunService;

        // каталог для  отчета в формате
        // родительский каталог / наименование_скрипта / YYYYMMDD / YYYYMMDDHHMMSS(старта)
        if (!pathReport.endsWith("/") && !pathReport.endsWith("\\")){
            pathReport = pathReport + "/";
        }
        pathReport = pathReport + multiRunService.getName() + "/";
        pathReport = pathReport + sdf4.format(multiRunService.getTestStartTime()) + "/";
        pathReport = pathReport + sdf3.format(multiRunService.getTestStartTime()) + "/";
        File path = new File(pathReport);
        if (!path.exists()){
            path.mkdirs();
        }
        this.pathReport = pathReport;

        /* ==== графики
            0 - VU
            1 - Response time
            2 - Длительность выполнения процесса (информация из БД)
            3 - TPS
            4 - Статистика из БД БПМ
            5 - Ошибки
            6 - Количество шагов завершенных в секунду
            7 - BpmsJobEntityImpl Count
            8 - RetryPolicyJobEntityImpl Count
        */

        LOG.info("{}: Формирование отчета...", multiRunService.getName());

        // формируем HTML - файл
        StringBuilder sbHtml = new StringBuilder(
                "<html>\n" +
                        "\t<head>\n" +
                        "\t\t<meta charset=\"UTF-8\">\n" +
                        "\t\t<style>\n" +
                        "\t\t\tbody, html {width:100%; height:100%; margin:0; background:#fdfdfd}\n\n" +
                        "\t\t\t.graph {width:95%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:20px auto; border:1px; solid #ccc; background:#fff}\n\n" +
                        "\t\t\ttable {border: solid 1px; border-collapse: collapse;}\n" +
                        "\t\t\tcaption {font-size: 10;}\n" +
                        "\t\t\ttd {border: solid 1px;}\n" +
                        "\t\t\tth {border: solid 1px; background: #f0f0f0; font-size: 12;}\n" +
                        "\t\t\t.td_red {border: solid 1px; background-color: rgb(255, 192, 192);}\n" +
                        "\t\t\t.td_green {border: solid 1px; background-color: rgb(192, 255, 192);}\n" +
                        "\t\t\t.td_yellow {border: solid 1px; background-color: rgb(255, 255, 192);}\n" +
                        "\t\t\ttable.scroll {border-spacing: 0; border: 1px solid black;}\n" +
                        "\t\t\ttable.scroll tbody,\n" +
                        "\t\t\ttable.scroll thead {height: 80px; display: block; }\n" +
                        "\t\t\ttable.scroll tbody {height: 300px; overflow-y: auto; overflow-x: hidden;}\n"+
//                        "\t\t\ttbody td:last-child, thead th:last-child {border-right: none;}\n" +
                        "\t\t</style>\n" +
                        "\t</head>\n" +
                        "\t<body>\n" +
                        "<h2>" + multiRunService.getName() + " (" + multiRunService.getProcessDefinitionKey() +
                        ") период " + sdf1.format(multiRunService.getTestStartTimeReal()) +
                        " - " + sdf1.format(multiRunService.getTestStopTimeReal()) + " (" +
                        timeMillisToString(multiRunService.getTestStartTimeReal(), multiRunService.getTestStopTimeReal()) +
                        ")</h2>\n");

        // информация по версиям модуля и активности хостов
        sbHtml.append(getInfoFromCSM(multiRunService.getCsmUrl()));

        // параметры
        sbHtml.append(multiRunService.getParams());

        sbHtml.append("<script>\n" +
                "\tfunction GraphVisible(obj, x) {\n" +
                "\t\tvar lines=document.getElementsByClassName(x)\n" +
                "//\t\talert(lines.length)\n" +
                "//\t\talert(lines[0].style.visible)\n" +
                "//\t\tconsole.log(x, lines, lines[0])\n" +
                "\t\tfor (var z = 0; z < lines.length; z++){\n" +
                "\t\t\tif (obj.checked){lines[z].style.display = 'block'} else {lines[z].style.display = 'none'}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "</script>\n");

        // процессы выполняемые после снятия нагрузки
        if (multiRunService.getDataFromDB().getWaitCountStart() != null) {
            sbHtml.append("<br><table><tbody>\n" +
                    "<tr><td>Количество не выполненных процессов на конец теста (" + sdf1.format(multiRunService.getDataFromDB().getWaitStartTime()) + ")</td><td>")
                    .append(multiRunService.getDataFromDB().getWaitCountStart())
                    .append("</td></tr>\n" +
                            "<tr><td>Время ожидания завершения процессов (сек)</td><td>")
                    .append(multiRunService.getDataFromDB().getWaitTime() / 1000)
                    .append("</td></tr>\n" +
                            "<tr><td>Количество не выполненных процессов после ожидания (" + sdf1.format(multiRunService.getDataFromDB().getWaitStopTime()) + ")</td><td>")
                    .append(multiRunService.getDataFromDB().getWaitCountStop())
                    .append("</td></tr>\n</tbody></table>");
        }

        // старт VU
        sbHtml.append("\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine("Running Vusers",
                        multiRunService,
                        multiRunService.getVuList(),
                        true,
                        true,
                        printMetrics))
                .append("\t\t</div>\n");

        // TPS
        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine("Количество запросов в секунду (tps)",
                        multiRunService,
                        multiRunService.getMetricsList(),
                        yStartFrom0,
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        sbHtml.append(getTpsAvg());

        // Response time
        if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DUR_MAX) > 0) {
            sbHtml.append("\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Response time",
                            multiRunService,
                            multiRunService.getMetricsList(),
                            new double[]{multiRunService.getResponseTimeMax_ms()},
                            yStartFrom0,
                            false,
                            printMetrics))
                    .append("\t\t</div>\n");

            sbHtml.append("<!-- Статистика по Response time -->\n" +
                    "\t\t<div>\n<table style=\"width: 50%;\"><tbody>\n" +
                    "<tr><th rowspan=\"2\">Сервис</th>\n" +
                    "<th colspan=\"4\">Response time (мс)</th>\n" +
                    "<th colspan=\"3\">Количество запросов</th></tr>\n" +
                    "<tr><th>минимальная</th>\n" +
                    "<th>средняя</th>\n" +
                    "<th>90 перцентиль</th>\n" +
                    "<th>максимальная</th>\n" +
                    "<th>общее</th>\n" +
                    "<th>с ответом</th>\n" +
                    "<th>без ответа</th>\n")
                    .append("<tr><td>")
                    .append(multiRunService.getName())
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DUR_MIN)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DUR_AVG)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DUR_90)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DUR_MAX)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL_RS)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL) -
                            multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL_RS)))
                    .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");
        }

        // группа метрик из БД БПМ (выводим при наличии)
        StatData countStepCompleteInSec = null;
        StatData transitionsTime = null;
        if (multiRunService.getMetricsList().get(0).getIntValue(new Metric[]{
                Metric.DB_COMPLETED,
                Metric.DB_RUNNING,
                Metric.DB_FAILED}) > 0) {

            // длительность выполнения процесса (информация из БД)
            sbHtml.append("\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Длительность выполнения процесса (информация из БД)",
                            multiRunService,
                            multiRunService.getMetricsList(),
                            yStartFrom0,
                            false,
                            printMetrics))
                    .append("\t\t</div>\n");

            sbHtml.append("<!-- Статистика по длительности выполнения (информация из БД) -->\n" +
                    "\n<br><div><table style=\"width: 50%;\">\n" +
                    "<thead>\n" +
                    "<tr><th colspan=\"6\">Длительность выполнения процесса (информация из БД)</th></tr>\n" +
                    "<tr><td colspan=\"6\" align=\"Center\" style=\"font-size: 10px;\">" + multiRunService.getSqlSelect(1) + "</td></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"font-size: 10px;\">" +
                    "<th rowspan=\"2\">Процесс</th>\n" +
                    "<th colspan=\"4\">Длительность выполнения (из БД) (мс)</th>\n" +
                    "<th>Количество запросов</th></tr>\n" +
                    "<tr><th>минимальная</th>\n" +
                    "<th>средняя</th>\n" +
                    "<th>90 перцентиль</th>\n" +
                    "<th>максимальная</th>\n" +
                    "<th>COMPLETED</th>\n")
                    .append("<tr><td>")
//                    .append(multiRunService.getName())
                    .append(multiRunService.getProcessDefinitionKey())
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DB_DUR_MIN)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DB_DUR_AVG)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DB_DUR_90)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DB_DUR_MAX)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).compare(Metric.COUNT_CALL, Metric.DB_COMPLETED)) {
                sbHtml.append("<td class=\"td_green\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_COMPLETED)));
            sbHtml.append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

            // Длительность выполнения шагов из БД БПМ
            sbHtml.append(multiRunService
                    .getDataFromDB()
                    .getTaskDuration(
                            multiRunService.getProcessDefinitionKey(),
                            multiRunService.getTestStartTime(),
                            multiRunService.getTestStopTime()));

            // Статистика из БД БПМ
            sbHtml.append("\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Статистика из БД БПМ",
                            multiRunService,
                            multiRunService.getMetricsList(),
                            yStartFrom0,
                            false,
                            printMetrics))
                    .append("\t\t</div>\n");

            sbHtml.append("<!-- Статистика из БД БПМ  -->\n")
                    .append("\n<br><div><table style=\"width: 50%;\">\n" +
                    "<thead>\n" +
                    "<tr><th colspan=\"6\">Статистика из БД БПМ</th></tr>\n" +
                    "<tr><td colspan=\"6\" align=\"Center\" style=\"font-size: 10px;\">" + multiRunService.getSqlSelect(0) + "</td></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"font-size: 10px;\">" +
                    "<th>Сервис</th>\n" +
                    "<th>Отправлено</th>\n" +
                    "<th>COMPLETED</th>\n" +
                    "<th>RUNNING</th>\n" +
                    "<th>FAILED</th>\n" +
                    "<th>Потеряно</th></tr>\n")
                    .append("<tr><td>")
                    .append(multiRunService.getName())
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).compare(Metric.COUNT_CALL, Metric.DB_COMPLETED)) {
                sbHtml.append("<td class=\"td_green\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_COMPLETED)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_RUNNING) > 0) {
                sbHtml.append("<td class=\"td_yellow\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_RUNNING)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_FAILED) > 0) {
                sbHtml.append("<td class=\"td_red\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_FAILED)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).compare(Metric.COUNT_CALL, new Metric[]{
                    Metric.DB_COMPLETED,
                    Metric.DB_RUNNING,
                    Metric.DB_FAILED})) {
                sbHtml.append("<td>");
            } else {
                sbHtml.append("<td class=\"td_red\">");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL) -
                    multiRunService.getMetricsList().get(0).getIntValue(new Metric[]{
                            Metric.DB_COMPLETED,
                            Metric.DB_RUNNING,
                            Metric.DB_FAILED})))
                    .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

            // Количество шагов завершенных в секунду
            countStepCompleteInSec = multiRunService
                    .getDataFromDB()
                    .getCountStepCompleteInSec(
                            multiRunService.getProcessDefinitionKey(),
                            multiRunService.getTestStartTime(),
                            multiRunService.getTestStopTime());

            if (multiRunService.getDataFromDB().getCountEndInSecList().size() > 0) {
                sbHtml.append(countStepCompleteInSec.getResultStr());
                sbHtml.append("\t\t<div class=\"graph\">\n")
                        .append(graph.getSvgGraphLine("Количество шагов завершенных в секунду",
                                multiRunService,
                                multiRunService.getDataFromDB().getCountEndInSecList(),
                                yStartFrom0,
                                false,
                                printMetrics))
                        .append("\t\t</div>\n");
            }

            // Throttling
            if (!isEmptyDateTimeValue(multiRunService.getBpmsJobEntityImplCountList())) {
                sbHtml.append("\t\t<div class=\"graph\">\n")
                        .append(graph.getSvgGraphLine("BpmsJobEntityImpl Count",
                                multiRunService,
                                multiRunService.getBpmsJobEntityImplCountList(),
                                yStartFrom0,
                                false,
                                printMetrics))
                        .append("\t\t</div>\n");
            }

            // Retry
            if (!isEmptyDateTimeValue(multiRunService.getRetryPolicyJobEntityImplCountList())) {
                sbHtml.append("\t\t<div class=\"graph\">\n")
                        .append(graph.getSvgGraphLine("RetryPolicyJobEntityImpl Count",
                                multiRunService,
                                multiRunService.getRetryPolicyJobEntityImplCountList(),
                                yStartFrom0,
                                false,
                                printMetrics))
                        .append("\t\t</div>\n");
            }

            // длительность переходов между процессами
            transitionsTime = multiRunService
                    .getDataFromDB()
                    .getTransitionsTime(
                            multiRunService.getProcessDefinitionKey(),
                            multiRunService.getTestStartTime(),
                            multiRunService.getTestStopTime());
            if (transitionsTime != null) {
                sbHtml.append(transitionsTime.getResultStr());
            }

            // дубли в БД БПМ
            sbHtml.append(multiRunService
                    .getDataFromDB()
                    .getDuplicateCheck(
                            multiRunService.getTestStartTime(),
                            multiRunService.getTestStopTime()));
        }


        // ошибки (при наличии)
        boolean printError = (multiRunService.getErrorList().size() > 0) ? true : false;
        if (printError) {
            sbHtml.append("<!-- Ошибки -->\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Ошибки",
                            multiRunService,
                            multiRunService.getMetricsList(),
                            yStartFrom0,
                            false,
                            printMetrics))
                    .append("\t\t</div>\n");
        }
        // группируем ошибки по типам
        if (printError) {
            sbHtml.append(getErrorsGroupComment(
                    multiRunService.getErrorList(),
                    multiRunService.getErrorRsGroupList()));
        }

        // ссылка на Графану (Хосты детализировано)
        sbHtml.append(getLinkUrl(
                "Grafana - ППРБ Хосты детализированно",
                multiRunService.getGrafanaHostsDetailUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime()));

        // ссылка на Графану (Хосты детализировано CPU)
        sbHtml.append(getLinkUrl(
                "Grafana - ППРБ Хосты детализированно CPU",
                multiRunService.getGrafanaHostsDetailCpuUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime()));

        // PNG Хосты детализировано CPU
        sbHtml.append(getPngFromGrafana(
                multiRunService.getGrafanaApiKey(),
                multiRunService.getGrafanaHostsDetailCpuPngUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime(),
                "GrafanaHostsDetailCPU"));

        // ссылка на Графану (TransportThreadPools)
        sbHtml.append(getLinkUrl(
                "Grafana - ППРБ TransportThreadPools",
                multiRunService.getGrafanaTransportThreadPoolsUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime()));

        // PNG TransportThreadPools
        sbHtml.append(getPngFromGrafana(
                multiRunService.getGrafanaApiKey(),
                multiRunService.getGrafanaTransportThreadPoolsPngUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime(),
                "TransportThreadPools"));

        // ссылка на Графану (ТС)
        sbHtml.append(getLinkUrl(
                "Grafana - ТС (БПМ модульный)",
                multiRunService.getGrafanaTsUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime()));

        // ссылка на Спланк
        sbHtml.append(getLinkUrl(
                "Splunk - Метрики BPM",
                multiRunService.getSplunkUrl(),
                String.valueOf(multiRunService.getTestStartTime()).substring(0, 10),
                String.valueOf(multiRunService.getTestStopTime()).substring(0, 10)));




        // сравнительная таблица для Confluence
        sbHtml.append("<br><table><caption>Сравнительная таблица</caption><tbody>\n<tr>" +
                "<th rowspan=\"2\">Кол-во<br>узлов</th>" +
                "<th rowspan=\"2\">Сервис</th>" +
                "<th rowspan=\"2\">Длительность<br>теста</th>" +
                "<th rowspan=\"2\">Отправлено<br>запросов</th>" +
                "<th colspan=\"4\">Процессы в БД</th>" +

                "<th rowspan=\"2\">max tps (VU)</th>" +
                "<th rowspan=\"2\">Response<br>time<br>90% (мс)</th>" +
                "<th rowspan=\"2\">Длительность<br>выполнения<br>90% (мс)</th>" +

                "<th colspan=\"3\">Количество шагов завершенных в секунду</th>" +
                "<th rowspan=\"2\">Длительность<br>переходов<br>90% (мс)</th>" +
                "<th rowspan=\"2\">CPU<br>core max<br>(%)</th></tr\n>" +
                "<tr><th>COMPETED</th>" +
                "<th>RUNNING</th>" +
                "<th>FAILED</th>" +
                "<th>потеряно</th>" +
                "<th>всего шагов</th>" +
                "<th>шагов<br>COMPLETED</th>" +
                "<th>90%</th>" +
                "</tr>\n<tr><td>");
        sbHtml.append(countActiveHost)
                .append("</td><td>")
                .append(multiRunService.getName())
                .append("</td><td>")
                .append(timeMillisToString(multiRunService.getTestStartTimeReal(), multiRunService.getTestStopTimeReal()))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).compare(Metric.COUNT_CALL, Metric.DB_COMPLETED)) {
            sbHtml.append("<td class=\"td_green\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_COMPLETED)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_RUNNING) > 0) {
            sbHtml.append("<td class=\"td_yellow\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_RUNNING)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_FAILED) > 0) {
            sbHtml.append("<td class=\"td_red\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DB_FAILED)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).compare(Metric.COUNT_CALL, new Metric[]{
                Metric.DB_COMPLETED,
                Metric.DB_RUNNING,
                Metric.DB_FAILED})) {
            sbHtml.append("<td>");
        } else {
            sbHtml.append("<td class=\"td_red\">");
        }
        sbHtml.append(decimalFormat.format(
                multiRunService.getMetricsList().get(0).getIntValue(Metric.COUNT_CALL) -
                        (multiRunService.getMetricsList().get(0).getIntValue(new Metric[]{
                                Metric.DB_COMPLETED,
                                Metric.DB_RUNNING,
                                Metric.DB_FAILED}))))
                .append("</td><td>")
                .append(decimalFormat.format(tpsMax))
                .append(" (")
                .append(vuCountMax)
                .append(")</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DUR_90)))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DB_DUR_90)))
                .append("</td><td>");

        if (countStepCompleteInSec != null) { // Количество шагов завершенных в секунду
            sbHtml.append(decimalFormat.format(countStepCompleteInSec.getCountList(0)))
                    .append("</td><td>")
                    .append(decimalFormat.format(countStepCompleteInSec.getCountList(1)))
                    .append("</td><td>")
                    .append(decimalFormat.format(countStepCompleteInSec.getVal90()))
                    .append("</td><td>");
        } else {
            sbHtml.append("</td> <td></td> <td></td><td>");
        }

        if (transitionsTime != null) { // Длительность переходов
            sbHtml.append(decimalFormat.format(transitionsTime.getVal90()))
                    .append("</td><td>");
        } else {
            sbHtml.append("</td><td>");
        }

        sbHtml.append("</td></tr>\n</tbody></table>\n")
              .append("<br><br>\n")
              .append("\n\t</body>\n</html>");

        // сохраняем HTML - файл
        String fileName = pathReport + multiRunService.getName() + "_" +
                sdf3.format(multiRunService.getTestStartTime()) + "-" +
                sdf3.format(multiRunService.getTestStopTime()) + ".html";
        fileUtils.writeFile(fileName, sbHtml.toString());
        LOG.info("{}: Сформирован отчет {}", multiRunService.getName(), fileName);
        if (multiRunService.getNumberRequest().get() > 0) {
            LOG.info("{}: Количество запросов: {}", multiRunService.getName(), multiRunService.getNumberRequest());
        }
    }

    /**
     * Список не исполненных запросов
     *
     * @param callList
     * @return
     */
    private String getRunningRequests(List<Call> callList) {
        StringBuilder res = new StringBuilder();
        int cnt = 0;
        for (int i = 0; i < callList.size(); i++) {
            if (callList.get(i).getDuration() == null) {
                res.append("<tr><td>")
                        .append(++cnt)
                        .append("</td><td>")
                        .append(sdf1.format(callList.get(i).getStartTime()))
                        .append("</td></tr>\n");
            }
        }
        if (res.length() > 0) {
            return "<br>\nНе исполненные запросы<br>\n" +
                    "<table>" + res.toString() + "</table>\n";
        } else return "";
    }


    /**
     * Группировка ошибок по типу
     */
    public String getErrorsGroupComment(
            List<ErrorRs> errorList,
            List<ErrorRsGroup> errorRsGroupList
    ) {
        StringBuilder sbErrors = new StringBuilder("\n<div>\nОшибки<br>\n<table><tbody>\n" +
                "<tr><th>Группа ошибок</th>" +
                "<th>Количество</th>" +
                "<th>Первая ошибка из группы</th></tr>\n");

        try {
            for (int i = 0; i < errorList.size(); i++) {
                int find1;
                String text = errorList.get(i).getText();
                String comment = text;
                if ((find1 = findErrorGroup(text)) > -1) {
                    comment = errorsGroup.getComment(find1);
                }
                int find2;
                if ((find2 = findErrorGroupCommentList(errorRsGroupList, comment)) > -1) {
                    errorRsGroupList.get(find2).incCount();
                } else {
                    errorRsGroupList.add(new ErrorRsGroup(text, comment, 1));
                }
            }

            // соберем сгруппированные ошибки
            for (int i = 0; i < errorRsGroupList.size(); i++) {
                sbErrors.append("<tr>")
                        .append("<td>")
                        .append(errorRsGroupList.get(i).getComment())
                        .append("</td>")
                        .append("<td>")
                        .append(errorRsGroupList.get(i).getCount())
                        .append("</td>")
                        .append("<td>")
                        .append(errorRsGroupList.get(i).getFirstError())
                        .append("</td>")
                        .append("</tr>\n");
            }
        } catch (Exception e) {
            LOG.error("Не удалось сгруппировать ошибки\n", e);
        }

        sbErrors.append("</tbody></table>\n</div>\n");
        return sbErrors.toString();
    }

    /**
     * Поиск зафиксированной группы с ошибкой
     *
     * @param comment
     * @return
     */
    private int findErrorGroupCommentList(
            List<ErrorRsGroup> errorRsGroupList,
            String comment) {
        int res = -1;
        if (comment != null) {
            for (int i = 0; i < errorRsGroupList.size(); i++) {
                if (errorRsGroupList.get(i).getComment().equals(comment)) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Поиск типа ошибки
     *
     * @param text
     * @return
     */
    private int findErrorGroup(String text) {
        int res = -1;
        if (text != null) {
            for (int i = 0; i < errorsGroup.getCount(); i++) {
                int find = 0;
                int count = errorsGroup.getConditions(i).length;
                for (int j = 0; j < count; j++) {
                    if (text.indexOf(errorsGroup.getConditions(i)[j]) > -1) {
                        find++;
                    } else {
                        break;
                    }
                }
                if (find == count) { // нашлись все совпадения
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Формирование URL по шаблону
     *
     * @param blank
     * @param baseUrl
     * @param startTime
     * @param stopTime
     * @return
     */
    private String getLinkUrl(
            String blank,
            String baseUrl,
            long startTime,
            long stopTime) {

        return getLinkUrl(
                blank,
                baseUrl,
                String.valueOf(startTime),
                String.valueOf(stopTime));
    }
    /**
     * Формирование URL по шаблону
     *
     * @param blank     - название ссылки
     * @param baseUrl   - базовый URL
     * @param startTime
     * @param stopTime
     * @return
     */
    private String getLinkUrl(
            String blank,
            String baseUrl,
            String startTime,
            String stopTime) {

        StringBuilder res = new StringBuilder("\n<div><p><a href=\"");
        res.append(baseUrl
                .replace("{from}", startTime)
                .replace("{to}", stopTime));
        res.append("\" target=\"_blank\">" + blank + "</a></p></div>\n");

        LOG.debug("Ссылка на {} {}", blank, res.toString());
        return res.toString();
    }

    /**
     * Информация о версии модуля и активности хостов
     *
     * @param csmUrl
     * @return
     */
    private String getInfoFromCSM(String csmUrl) {
        StringBuilder res = new StringBuilder("\n<h3>Версия модуля, активность хостов</h3>\n" +
                "<table><tbody>\n" +
                "<tr><th>Host</th><th>Module</th><th>Version</th><th>Active</th></tr>\n");
        if (!csmUrl.isEmpty()) {
            try {
                URL url = new URL(csmUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                InputStream content = connection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(content));
                String line;
                StringBuilder data = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    data.append(line);
                }

                LOG.debug("CSM Response: {}", data.toString());
                JSONArray jsonArray = new JSONArray(data.toString());
                for (int h = 0; h < jsonArray.length(); h++) {
                    JSONObject jsonObjectHost = jsonArray.getJSONObject(h);
                    LOG.debug("CSM Response[{}]: {}", h, jsonObjectHost.toString());

                    String host = jsonObjectHost.getString("host");
                    String module = jsonObjectHost.getJSONObject("module").getString("normalName");
                    String version = jsonObjectHost.getJSONObject("module").getString("version");
                    boolean enabled = jsonObjectHost.getJSONObject("module").getBoolean("enabled");

                    res.append("<tr>")
                            .append("<td>")
                            .append(host)
                            .append("</td>")
                            .append("<td>")
                            .append(module)
                            .append("</td>")
                            .append("<td>")
                            .append(version)
                            .append("</td>");

                    if (enabled) {
                        countActiveHost++;
                        res.append("<td class=\"td_green\">Да</td>");
                    } else {
                        res.append("<td class=\"td_red\">Нет</td>");
                    }

                    res.append("</tr>\n");

                    LOG.debug("{} {} {} {}", host, module, version, enabled);
                }

            } catch (Exception e) {
                LOG.error("Ошибка при получении данных из CSM\n", e);
            }
        }
        res.append("</tbody></table>\n");
        return res.toString();
    }


    /**
     * Преобразуем длительность периода в миллисекундах в чч:мм:сс
     *
     * @param startTime
     * @param stopTime
     * @return
     */
    public String timeMillisToString(long startTime, long stopTime) {
        long time = 0;
        try {
            time = sdf2.parse(sdf2.format(stopTime)).getTime() - sdf2.parse(sdf2.format(startTime)).getTime();
        } catch (ParseException e) {
            LOG.error("", e);
        }
        return timeMillisToString(time);
    }

    /**
     * Преобразуем длительность периода в миллисекундах в чч:мм:сс
     *
     * @param millis
     * @return
     */
    public String timeMillisToString(long millis) {
        long hour = millis / (3600 * 1000),
                min = millis / (60 * 1000) % 60,
                sec = (int) Math.ceil(millis / 1000.00 % 60);
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }

    /**
     * Значение tps
     *
     * @return
     */
    public String getTpsAvg() {
        final int step = 2999;
        double[] tps = {0.00, 0.00};    // 0-tps, 1-tpsRs
        int[] vuCount = {0, 0};         // 0-vuCount, 1-vuCountRs
        long[] prevTime = {0L};
        int[] prevCount = {-1};

        multiRunService.getVuList()
                .stream()
                .sorted()
                .forEach(x -> {
                    if (prevCount[0] != x.getIntValue()){
                        if (prevCount[0] > 0 && (x.getTime() - prevTime[0] - 1) > step){
                            // среднее значение TPS
                            double[] tpsCur = {0.00, 0.00}; // 0-tpsCur, 1-tpsCurRs
                            int[] periodCount = {0};

                            multiRunService.getMetricsList()
                                    .stream()
                                    .filter(f -> f.getTime()>=prevTime[0] && f.getTime() <= x.getTime()-1)
                                    .forEach(d -> {
                                        periodCount[0]++;
                                        tpsCur[0] = tpsCur[0] + d.getDoubleValue(Metric.TPS);
                                        tpsCur[1] = tpsCur[1] + d.getDoubleValue(Metric.TPS_RS);
                                    });

/*
                            long[] times = {prevTime[0], 0L, x.getTime()-1}; // 0-start, 1-stopStep, 2-stop
                            while (times[0] <= times[2]) {
                                periodCount[0]++;
                                times[1] = times[0] + 1000;
                                times[1] = Math.min(times[1], times[2]);

                                CallMetrics callMetrics = multiRunService.getMetricsForPeriod(times[0], times[1]);
                                LOG.trace("{}: {}-{}: {}, {} {}",
                                        multiRunService.getName(),
                                        sdf1.format(times[0]),
                                        sdf1.format(times[1]),
                                        prevCount[0],
                                        callMetrics.getTps(),
                                        callMetrics.getTpsRs());

                                tpsCur[0] = tpsCur[0] + callMetrics.getTps();
                                tpsCur[1] = tpsCur[1] + callMetrics.getTpsRs();
                                times[0] = times[1] + 1;
                            }
*/

                            tpsCur[0] = tpsCur[0] / periodCount[0];
                            tpsCur[1] = tpsCur[1] / periodCount[0];

                            if (tpsCur[0] > tps[0]) {
                                tps[0] = tpsCur[0];
                                vuCount[0] = prevCount[0];
                            }
                            if (tpsCur[1] > tps[1]) {
                                tps[1] = tpsCur[1];
                                vuCount[1] = prevCount[0];
                            }
                            LOG.debug("{}: VU ({} - {}): {}; tps:{}; tpsRs:{}",
                                    multiRunService.getName(),
                                    sdf1.format(prevTime[0]),
                                    sdf1.format(x.getTime()-1),
                                    prevCount[0],
                                    tps[0],
                                    tps[1]);
                        }
                        prevTime[0] = x.getTime();
                        prevCount[0] = x.getIntValue();
                    }
                });

        if (tps[1] > 0) { // TPS с ответами
            tpsMax = tps[1];
            vuCountMax = vuCount[1];
        } else {
            tpsMax = tps[0];
            vuCountMax = vuCount[0];
        }
        StringBuilder res = new StringBuilder("<table><tbody>\n" +
                "<tr><th rowspan=\"2\">Сервис</th>" +
                "<th colspan=\"2\">tps отправлено</th>" +
                "<th colspan=\"2\">tps response</th></tr>\n" +
                "<tr><th>max</th><th>VU</th><th>max</th><th>VU</th></tr>\n" +
                "<tr><td>");
        res.append(multiRunService.getName())
                .append("</td><td>")
                .append(decimalFormat.format(tps[0]))
                .append("</td><td>")
                .append(decimalFormat.format(vuCount[0]))
                .append("</td><td>")
                .append(decimalFormat.format(tps[1]))
                .append("</td><td>")
                .append(decimalFormat.format(vuCount[1]))
                .append("</td></tr>\n</tbody></table>\n");
        return res.toString();
    }


    /**
     * Есть не нулевые значения в списке
     * @param list
     * @return
     */
    private boolean isEmptyDateTimeValue(List<DateTimeValues> list) {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                for (Map.Entry<Metric, Number> map : list.get(i).getValues().entrySet() ) {
                    if (list.get(i).getDoubleValue(map.getKey()) != 0 ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * График в формате PNG из Grafana
     * @param apiKey
     * @param grafanaUrl
     * @param startTime
     * @param stopTime
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String grafanaUrl,
            long startTime,
            long stopTime,
            String fileName){

        return getPngFromGrafana(
                apiKey,
                grafanaUrl,
                0,
                0,
                startTime,
                stopTime,
                1000,
                500,
                fileName);
    }

    /**
     * График в формате PNG из Grafana
     *
     * @param apiKey
     * @param grafanaUrl
     * @param orgId
     * @param panelId
     * @param startTime
     * @param stopTime
     * @param width
     * @param height
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String grafanaUrl,
            int orgId,
            int panelId,
            long startTime,
            long stopTime,
            int width,
            int height,
            String fileName){

        if (grafanaUrl != null && !grafanaUrl.isEmpty()) {
            fileName = fileName.replace(".png", "").replace(".PNG", "");
            fileName = fileName + "_" + sdf3.format(startTime) + "-" + sdf3.format(stopTime) + ".png";
            LOG.info("График из Grafana: {}", fileName);
            String fileFull = pathReport + fileName;

            try {
//            startTime = 1585059368000L; // отладка
//            stopTime = 1585060349000L;

                grafanaUrl = grafanaUrl
                        .replace("{from}", String.valueOf(startTime))
                        .replace("{to}", String.valueOf(stopTime))
                        .replace("{orgId}", String.valueOf(orgId))
                        .replace("{panelId}", String.valueOf(panelId))
                        .replace("{width}", String.valueOf(width))
                        .replace("{height}", String.valueOf(height));

                LOG.debug("{}", grafanaUrl);

                URL url = new URL(grafanaUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setDoOutput(true);

                try (
                        InputStream inputStream = connection.getInputStream();
                        FileOutputStream fileOutPutStream = new FileOutputStream(fileFull);
                ) {
                    int bytesRead;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                        fileOutPutStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    LOG.error("Ошибка при записи файла {}\n", fileFull, e);
                    fileName = null;
                }

            } catch (Exception e) {
                LOG.error("Ошибка при получении данных из Grafana\n", e);
                fileName = null;
            }

            if (fileName != null) {
                return "\n<div class=\"graph\"><p><img style=\"width:95%\" src=\"" + fileName + "\" alt=\"PNG\"/></p></div>\n";
            }
        }

        return "";
    }
}
