package ru.examples.jsoupExample;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class JsoupExample {
    public static void main(String[] args) throws IOException {
        String url = "http://www.cbr.ru/currency_base/daily/";
        Document doc = Jsoup.parse(new URL(url), 3000);
        Elements element = doc.select("table[class=\"data\"]");
        Elements elements = element.select("tr"); // ???
//        System.out.println(elements);
        int i;
        String currName;
        double curr;
        int kf;
        for (Element el : elements) {
            System.out.println(el);
            i = 0;
            currName = "";
            curr = 0.0000;
            kf = 0;
            Elements elements2 = el.select("td");
            for (Element el2 : elements2) {
                i++;
                switch (i) {
                    case 3:
                        kf = Integer.parseInt(el2.text());
                        break;
                    case 4:
                        currName = el2.text();
                        break;
                    case 5:
                        curr = Double.parseDouble(el2.text().replaceAll(",","."));
                        break;
                }
//                System.out.println(i + " : " + el2);
            }
            System.out.println("==============================================");
            System.out.println(currName + " " + (curr / kf));
            System.out.println("==============================================\r\n");
        }
    }
}
