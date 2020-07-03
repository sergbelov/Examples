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

public class HtmlParser {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlParser.class);
    private String htmlString1;
    private String htmlString2;

    public static void main(String[] args) {
        String fileHtml1 = "C:/TEMP/AWR.html";
        String fileHtml2 = "C:/TEMP/AWR2.html";
        LOG.info("============================================================");
        HtmlParser htmlParser = new HtmlParser();

        htmlParser.setHTML1(htmlParser.readFile(fileHtml1));
        htmlParser.setHTML2(htmlParser.readFile(fileHtml2));

        String body = htmlParser.getTable(
                htmlParser.getHTML1(),
                htmlParser.getHTML2());

        LOG.info(body);
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


    public String getTable(String html1, String html2){
        Document doc1 = Jsoup.parse(html1);
        Document doc2 = Jsoup.parse(html2);

/*
        String body = doc1.body().text();
        String title = doc.title();
        String h1 = doc.body().getElementsByTag("h1").text();
        String table = doc.body().getElementsByTag("table").text();

        LOG.info("Input HTML String to JSoup : {}", htmlString);
        LOG.info("After parsing, Title : {}", title);
        LOG.info("Afte parsing, Heading : {}", h1);
        LOG.info("{}", table);
*/

//        Elements elemTables = doc1.select("table[class=\"tdiff\"]");
//        Elements elemTables1 = elemTables.select("tr");
//        elemTables = doc2.select("table[class=\"tdiff\"]");
//        Elements elTables2 = elemTables.select("tr");

        Elements elemTables1 = doc1.select("[summary]");
        Elements elemTablesTr1 = elemTables1.select("tr");

        Elements elemTables2 = doc2.select("[summary]");
        Elements elemTablesTr2 = elemTables2.select("tr");

        int t = 0;
        List<String> thList = new ArrayList<>();
        String tableCaption = "";
        for (int e = 0; e < elemTablesTr1.size(); e++){
            //ToDo
            tableCaption = elemTables1.get(e).attributes().get("summary");
            Element el1 = elemTablesTr1.get(e);

            Element el2 = null; //elemTables1.get(e);
            for (Element el: elemTables2) {
                if (tableCaption.equals(el.attributes().get("summary"))){
                    el2 = el;
                    break;
                }
            }
            LOG.debug("el:\n{}", el1);

            Elements elemTh1 = el1.select("th");
            Elements elemTh2 = el2.select("th");
            if (elemTh1.size() > 0 && elemTh2.size() == elemTh1.size()) { // новая таблица
                t++;
                LOG.info("{}", elemTables1.get(e));
                tableCaption = elemTables1.get(e).attributes().get("summary");
                LOG.info("========== Table {}: {}", t, tableCaption);
                thList.clear();
                for (Element el: elemTh1) {
                    LOG.debug("Th: {}", el.text());
                    thList.add(el.text());
                }
            }

            Elements elemTd1 = el1.select("td");
            Elements elemTd2 = el2.select("td");
            if (elemTd1.size() == thList.size() && elemTd2.size() == thList.size()) { // таблица без th не обработается
                for (int c = 0; c < thList.size(); c++) {
                    LOG.debug("Td: {}", elemTd1.get(c).text());
                    LOG.info("Table:{}; {}: {} | {}", t, thList.get(c), elemTd1.get(c).text(), elemTd2.get(c).text());
                }
            }

        }

        return "";
    }

    /**
     * Читаем файл
     * @param fileName
     */
    public String readFile(String fileName){
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
