package ru.utils.load.data;

public class GrafanaData {
    private String name; // наименование дашборда
    private String file; // наименование файла PNG
    private String urlGraph; // ссылка на URL Grafana
    private String urlPNG; // ссылка на PNG

    public GrafanaData() {
    }

    public GrafanaData(String name, String file, String urlGraph, String urlPNG) {
        this.name = name;
        this.file = file;
        this.urlGraph = urlGraph;
        this.urlPNG = urlPNG;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getUrlGraph() {
        return urlGraph;
    }

    public String getUrlPNG() {
        return urlPNG;
    }
}
