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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class Report {
    private static final Logger LOG = LogManager.getLogger(Report.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private Graph graph = new Graph();
    private FileUtils fileUtils = new FileUtils();
    private ErrorsGroup errorsGroup = new ErrorsGroup(); // типы ошибок (для группировки)

    /**
     * Сохраняем отчет в HTML - файл
     *
     * @param multiRunService
     */
    public void saveReportHtml(
            MultiRunService multiRunService,
            String pathReport) {
        saveReportHtml(multiRunService, pathReport, false);
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

        LOG.info("Формируем отчет...");
        // формируем HTML - файл
        StringBuilder sbHtml = new StringBuilder(
                "<html>\n" +
                        "\t<head>\n" +
                        "\t\t<meta charset=\"UTF-8\">\n" +
                        "\t\t<style>\n" +
                        "\t\t\tbody, html{width:100%; height:100%; margin:0; background:#fdfdfd}\n\n" +
                        "\t\t\t.graph{width:80%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:50px auto; border:1px; solid #ccc; background:#fff}\n\n" +
                        "\t\t\ttable{border: solid 1px; border-collapse: collapse;}\n" +
                        "\t\t\ttd{border: solid 1px;}\n" +
                        "\t\t\tth{border: solid 1px; background: #dfdfdf;}\n" +
                        "\t\t\t.td_red{border: solid 1px; background-color: rgb(255, 192, 192);}\n" +
                        "\t\t\t.td_green{border: solid 1px; background-color: rgb(192, 255, 192);}\n" +
                        "\t\t\t.td_yellow{border: solid 1px; background-color: rgb(255, 255, 192);}\n" +
                        "\t\t</style>\n" +
                        "\t</head>\n" +
                        "\t<body>\n" +
                        "<h2>" + multiRunService.getName() + " (" + sdf2.format(multiRunService.getTestStartTime()) + " - " + sdf2.format(multiRunService.getTestStopTime()) + ")</h2>\n");

        // информация по версиям модуля и активности хостов
        sbHtml.append(getInfoFromCSM(multiRunService.getCsmUrl()));

        // параметры
        sbHtml.append(multiRunService.getParams());


        sbHtml.append("\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Running Vusers",
                        new String[]{"Running Vusers"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getVuList(),
                        true,
                        printMetrics,
                        "#0000ff",
                        multiRunService))
                .append("\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Количество операций в секунду (TPC)",
                        new String[]{"TPC - отправлено", "TPC - выполнено"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getTpcList(),
                        false,
                        printMetrics,
                        multiRunService))
                .append("\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Производительность БПМ",
                        new String[]{"Отправлено запросов", "COMPLETE", "RUNNING"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getBpmProcessStatisticList(),
                        false,
                        printMetrics,
                        multiRunService))
                .append("\t\t</div>\n");

        String sqlBpm = multiRunService.getDataFromSQL().getStatisticsFromBpm(
                multiRunService.getKeyBpm(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime(),
                multiRunService.getCallList(),
                multiRunService.getBpmProcessStatisticList());
        int lastIndexBpm = multiRunService.getBpmProcessStatisticList().size() - 1;
        sbHtml.append("<!-- Статистика из БД БПМ  -->\n")
                .append(sqlBpm)
                .append("<br><br>\n\t\t<div>\n<table><tbody>\n" +
                        "<tr><th>Сервис</th>\n" +
                        "<th>Всего отправлено</th>\n" +
                        "<th>COMPLETE</th>\n" +
                        "<th>RUNNING</th>\n" +
                        "<th>Потеряно</th>\n")
                .append("<tr><td>")
                .append(multiRunService.getName())
                .append("</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(0))
                .append("</td>");

        if (multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(1) > 0) {
            sbHtml.append("<td class=\"td_green\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(1))
                .append("</td>");

        if (multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(2) > 0) {
            sbHtml.append("<td class=\"td_yellow\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(2))
                .append("</td>");

        if ((multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(1) +
                multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(2)) !=
                multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(0)) {
            sbHtml.append("<td class=\"td_red\">");
        } else {
            sbHtml.append("<td>");
        }
        sbHtml.append(multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(0) -
                (multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(1) +
                 multiRunService.getBpmProcessStatisticList().get(lastIndexBpm).getIntValue(2)))
                .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Длительность выполнения",
                        new String[]{"Минимальная длительность (мс)", "Средняя длительность (мс)", "Перцентиль 90% (мс)", "Максимальная длительность (мс)"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getDurationList(),
                        false,
                        printMetrics,
                        multiRunService))
                .append("\t\t</div>\n");

        multiRunService.getStatistics(multiRunService.getTestStartTime(), multiRunService.getTestStopTime());
        int lastIndexDur = multiRunService.getDurationList().size() - 1;
        sbHtml.append("<!-- Статистика по длительности выполнения сервиса -->\n" +
                "\t\t<div>\n<table><tbody>\n" +
                "<tr><th rowspan=\"2\">Сервис</th>\n" +
                "<th colspan=\"4\">Длительность выполнения (мс)</th>\n" +
                "<th colspan=\"3\">Количество</th></tr>\n" +
                "<tr><th>минимальная</th>\n" +
                "<th>средняя</th>\n" +
                "<th>перцентиль 90%</th>\n" +
                "<th>максимальная</th>\n" +
                "<th>всего</th>\n" +
                "<th>с ответом</th>\n" +
                "<th>без ответа</th>\n")
                .append("<tr><td>")
                .append(multiRunService.getName())
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(0))
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(1))
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(2))
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(3))
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(4))
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(5))
                .append("</td><td>")
                .append(multiRunService.getDurationList().get(lastIndexDur).getIntValue(4) - multiRunService.getDurationList().get(lastIndexDur).getIntValue(5))
                .append("</td></tr>\n</tbody></table>\n\t\t</div>\n");

        // выведем ошибки при наличии
        boolean printError = false;
        for (int i = 0; i < multiRunService.getErrorGroupList().size(); i++) {
            if (multiRunService.getErrorGroupList().get(i).getValue() > 0) {
                printError = true;
                break;
            }
        }
        if (printError) {
            sbHtml.append("<!-- Ошибки -->\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine(
                            "Ошибки",
                            new String[]{"Ошибки"},
                            multiRunService.getTestStartTime(),
                            multiRunService.getErrorGroupList(),
                            false,
                            printMetrics,
                            "#ff0000",
                            multiRunService))
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
        LOG.info("Сформирован отчет {}", fileName);
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
        StringBuilder sbErrors = new StringBuilder("\n<br><div>\nОшибки<br>\n<table><tbody>\n" +
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
                    res.append("<td class=\"td_green\">Да</td>");
                } else {
                    res.append("<td class=\"td_red\">Нет</td>");
                }

                res.append("</tr>\n");

                LOG.debug("{} {} {} {}", host, module, version, enabled);
            }

        } catch (Exception e) {
            LOG.error("Ошибка при получении данных с CSM\n", e);
        }
        res.append("</tbody></table>\n");
        return res.toString();
    }
}
