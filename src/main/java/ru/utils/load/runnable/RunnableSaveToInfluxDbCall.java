package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.TimeUnit;

/**
 * Created by Belov Sergey
 */
public class RunnableSaveToInfluxDbCall implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableSaveToInfluxDbCall.class);
    private long start;
    private Long dur = null;
    private MultiRunService multiRunService;
    private InfluxDB influxDB;
    private int thread;

    public RunnableSaveToInfluxDbCall(
            long start,
            Long stop,
            MultiRunService multiRunService,
            int thread
    ) {
        this.start = start;
        this.multiRunService = multiRunService;
        this.influxDB = multiRunService.getInfluxDB();
        this.thread = thread;
        if (stop != null){
            this.dur = stop - start;
        }
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        int num = multiRunService.getNumberRequest().incrementAndGet();
        if (influxDB != null) {
            try {
                Point point = null;
                if (dur != null) {
                    point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                            .time(start, TimeUnit.MILLISECONDS)
                            .tag("type", "call")
                            .tag("thread", String.valueOf(thread))
                            .tag("api", multiRunService.getName())
                            .tag("key", multiRunService.getProcessDefinitionKey())
                            .addField("i", 1)
                            .addField("duration", dur)
                            .build();
                } else {
                    point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                            .time(start, TimeUnit.MILLISECONDS)
                            .tag("type", "call")
                            .tag("thread", String.valueOf(thread))
                            .tag("api", multiRunService.getName())
                            .tag("key", multiRunService.getProcessDefinitionKey())
                            .addField("i", 1)
                            .build();
                }
//                influxDB.write(point);

                BatchPoints batchPoints = BatchPoints
                        .database(multiRunService.getInfluxDbBaseName())
    //                    .retentionPolicy("defaultPolicy")
                        .build();
                batchPoints.point(point);
                influxDB.write(batchPoints);

            } catch (Exception e) {
                LOG.error("Ошибка при сохранении метрик в InfluxDB\n", e);
            }
        }
        threadNum = multiRunService.stopThread();
    }
}
