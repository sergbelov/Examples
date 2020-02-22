package ru.examples.influxDbExample;

import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.impl.Preconditions;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.concurrent.TimeUnit;

public class InfluxDbExample {
    public static void main(String[] args) {

        String url = "http://localhost:8086";
        String username = "admin";
        String password = "admin";
        String dbName = "java-database";

        Preconditions.checkNonEmptyString(url, "url");

/*
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        InfluxDB influxDB0 = InfluxDBFactory.connect(url , username, password, builder);
*/

/*
        InfluxDB influxDB = InfluxDBFactory.connect(url,
                username,
                password,
                new OkHttpClient.Builder(),
                InfluxDB.ResponseFormat.JSON);
*/

        InfluxDB influxDB = InfluxDBFactory.connect(url,
                "admin",
                "password");

        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

//        influxDB.deleteDatabase("java-database");

        influxDB.createDatabase(dbName);
        influxDB.createRetentionPolicy(
                "one-year", dbName, "365d", 1, true);



        BatchPoints batchPoints = BatchPoints
                .database(dbName)
//                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();

        Point point2 = Point.measurement("memory")
                .time(System.currentTimeMillis() - 100, TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743696L)
                .addField("used", 1016096L)
                .addField("buffer", 1008467L)
                .build();

        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);
        influxDB.close();
    }
}
