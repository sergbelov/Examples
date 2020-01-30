package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.ErrorGroupComment;
import ru.utils.load.data.ErrorRs;
import ru.utils.load.data.ErrorsRegx;
import ru.utils.files.FileUtils;

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
    private ErrorsRegx errorsRegx = new ErrorsRegx(); // типы ошибок (для группировки)

    public Report() {
    }

    /**
     * Сохраняем отчет в HTML - файл
     * @param multiRunService
     */
    public void saveReportHtml(MultiRunService multiRunService) {
//        fileUtils.writeFile("Reports/Report_" + sdf3.format(System.currentTimeMillis()) + ".txt", stringBuilder.toString());

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
                        "<h2>" + multiRunService.getName() + " (" + sdf2.format(multiRunService.getTestStartTime()) + " - " + sdf2.format(multiRunService.getTestStopTime()) + ")</h2>\n" +
                        multiRunService.getParams() + "\n");

        sbHtml.append("\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Running Vusers",
                        new String[]{"Running Vusers"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getVuList(),
                        true,
                        true,
                        "#0000ff"))
                .append("\n\t\t</div>\n");

        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "TPC",
                        new String[]{"TPC"},
                        multiRunService.getTestStartTime(),
                        multiRunService.getTpcList(),
                        false,
                        true,
                        "#009f9f"))
                .append("\n\t\t</div>\n");

        String[] lineTitle1 = {"Отправлено запросов", "COMPLETE", "RUNNING"};
        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Производительность ",
                        lineTitle1,
                        multiRunService.getTestStartTime(),
                        multiRunService.getBpmProcessStatisticList(),
                        false,
                        true))
                .append("\n\t\t</div>\n");

        String[] lineTitle2 = {"Минимальная длительность (мс)", "Средняя длительность (мс)", "Перцентиль 90% (мс)", "Максимальная длительность (мс)"};
        sbHtml.append("\n\t\t<div class=\"graph\">\n")
                .append(graph.getSvgGraphLine(
                        "Длительность выполнения",
                        lineTitle2,
                        multiRunService.getTestStartTime(),
                        multiRunService.getDurationList(),
                        false,
                        true))
                .append("\n\t\t</div>\n");

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
                            true,
                            "#ff0000"))
                    .append("\n\t\t</div>\n");
        }

        LOG.warn("Не прерывайте работы программы, пауза {} сек...", 10);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        multiRunService.getStatistics(multiRunService.getTestStartTime(), multiRunService.getTestStopTime());
        int lastIndex = multiRunService.getDurationList().size() - 1;
//        min, avg, prc90, max
        sbHtml.append("\n\t\t<div>\n" +
                "<table>\n" +
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
                        "</table>\n" +
                        "</div><br><br>\n");


        String sqlBpm = multiRunService.getDataFromSQL().getStatisticsFromBpm(
                multiRunService.getCallList(),
                multiRunService.getBpmProcessStatisticList(),
                multiRunService.getTestStartTime(),
                multiRunService.getTestStopTime());
        lastIndex = multiRunService.getBpmProcessStatisticList().size()-1;
        sbHtml.append("\n\t\t<div>\n")
                .append(sqlBpm)
                .append("<br><br>\n\n<table>\n" +
                        "<tr><td>Отправлено запросов</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndex).getIntValue(0))
                .append("</td></tr>\n" +
                        "<tr><td>БД - в статусе COMPLETE</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndex).getIntValue(1))
                .append("</td></tr>\n" +
                        "<tr><td>БД - в статусе RUNNING</td><td>")
                .append(multiRunService.getBpmProcessStatisticList().get(lastIndex).getIntValue(2))
                .append("</td></tr>\n" +
                        "</table>\n" +
                        "</div>\n");


        // сгруппируем ошибки по типам
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
        fileUtils.writeFile("Reports/" + multiRunService.getName() + "_" + sdf3.format(System.currentTimeMillis()) + ".html", sbHtml.toString());
    }


    /**
     * Группировка ошибок по типу
     */
    public String getErrorsGroupComment(
            List<ErrorRs> errorList,
            List<ErrorGroupComment> errorGroupCommentList
    ){
        StringBuilder sbErrors = new StringBuilder("\n<br><div>\nОшибки<br>\n<table>\n");
        for (int i = 0; i < errorList.size(); i++){
            int find1;
            String text = errorList.get(i).getText();
            if ((find1 = findErrorRegx(text)) > -1){
                String comment = errorsRegx.getComment(find1);
                int find2;
                if ((find2 = findErrorGroupCommentList(errorGroupCommentList, comment)) > -1){
                    errorGroupCommentList.get(find2).incCount();
                } else {
                    errorGroupCommentList.add(new ErrorGroupComment(comment, 1));
                }
            } else {
                sbErrors.append("<tr>")
                        .append("<td>")
                        .append(text)
                        .append("</td>")
                        .append("<td>")
                        .append("1")
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
                    .append("</tr>\n");
        }

        sbErrors.append("</table>\n</div>\n");
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
        for (int i = 0; i < errorsRegx.getCount(); i++){
            int find = 0;
            int count = errorsRegx.getRegx(i).length;
            for (int j = 0; j < count; j++){
                if (text.indexOf(errorsRegx.getRegx(i)[j]) > -1){
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
//        http://grafana/d/jtiKjshWk/pprb-khosty-detalizirovanno?orgId=25&var-DS=Izanagi37API&var-GROUP=All&var-HOST=amaterasu210&var-HOST=vck-s057-gri001&var-HOST=vck-s057-gri002&var-HOST=vck-s057-gri003&var-HOST=vck-s057-gri004&var-APPS=All&from=1580281500000&to=1580282100000
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
//        http://10.116.159.78:8000/en-GB/app/BPM/_bpm?form.time.earliest=1580284800&form.time.latest=1580288430&form.periodCompression=1m&form.index=bpm_modul
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
}
