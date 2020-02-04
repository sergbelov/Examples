package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.utils.files.FileUtils;
import ru.utils.load.data.Call;
import ru.utils.load.data.ErrorGroupComment;
import ru.utils.load.data.ErrorRs;
import ru.utils.load.data.ErrorsGroup;

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

    public Report() {
    }

    /**
     * Сохраняем отчет в HTML - файл
     * @param multiRunService
     */
    public void saveReportHtml(MultiRunService multiRunService) {
        saveReportHtml(multiRunService, false);
    }
    /**
     * Сохраняем отчет в HTML - файл
     * @param multiRunService
     */
    public void saveReportHtml(MultiRunService multiRunService, boolean printMetrics) {

        LOG.info("Формируем отчет...");
        // формируем HTML - файл
        StringBuilder sbHtml = new StringBuilder(
                "<html>\n" +
                        "\t<head>\n" +
                        "\t\t<meta charset=\"UTF-8\">\n" +
                        "\t\t<style>\n" +
                        "\t\t\tbody, html{width:100%; height:100%; margin:0; background:#fdfdfd}\n\n" +
                        "\t\t\t.graph{width:80%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:50px auto; border:1px solid #ccc; background:#fff}\n\n" +
                        "\t\t\ttable{border: solid 1px; border-collapse: collapse;}\n" +
                        "\t\t\ttd{border: solid 1px;}\n" +
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
                        "#0000ff"))
                .append("\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "TPC",
                        new String[]{"TPC - отправлено", "TPC - выполнено"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getTpcList(),
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Производительность БПМ",
                        new String[]{"Отправлено запросов", "COMPLETE", "RUNNING"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getBpmProcessStatisticList(),
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Длительность выполнения",
                        new String[]{"Минимальная длительность (мс)", "Средняя длительность (мс)", "Перцентиль 90% (мс)", "Максимальная длительность (мс)"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getDurationList(),
                        false,
                        printMetrics))
                .append("\t\t</div>\n");

        // выведем ошибки при наличии
        boolean printError = false;
        for (int i = 0; i < multiRunService.getErrorGroupList().size(); i++){
            if (multiRunService.getErrorGroupList().get(i).getValue() > 0){
                printError = true;
                break;
            }
        }
        if (printError) {
            sbHtml.append("\n\t\t<div class=\"graph\">\n")
                    .append(graph.getSvgGraphLine(
                            "Ошибки",
                            new String[]{"Ошибки"},
                            multiRunService.getTestStartTime(),
                            multiRunService.getErrorGroupList(),
                            false,
                            printMetrics,
                            "#ff0000"))
                    .append("\t\t</div>\n");
        }

        multiRunService.getStatistics(multiRunService.getTestStartTime(), multiRunService.getTestStopTime());
        int lastIndex = multiRunService.getDurationList().size() - 1;
//        min, avg, prc90, max
        sbHtml.append("\n\t\t<div>\n" +
                "<table><tbody>\n" +
                 "<tr><td>Минимальная длительность (мс)</td><td>")
                .append(multiRunService.getDurationList().get(lastIndex).getIntValue(0))
                .append("</td></tr>\n" +
                        "<tr><td>Средняя длительность (мс)</td><td>")
                .append(multiRunService.getDurationList().get(lastIndex).getIntValue(1))
                .append("</td></tr>\n" +
                        "<tr><td>Перцентиль 90%(мс)</td><td>")
                .append(multiRunService.getDurationList().get(lastIndex).getIntValue(2))
                .append("</td></tr>\n" +
                        "<tr><td>Максимальная длительность(мс)</td><td>")
                .append(multiRunService.getDurationList().get(lastIndex).getIntValue(3))
                .append("</td></tr>\n" +
                        "</tbody></table>\n" +
                        "</div><br><br>\n");


        String sqlBpm = multiRunService.getDataFromSQL().getStatisticsFromBpm(
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime(),
                multiRunService.getCallList(),
                multiRunService.getBpmProcessStatisticList());
        lastIndex = multiRunService.getBpmProcessStatisticList().size()-1;
        sbHtml.append("\n\t\t<div>\n")
                .append(sqlBpm)
                .append("<br><br>\n\n<table><tbody>\n" +
                        "<tr><td>Отправлено запросов</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndex).getIntValue(0))
                .append("</td></tr>\n" +
                        "<tr><td>БД БПМ - в статусе COMPLETE</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndex).getIntValue(1))
                .append("</td></tr>\n" +
                        "<tr><td>БД БПМ - в статусе RUNNING</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndex).getIntValue(2))
                .append("</td></tr>\n" +
                        "</tbody></table>\n" +
                        "</div>\n");

/*
        // список не исполненных запросов
        sbHtml.append(getRunningRequests(multiRunService.getCallList()));
*/


        // группируем ошибки по типам
        if (printError) {
            sbHtml.append(getErrorsGroupComment(
                    multiRunService.getErrorList(),
                    multiRunService.getErrorGroupCommentList()));
        }

        // ссылка на Графану (Хосты детализировано)
        sbHtml.append(getGrafanaHostsDetailUrl(
                multiRunService.getGrafanaHostsDetailUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime()));

        // ссылка на Спланк
        sbHtml.append(getSplunkUrl(
                multiRunService.getSplunkUrl(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime()));

        sbHtml.append("\n<br>\n\t</body>" +
                "\n</html>");

        // сохраняем HTML - файл
        String fileName = "Reports/" + multiRunService.getName() + "_" +
                sdf3.format(multiRunService.getTestStartTime()) + "-" +
                sdf3.format(multiRunService.getTestStopTime()) + ".html";
        fileUtils.writeFile(fileName, sbHtml.toString());
        LOG.info("Сформирован отчет {}", fileName);
    }

    /**
     * Список не исполненных запросов
     * @param callList
     * @return
     */
    private String getRunningRequests(List<Call> callList) {
        StringBuilder res = new StringBuilder();
        int cnt = 0;
        for (int i = 0; i < callList.size(); i++){
            if (callList.get(i).getDuration() == 0){
                res.append("<tr><td>")
                        .append(++cnt)
                        .append("</td><td>")
                        .append(sdf1.format(callList.get(i).getTimeBegin()))
                        .append("</td></tr>\n");
            }
        }
        if (res.length() > 0){
            return "<br>\nНе исполненные запросы<br>\n" +
                    "<table>" + res.toString() + "</table>\n";
        } else return "";
    }


    /**
     * Группировка ошибок по типу
     */
    public String getErrorsGroupComment(
            List<ErrorRs> errorList,
            List<ErrorGroupComment> errorGroupCommentList
    ){
        StringBuilder sbErrors = new StringBuilder("\n<br><div>\nОшибки<br>\n<table><tbody>\n" +
                "<tr><td>Группа ошибок</td>" +
                "<td>Количество</td>" +
                "<td>Первая ошибка из группы</td></tr>");

        for (int i = 0; i < errorList.size(); i++){
            int find1;
            String text = errorList.get(i).getText();
            if ((find1 = findErrorRegx(text)) > -1){
                String comment = errorsGroup.getComment(find1);
                int find2;
                if ((find2 = findErrorGroupCommentList(errorGroupCommentList, comment)) > -1){
                    errorGroupCommentList.get(find2).incCount();
                } else {
                    errorGroupCommentList.add(new ErrorGroupComment(text, comment, 1));
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
        for (int i = 0; i < errorGroupCommentList.size(); i++){
            sbErrors.append("<tr>")
                    .append("<td>")
                    .append(errorGroupCommentList.get(i).getComment())
                    .append("</td>")
                    .append("<td>")
                    .append(errorGroupCommentList.get(i).getCount())
                    .append("</td>")
                    .append("<td>")
                    .append(errorGroupCommentList.get(i).getFirstError())
                    .append("</td>")
                    .append("</tr>\n");
        }

        sbErrors.append("</tbody></table>\n</div>\n");
        return sbErrors.toString();
    }

    /**
     * Поиск зафиксированной группы
     * @param comment
     * @return
     */
    private int findErrorGroupCommentList(
            List<ErrorGroupComment> errorGroupCommentList,
            String comment){
        int res = -1;
        for (int i = 0; i < errorGroupCommentList.size(); i++){
            if (errorGroupCommentList.get(i).getComment().equals(comment)){
                res = i;
                break;
            }
        }
        return res;
    }

    /**
     * Поиск типа ошибки
     * @param text
     * @return
     */
    private int findErrorRegx(String text){
        int res = -1;
        for (int i = 0; i < errorsGroup.getCount(); i++){
            int find = 0;
            int count = errorsGroup.getRegx(i).length;
            for (int j = 0; j < count; j++){
                if (text.indexOf(errorsGroup.getRegx(i)[j]) > -1){
                    find++;
                }
            }
            if (find == count){
                res = i;
                break;
            }
        }
        return res;
    }

    /**
     * Формирование URL для просмотра в Графане (Хосты детелизировано)
     * @param startTime
     * @param stopTime
     * @return
     */
    private String getGrafanaHostsDetailUrl(String grafanaHostsDetailUrl, long startTime, long stopTime){
        StringBuilder res = new StringBuilder("\n<div>" +
                "<p><a href=\"");
        res.append(grafanaHostsDetailUrl
                    .replace("{startTime}", String.valueOf(startTime))
                    .replace("{stopTime}", String.valueOf(stopTime)));
        res.append("\" target=\"_blank\">Grafana - ППРБ Хосты детализированно</a>" +
                "</p></div>\n");
        LOG.debug("Ссылка на Grafana {}", res.toString());
        return res.toString();
    }

    private String getSplunkUrl(String splunkUrl, long startTime, long stopTime){
        StringBuilder res = new StringBuilder("<div>" +
                "<p><a href=\"");
        res.append(splunkUrl
                .replace("{startTime}", String.valueOf(startTime).substring(0, 10))
                .replace("{stopTime}", String.valueOf(stopTime).substring(0,10)));
        res.append("\" target=\"_blank\">Splunk - Метрики BPM</a>" +
                "</p></div>\n");
        LOG.debug("Ссылка на Splunk {}", res.toString());
        return res.toString();
//        1580286544
    }

    /**
     * Информация о версии модуля и активности хостов
     * @param csmUrl
     * @return
     */
    private String getInfoFromCSM(String csmUrl){
        StringBuilder res = new StringBuilder("\n<h3>Версия модуля, активность хостов<h3>\n" +
                "<table><tbody>\n" +
                "<tr><td>Host</td><td>Module</td><td>Version</td><td>Active</td></tr>\n");
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
            for (int h = 0; h < jsonArray.length(); h++){
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
                        .append("</td>")
                        .append("<td>")
                        .append(enabled ? "Да" : "Нет")
                        .append("</td>")
                        .append("</tr>\n");

                LOG.debug("{} {} {} {}", host, module, version, enabled);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        res.append("</tbody></table>\n");
        return res.toString();
    }
}
