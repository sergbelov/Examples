package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import ru.utils.load.data.CallMetrics;
import ru.utils.load.data.graph.Graph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.*;
import java.util.List;
import java.util.Map;

public class Report {
    private static final Logger LOG = LogManager.getLogger(Report.class);
    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private MultiRunService multiRunService;
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
    public void saveReportHtml(
            MultiRunService multiRunService,
            String pathReport) {
        saveReportHtml(multiRunService, pathReport, false, false);
    }

    /**
     * Сохраняем отчет в HTML - файл
     *
     * @param multiRunService
     */
    public void saveReportHtml(
            MultiRunService multiRunService,
            String pathReport,
            boolean yStartFrom0,
            boolean printMetrics) {

        this.multiRunService = multiRunService;

        /* ==== графики
            0 - VU
            1 - Response time
            2 - Длительность выполнения (информация из БД)
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
                        "\t\t\tbody, html { width:100%; height:100%; margin:0; background:#fdfdfd}\n\n" +
                        "\t\t\t.graph { width:95%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:50px auto; border:1px; solid #ccc; background:#fff}\n\n" +
                        "\t\t\ttable { border: solid 1px; border-collapse: collapse;}\n" +
                        "\t\t\tcaption {font-size: 10;}\n" +
                        "\t\t\ttd { border: solid 1px;}\n" +
                        "\t\t\tth { border: solid 1px; background: #f0f0f0; font-size: 12;}\n" +
                        "\t\t\t.td_red { border: solid 1px; background-color: rgb(255, 192, 192);}\n" +
                        "\t\t\t.td_green { border: solid 1px; background-color: rgb(192, 255, 192);}\n" +
                        "\t\t\t.td_yellow { border: solid 1px; background-color: rgb(255, 255, 192);}\n" +
                        "\t\t\ttable.scroll { border-spacing: 0; border: 1px solid black;}\n" +
                        "\t\t\ttable.scroll tbody,\n" +
                        "\t\t\ttable.scroll thead { display: block; }\n" +
                        "\t\t\ttable.scroll tbody { height: 100px; overflow-y: auto; overflow-x: hidden;}\n" +
                        "\t\t\ttbody td:last-child, thead th:last-child { border-right: none;}\n" +
                        "\t\t</style>\n" +
                        "\t</head>\n" +
                        "\t<body>\n" +
                        "<h2>" + multiRunService.getName() + " (" + multiRunService.getKeyBpm() +
                        ") период " + sdf1.format(multiRunService.getTestStartTimeReal()) +
                        " - " + sdf1.format(multiRunService.getTestStopTimeReal()) + " (" +
                        timeMillisToString(multiRunService.getTestStartTimeReal(), multiRunService.getTestStopTimeReal()) +
                        ")</h2>\n");

        // информация по версиям модуля и активности хостов
        sbHtml.append(getInfoFromCSM(multiRunService.getCsmUrl()));

        // параметры
        sbHtml.append(multiRunService.getParams());

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
        if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DurMax) > 0) {
            sbHtml.append("\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Response time",
                            multiRunService,
                            multiRunService.getMetricsList(),
                            yStartFrom0,
                            false,
                            printMetrics))
                    .append("\t\t</div>\n");

            sbHtml.append("<!-- Статистика по Response time -->\n" +
                    "\t\t<div>\n<table><tbody>\n" +
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
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DurMin)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DurAvg)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.Dur90)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DurMax)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCall)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCallRs)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCall) -
                            multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCallRs)))
                    .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");
        }

        // группа метрик из БД БПМ (выводим при наличии)
        StatData countStepCompleteInSec = null;
        StatData transitionsTime = null;
        if (multiRunService.getMetricsList().get(0).getIntValue(new Metric[]{
                Metric.DbCompleted,
                Metric.DbRunning,
                Metric.DbFailed}) > 0) {

            // длительность выполнения (информация из БД)
            sbHtml.append("\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Длительность выполнения (информация из БД)",
                            multiRunService,
                            multiRunService.getMetricsList(),
                            yStartFrom0,
                            false,
                            printMetrics))
                    .append("\t\t</div>\n");

            sbHtml.append("<!-- Статистика по длительности выполнения (информация из БД) -->\n" +
                    "\t\t<div>\n<table>\n" +
                    "<caption>Длительность выполнения (информация из БД)<br>\n" + multiRunService.getSqlSelect(1) + "</caption>" +
                    "<tbody>\n" +
                    "<tr><th rowspan=\"2\">Сервис</th>\n" +
                    "<th colspan=\"4\">Длительность выполнения (из БД) (мс)</th>\n" +
                    "<th>Количество запросов</th></tr>\n" +
                    "<tr><th>минимальная</th>\n" +
                    "<th>средняя</th>\n" +
                    "<th>90 перцентиль</th>\n" +
                    "<th>максимальная</th>\n" +
                    "<th>COMPLETED</th>\n")
                    .append("<tr><td>")
                    .append(multiRunService.getName())
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DbDurMin)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DbDurAvg)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DbDur90)))
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DbDurMax)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).compare(Metric.CountCall, Metric.DbCompleted)) {
                sbHtml.append("<td class=\"td_green\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbCompleted)));
            sbHtml.append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

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
                    .append("\t\t<div>\n<table><caption>Статистика из БД БПМ<br>\n" +
                            multiRunService.getSqlSelect(0) +
                            "</caption><tbody>\n" +
                            "<tr><th>Сервис</th>\n" +
                            "<th>Отправлено</th>\n" +
                            "<th>COMPLETED</th>\n" +
                            "<th>RUNNING</th>\n" +
                            "<th>FAILED</th>\n" +
                            "<th>Потеряно</th>\n")
                    .append("<tr><td>")
                    .append(multiRunService.getName())
                    .append("</td><td>")
                    .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCall)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).compare(Metric.CountCall, Metric.DbCompleted)) {
                sbHtml.append("<td class=\"td_green\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbCompleted)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DbRunning) > 0) {
                sbHtml.append("<td class=\"td_yellow\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbRunning)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DbFailed) > 0) {
                sbHtml.append("<td class=\"td_red\">");
            } else {
                sbHtml.append("<td>");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbFailed)))
                    .append("</td>");

            if (multiRunService.getMetricsList().get(0).compare(Metric.CountCall, new Metric[]{
                    Metric.DbCompleted,
                    Metric.DbRunning,
                    Metric.DbFailed})) {
                sbHtml.append("<td>");
            } else {
                sbHtml.append("<td class=\"td_red\">");
            }
            sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCall) -
                    multiRunService.getMetricsList().get(0).getIntValue(new Metric[]{
                            Metric.DbCompleted,
                            Metric.DbRunning,
                            Metric.DbFailed})))
                    .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

            // Длительность процессов из БД БПМ
            sbHtml.append(multiRunService
                    .getDataFromDB()
                    .getProcessDuration(
                            multiRunService.getKeyBpm(),
                            multiRunService.getTestStartTime(),
                            multiRunService.getTestStopTime()));

            // Количество шагов завершенных в секунду
            countStepCompleteInSec = multiRunService
                    .getDataFromDB()
                    .getCountStepCompleteInSec(
                            multiRunService.getKeyBpm(),
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
                            multiRunService.getKeyBpm(),
                            multiRunService.getTestStartTime(),
                            multiRunService.getTestStopTime());
            if (transitionsTime != null) {
                sbHtml.append(transitionsTime.getResultStr());
            }

            // дубли в БД БПМ
            sbHtml.append(multiRunService
                    .getDataFromDB()
                    .getDoubleCheck(
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

        // сравнительная таблица для Confluence
        sbHtml.append("<br><table><caption>Сравнительная таблица</caption><tbody>\n<tr>" +
                "<th rowspan=\"2\">Кол-во<br>узлов</th>" +
                "<th rowspan=\"2\">Сервис</th>" +
                "<th rowspan=\"2\">Длительность<br>теста</th>" +
                "<th rowspan=\"2\">Отправлено<br>запросов</th>" +
                "<th colspan=\"4\">Процессы в БД</th>" +

                "<th rowspan=\"2\">max tps при VU</th>" +
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
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCall)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).compare(Metric.CountCall, Metric.DbCompleted)) {
            sbHtml.append("<td class=\"td_green\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbCompleted)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DbRunning) > 0) {
            sbHtml.append("<td class=\"td_yellow\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbRunning)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(Metric.DbFailed) > 0) {
            sbHtml.append("<td class=\"td_red\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(Metric.DbFailed)))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).compare(Metric.CountCall, new Metric[]{
                Metric.DbCompleted,
                Metric.DbRunning,
                Metric.DbFailed})) {
            sbHtml.append("<td>");
        } else {
            sbHtml.append("<td class=\"td_red\">");
        }
        sbHtml.append(decimalFormat.format(
                multiRunService.getMetricsList().get(0).getIntValue(Metric.CountCall) -
                        (multiRunService.getMetricsList().get(0).getIntValue(new Metric[]{
                                Metric.DbCompleted,
                                Metric.DbRunning,
                                Metric.DbFailed}))))
                .append("</td><td>")
                .append(decimalFormat.format(tpsMax))
                .append(" / ")
                .append(vuCountMax)
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.Dur90)))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getDoubleValue(Metric.DbDur90)))
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

        sbHtml.append("</td></tr>\n</tbody></table>\n");

        // ссылка на Графану (Хосты детализировано)
        sbHtml.append(getLinkUrl(
                "Grafana - ППРБ Хосты детализированно",
                multiRunService.getGrafanaHostsDetailUrl(),
                String.valueOf(multiRunService.getTestStartTime()),
                String.valueOf(multiRunService.getTestStopTime())));

        // ссылка на Графану (Хосты детализировано CPU)
        sbHtml.append(getLinkUrl(
                "Grafana - ППРБ Хосты детализированно CPU",
                multiRunService.getGrafanaHostsDetailCpuUrl(),
                String.valueOf(multiRunService.getTestStartTime()),
                String.valueOf(multiRunService.getTestStopTime())));

        // ссылка на Графану (TransportThreadPools)
        sbHtml.append(getLinkUrl(
                "Grafana - ППРБ TransportThreadPools",
                multiRunService.getGrafanaTransportThreadPoolsUrl(),
                String.valueOf(multiRunService.getTestStartTime()),
                String.valueOf(multiRunService.getTestStopTime())));

        // ссылка на Графану (ТС)
        sbHtml.append(getLinkUrl(
                "Grafana - ТС (БПМ модульный)",
                multiRunService.getGrafanaTsUrl(),
                String.valueOf(multiRunService.getTestStartTime()),
                String.valueOf(multiRunService.getTestStopTime())));

        // ссылка на Спланк
        sbHtml.append(getLinkUrl(
                "Splunk - Метрики BPM",
                multiRunService.getSplunkUrl(),
                String.valueOf(multiRunService.getTestStartTime()).substring(0, 10),
                String.valueOf(multiRunService.getTestStopTime()).substring(0, 10)));

        sbHtml.append("\n\t</body>" +
                "\n</html>");

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
                .replace("{startTime}", startTime)
                .replace("{stopTime}", stopTime));
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
        StringBuilder res = new StringBuilder("\n<h3>Версия модуля, активность хостов<h3>\n" +
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
            e.printStackTrace();
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
        double tps = 0;
        double tpsRs = 0;
        int vuCount = 0;
        int vuCountRs = 0;
        int vuStartIndex = 0;
        int step = 4999;
        while (multiRunService.getVuList().get(vuStartIndex).getIntValue() == 0 && vuStartIndex < multiRunService.getVuList().size()) {
            vuStartIndex++;
        }
        long startTime = multiRunService.getVuList().get(vuStartIndex).getTime();
        int vuCountMem = multiRunService.getVuList().get(vuStartIndex).getIntValue();
        vuStartIndex++;
        for (int v = vuStartIndex; v < multiRunService.getVuList().size(); v++) {
            if (multiRunService.getVuList().get(v).getIntValue() != vuCountMem) {
                long stopTime = multiRunService.getVuList().get(v).getTime() - 1;
                if ((stopTime - startTime) > 999) {
                    double tpsCur = 0, tpsCurRs = 0;
                    if ((stopTime - startTime) > step) {
                        // нужно найти стабильный максимум
                        CallMetrics callMetrics = multiRunService.getMetricsForPeriod(startTime, stopTime);
                        tpsCur = callMetrics.getTps();
                        tpsCurRs = callMetrics.getTpsRs();
                    } else {
                        CallMetrics callMetrics = multiRunService.getMetricsForPeriod(startTime, stopTime);
                        tpsCur = callMetrics.getTps();
                        tpsCurRs = callMetrics.getTpsRs();
                    }

                    if (tpsCur > tps) {
                        tps = tpsCur;
                        vuCount = vuCountMem;
                    }
                    if (tpsCurRs > tpsRs) {
                        tpsRs = tpsCurRs;
                        vuCountRs = vuCountMem;
                    }
                    startTime = stopTime + 1;
                    vuCountMem = multiRunService.getVuList().get(v).getIntValue();
                }
            }
        }
        tpsMax = tps;
        vuCountMax = vuCount;
        StringBuilder res = new StringBuilder("<table><tbody>\n" +
                "<tr><th rowspan=\"2\">Сервис</th>" +
                "<th colspan=\"2\">tps отправлено</th>" +
                "<th colspan=\"2\">tps response</th></tr>\n" +
                "<tr><th>max</th><th>VU</th><th>max</th><th>VU</th></tr>\n" +
                "<tr><td>");
        res.append(multiRunService.getName())
                .append("</td><td>")
                .append(decimalFormat.format(tps))
                .append("</td><td>")
                .append(decimalFormat.format(vuCount))
                .append("</td><td>")
                .append(decimalFormat.format(tpsRs))
                .append("</td><td>")
                .append(decimalFormat.format(vuCountRs))
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
                    if ((double) map.getValue() != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
