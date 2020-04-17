package ru.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GrafanaService {
    private static final Logger LOG = LogManager.getLogger();
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat sdf4 = new SimpleDateFormat("yyyyMMdd");

    public GrafanaService() {
    }


    /**
     * url дашборда
     *
     * @param baseUrl
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getUrl(
            String baseUrl,
            long startTime,
            long stopTime) {
        return getUrl(
                baseUrl,
                String.valueOf(startTime),
                String.valueOf(stopTime));
    }

    /**
     * url дашборда
     * 
     * @param baseUrl
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getUrl(
            String baseUrl,
            String startTime,
            String stopTime) {
        return baseUrl
                .replace("{from}", startTime)
                .replace("{to}", stopTime);
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
    public String getLinkUrl(
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
    public String getLinkUrl(
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
     * График в формате PNG из Grafana
     * @param apiKey
     * @param url
     * @param startTime
     * @param stopTime
     * @param width
     * @param height
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String url,
            long startTime,
            long stopTime,
            int width,
            int height,
            String filePath,
            String fileName) {

        return getPngFromGrafana(
                apiKey,
                url,
                0,
                0,
                startTime,
                stopTime,
                width,
                height,
                filePath,
                fileName);
    }

    /**
     * График в формате PNG из Grafana
     * @param apiKey
     * @param url
     * @param startTime
     * @param stopTime
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String url,
            long startTime,
            long stopTime,
            String filePath,
            String fileName){

        return getPngFromGrafana(
                apiKey,
                url,
                0,
                0,
                startTime,
                stopTime,
                1000,
                500,
                filePath,
                fileName);
    }

    /**
     * График в формате PNG из Grafana
     * @param apiKey
     * @param url
     * @param panelId
     * @param startTime
     * @param stopTime
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String url,
            int panelId,
            long startTime,
            long stopTime,
            String filePath,
            String fileName) {

        return getPngFromGrafana(
                apiKey,
                url,
                0,
                panelId,
                startTime,
                stopTime,
                1000,
                500,
                filePath,
                fileName);
    }

    /**
     * График в формате PNG из Grafana
     * @param apiKey
     * @param url
     * @param orgId
     * @param panelId
     * @param startTime
     * @param stopTime
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String url,
            int orgId,
            int panelId,
            long startTime,
            long stopTime,
            String filePath,
            String fileName) {

        return getPngFromGrafana(
                apiKey,
                url,
                orgId,
                panelId,
                startTime,
                stopTime,
                1000,
                500,
                filePath,
                fileName);
    }

    /**
     * График в формате PNG из Grafana
     *
     * @param apiKey
     * @param url
     * @param orgId
     * @param panelId
     * @param startTime
     * @param stopTime
     * @param width
     * @param height
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafana(
            String apiKey,
            String url,
//            String host,
//            String dashboard,
            int orgId,
            int panelId,
            long startTime,
            long stopTime,
            int width,
            int height,
            String filePath,
            String fileName) {

//        startTime=1585206141833L; // отладка
//        stopTime=1585210187296L;

//        startTime = 1585059368000L;
//        stopTime = 1585060349000L;

//        stopTime = stopTime + 5 * 60000; // + 5 мин

        if (url != null && !url.isEmpty()) {
            fileName = fileName.replace(".png", "").replace(".PNG", "");
            fileName = fileName + "_" + sdf3.format(startTime) + "-" + sdf3.format(stopTime) + ".png";
            LOG.info("График из Grafana: {}", fileName);
            String fileFull = filePath + fileName;

            try {
                url = url.replace("{from}", String.valueOf(startTime))
                        .replace("{to}", String.valueOf(stopTime))
                        .replace("{orgId}", String.valueOf(orgId))
                        .replace("{panelId}", String.valueOf(panelId))
                        .replace("{width}", String.valueOf(width))
                        .replace("{height}", String.valueOf(height));

                LOG.debug("{}", url);

                URL urlGrafana = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlGrafana.openConnection();
                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestProperty("Encoding", currEncoding);
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
                    LOG.error("Ошибка при записи файла {} {}\n", fileFull, url, e);
                    fileName = null;
                }

            } catch (Exception e) {
                LOG.error("Ошибка при получении данных из Grafana {}\n", url, e);
                fileName = null;
            }

            if (fileName != null && !fileName.isEmpty()) {
                return fileName;
            }
        }
        return "";
    }


    /**
     * Ссылка на график в формате PNG из Grafana
     *
     * @param apiKey
     * @param url
     * @param startTime
     * @param stopTime
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafanaHtmlImg(
            String apiKey,
            String url,
            long startTime,
            long stopTime,
            String filePath,
            String fileName) {

        return getPngFromGrafanaHtmlImg(
                apiKey,
                url,
                0,
                0,
                startTime,
                stopTime,
                1000,
                500,
                filePath,
                fileName);
    }

    /**
     * Ссылка на график в формате PNG из Grafana
     *
     * @param apiKey
     * @param url
     * @param orgId
     * @param panelId
     * @param startTime
     * @param stopTime
     * @param width
     * @param height
     * @param filePath
     * @param fileName
     * @return
     */
    public String getPngFromGrafanaHtmlImg(
            String apiKey,
            String url,
            int orgId,
            int panelId,
            long startTime,
            long stopTime,
            int width,
            int height,
            String filePath,
            String fileName) {

        String filePng = getPngFromGrafana(
                apiKey,
                url,
                orgId,
                panelId,
                startTime,
                stopTime,
                width,
                height,
                filePath,
                fileName);

        if (filePng != null && !filePng.isEmpty()) {
            return "\n<div class=\"graph\"><p><img style=\"width:95%\" src=\"" + filePng + "\" alt=\"PNG\"/></p></div>\n";
        }
        return "";
    }


    /**
     * Создание каталога для сохранения отчетных данных
     * каталог для  отчета в формате:
     * базовый каталог / YYYYMMDD / YYYYMMDDHHMMSS(startime)
     * @param pathReport
     * @param startTime
     * @return путь к созданному каталогу
     */
    public String mkdirs(String pathReport, long startTime) {
        if (!pathReport.endsWith("/") && !pathReport.endsWith("\\")) {
            pathReport = pathReport + "/";
        }
        pathReport = pathReport + sdf4.format(startTime) + "/";
        pathReport = pathReport + sdf3.format(startTime) + "/";
        File path = new File(pathReport);
        if (!path.exists()) {
            path.mkdirs();
        }
        return pathReport;
    }

}
