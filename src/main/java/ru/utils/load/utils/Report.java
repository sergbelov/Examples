package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.utils.files.FileUtils;
import ru.utils.load.data.Call;
import ru.utils.load.data.errors.ErrorRsGroup;
import ru.utils.load.data.errors.ErrorRs;
import ru.utils.load.data.errors.ErrorsGroup;
import ru.utils.load.data.graph.VarInList;
import ru.utils.load.data.metrics.Metrics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.*;
import java.util.List;

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

    /**
     * Сохраняем отчет в HTML - файл
     *
     * @param multiRunService
     */
    public void saveReportHtml(
            MultiRunService multiRunService,
            String pathReport) {
        saveReportHtml(multiRunService, pathReport, false);
//        saveReportHtml(multiRunService, pathReport, true);
    }

    /**
     * Сохраняем отчет в HTML - файл
     *
     * @param multiRunService
     */
    public void saveReportHtml(
            MultiRunService multiRunService,
            String pathReport,
            boolean printMetrics) {

        this.multiRunService = multiRunService;

        /* ==== графики
            0 - VU
            1 - Response time
            2 - TPS
            3 - Статистика из БД БПМ
            4 - Ошибки
        */

        /* ==== список метрик:
            0  - durMin
            1  - durAvg
            2  - dur90
            3  - durMax
            4  - tps
            5  - tpsRs
            6  - countCall
            7  - countCallRs
            8  - dbCompleted
            9  - dbRunning
            10 - dbLost
            11 - errors
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
                        "<h2>" + multiRunService.getName() +
                        " период " + sdf2.format(multiRunService.getTestStartTime()) +
                        " - " + sdf2.format(multiRunService.getTestStopTime()) + " (" +
                        timeMillisToString(multiRunService.getTestStartTime(), multiRunService.getTestStopTime()) +
                        ")</h2>\n");

        // информация по версиям модуля и активности хостов
        sbHtml.append(getInfoFromCSM(multiRunService.getCsmUrl()));

        // параметры
        sbHtml.append(multiRunService.getParams());

        // старт VU
        sbHtml.append("\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine("Running Vusers",
                        multiRunService,
                        multiRunService.getVuList(),
                        true,
                        printMetrics))
                .append("\t\t</div>\n");

        // Response time
        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine("Response time",
                        multiRunService,
                        multiRunService.getMetricsList(),
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        sbHtml.append("<!-- Статистика по длительности выполнения сервиса -->\n" +
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
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DurMin.getNum())))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DurAvg.getNum())))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.Dur90.getNum())))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DurMax.getNum())))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCallRs.getNum())))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum()) -
                        multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCallRs.getNum())))
                .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

        // TPS
        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine("Количество запросов в секунду (tps)",
                        multiRunService,
                        multiRunService.getMetricsList(),
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        sbHtml.append(getTpsAvg());

        // Статистика из БД БПМ
        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine("Статистика из БД БПМ",
                        multiRunService,
                        multiRunService.getMetricsList(),
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        sbHtml.append("<!-- Статистика из БД БПМ  -->\n")
//                .append(multiRunService.getSqlSelect())
                .append("\t\t<div>\n<table><caption>Статистика из БД БПМ<br>\n" +
                        multiRunService.getSqlSelect() +
                        "</caption><tbody>\n" +
                        "<tr><th>Сервис</th>\n" +
                        "<th>Отправлено</th>\n" +
                        "<th>COMPLETED</th>\n" +
                        "<th>RUNNING</th>\n" +
                        "<th>Потеряно</th>\n")
                .append("<tr><td>")
                .append(multiRunService.getName())
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum()) ==
            multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())) {
            sbHtml.append("<td class=\"td_green\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum())))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum()) > 0) {
            sbHtml.append("<td class=\"td_yellow\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum())))
                .append("</td>");

        if ((multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum()) +
                multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum())) !=
                multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())) {
            sbHtml.append("<td class=\"td_red\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum()) -
                (multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum()) +
                        multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum()))))
                .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

        // дубли в БД БПМ
        sbHtml.append(multiRunService.getDataFromDB().getDoubleCheck(multiRunService.getTestStartTime(), multiRunService.getTestStopTime()));

        // ошибки (при наличии)
        boolean printError = (multiRunService.getErrorList().size() > 0) ? true : false;
        if (printError) {
            sbHtml.append("<!-- Ошибки -->\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine("Ошибки",
                            multiRunService,
                            multiRunService.getMetricsList(),
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

        // сравнительная таблица для confluence
        sbHtml.append("<br><table><caption>Сравнительная таблица</caption><tbody>\n<tr>" +
                "<th rowspan=\"2\">Количество<br>узлов</th>" +
                "<th rowspan=\"2\">Сервис</th>" +
                "<th rowspan=\"2\">Отправлено<br>запросов</th>" +
                "<th colspan=\"3\">Процессы в БД</th>" +
                "<th rowspan=\"2\">tps max</th>" +
                "<th rowspan=\"2\">90<br>перцентиль</th>" +
                "<th rowspan=\"2\">CPU<br>core max<br>(%)</th>" +
                "<th rowspan=\"2\">Подробно</th></tr\n>" +
                "<tr><th>в статусе<br>COMPETED</th>" +
                "<th>в статусе<br>RUNNING</th>" +
                "<th>потеряно</th>" +
                "</tr>\n<tr><td>");
        sbHtml.append(countActiveHost)
                .append("</td><td>")
                .append(multiRunService.getName())
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum()) ==
            multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())) {
            sbHtml.append("<td class=\"td_green\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum())))
                .append("</td>");

        if (multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum()) > 0) {
            sbHtml.append("<td class=\"td_yellow\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum())))
                .append("</td>");

        if ((multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum()) +
             multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum())) !=
             multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum())) {
            sbHtml.append("<td class=\"td_red\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(decimalFormat.format(
                multiRunService.getMetricsList().get(0).getIntValue(VarInList.CountCall.getNum()) -
                (multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbCompleted.getNum()) +
                 multiRunService.getMetricsList().get(0).getIntValue(VarInList.DbRunning.getNum()))))
                .append("</td><td>")
                .append(decimalFormat.format(tpsMax))
                .append("</td><td>")
                .append(decimalFormat.format(multiRunService.getMetricsList().get(0).getIntValue(VarInList.Dur90.getNum())))
                .append("</td><td></td><td></td>")
                .append("</tr>\n</tbody></table>\n");

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
            if (callList.get(i).getDuration() == 0) {
                res.append("<tr><td>")
                        .append(++cnt)
                        .append("</td><td>")
                        .append(sdf1.format(callList.get(i).getTimeBegin()))
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
                if ((find1 = findErrorGroup(text)) > -1) {
                    String comment = errorsGroup.getComment(find1);
                    int find2;
                    if ((find2 = findErrorGroupCommentList(errorRsGroupList, comment)) > -1) {
                        errorRsGroupList.get(find2).incCount();
                    } else {
                        errorRsGroupList.add(new ErrorRsGroup(text, comment, 1));
                    }
                } else {
                    sbErrors.append("<tr>")
                            .append("<td>not group</td>")
                            .append("<td>1</td>")
                            .append("<td>")
                            .append(text)
                            .append("</td>")
                            .append("</tr>\n");
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
        int step = 5000;
        while (multiRunService.getVuList().get(vuStartIndex).getIntValue() == 0 && vuStartIndex < multiRunService.getVuList().size()) {
            vuStartIndex++;
        }
        long startTime = multiRunService.getVuList().get(vuStartIndex).getTime();
        int vuCountMem = multiRunService.getVuList().get(vuStartIndex).getIntValue();
        vuStartIndex++;
        for (int v = vuStartIndex; v < multiRunService.getVuList().size(); v++) {
            if (multiRunService.getVuList().get(v).getIntValue() != vuCountMem) {
                long stopTime = multiRunService.getVuList().get(v).getTime()-1;
                if ((stopTime - startTime) > 999) {
                    double tpsCur = 0, tpsCurRs = 0;
                    if ((stopTime - startTime) > step) {
                        // нужно найти стабильный максимум
                        Metrics metrics = multiRunService.getMetricsForPeriod(startTime, stopTime);
                        tpsCur = metrics.getTps();
                        tpsCurRs = metrics.getTpsRs();
                    } else {
                        Metrics metrics = multiRunService.getMetricsForPeriod(startTime, stopTime);
                        tpsCur = metrics.getTps();
                        tpsCurRs = metrics.getTpsRs();
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
        StringBuilder res = new StringBuilder("<table><tbody>\n" +
                "<tr><th rowspan=\"2\">Сервис</th>" +
                "<th colspan=\"2\">tps отправлено</th>" +
                "<th colspan=\"2\">tps выполнено</th></tr>\n" +
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


}
