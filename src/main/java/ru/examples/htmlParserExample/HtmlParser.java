package ru.examples.htmlParserExample;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.examples.htmlParserExample.data.awr.AwrTable;
import ru.examples.htmlParserExample.data.awr.AwrTableRow;

public class HtmlParser {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlParser.class);
    private String htmlString1;
    private String htmlString2;
    private List<AwrTable> awrTableList1 = new ArrayList<>();
    private List<AwrTable> awrTableList2 = new ArrayList<>();

    public static void main(String[] args) {
        String fileHtml1 = "C:/TEMP/AWR.html";
        String fileHtml2 = "C:/TEMP/AWR2.html";
        LOG.info("============================================================");
        HtmlParser htmlParser = new HtmlParser();

        htmlParser.setHTML1(htmlParser.readFile(fileHtml1));
        htmlParser.setHTML2(htmlParser.readFile(fileHtml2));

        List<AwrTable> awrTableList1 = htmlParser.getTables(htmlParser.getHTML1());
        for (AwrTable awrTable: awrTableList1){
            LOG.info(awrTable.getName());
            for (AwrTableRow row: awrTable.getRows()) {
                for (int i = 0; i < awrTable.getHeaders().size(); i++) {
                    LOG.info("{}: {}", awrTable.getHeaders().get(i), row.getRow().get(i));
                }
            }
        }
//        htmlParser.getTables(htmlParser.getHTML2());
    }

    public void setHTML1(String htmlString1) {
        this.htmlString1 = htmlString1;
    }

    public String getHTML1() {
        return htmlString1;
    }

    public void setHTML2(String htmlString2) {
        this.htmlString2 = htmlString2;
    }

    public String getHTML2() {
        return htmlString2;
    }

    public List<AwrTable> getTables(String html) {
        List<AwrTable> awrTableList = new ArrayList<>();
        Document doc1 = Jsoup.parse(html);
/*
        String body = doc1.body().text();
        String title = doc.title();
        String h1 = doc.body().getElementsByTag("h1").text();
        String table = doc.body().getElementsByTag("table").text();
*/

//        Elements elemTables = doc1.select("table[class=\"tdiff\"]");
//        Elements elemTables1 = elemTables.select("tr");
//        elemTables = doc2.select("table[class=\"tdiff\"]");
//        Elements elTables2 = elemTables.select("tr");

        int t = 0;
        Elements elemTables = doc1.select("[summary]");
        Elements elemTablesTr = elemTables.select("tr");
        List<String> thList = new ArrayList<>();
        List<AwrTableRow> rows = new ArrayList<>();
        String tableCaption = "";
        for (int e = 0; e < elemTablesTr.size(); e++) {
            Element elemTr = elemTablesTr.get(e);
            Elements elemTh1 = elemTr.select("th");
            if (elemTh1.size() > 0) { // новая таблица
                if (t > 0) {
                    awrTableList.add(new AwrTable(
                            tableCaption,
                            thList,
                            rows));
                }
                tableCaption = elemTables.get(t).attributes().get("summary");
                t++;
                LOG.info("{}", elemTablesTr.get(e));
                LOG.info("========== Table {}: {}", t, tableCaption);
                thList.clear();
                rows.clear();
                for (Element el : elemTh1) {
                    LOG.debug("Th: {}", el.text());
                    thList.add(el.text());
                }
            }
            Elements elemTd = elemTr.select("td");
            if (elemTd.size() == thList.size()) { // таблица без th не обработается
                AwrTableRow awrTableRow = new AwrTableRow();
                for (int c = 0; c < thList.size(); c++) {
                    LOG.debug("Td: {}", elemTd.get(c).text());
                    LOG.info("Table:{}; {}: {}", t, thList.get(c), elemTd.get(c).text());
                    awrTableRow.add(elemTd.get(c).text());
                }
                rows.add(awrTableRow);
            }
        }
        if (rows.size() > 0) {
            awrTableList.add(new AwrTable(
                    tableCaption,
                    thList,
                    rows));
        }
        return awrTableList;
    }

    /**
     * Читаем файл
     *
     * @param fileName
     */
    public String readFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName),
                        "cp1251"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                LOG.trace("{}", line);
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            LOG.error("Ошибка при чтении файла {}\n", fileName, e);
        }
        return stringBuilder.toString();
    }
}
