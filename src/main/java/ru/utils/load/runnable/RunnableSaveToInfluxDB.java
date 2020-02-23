package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Belov Sergey
 */
public class RunnableSaveToInfluxDB implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableSaveToInfluxDB.class);
    private int num;
    private long start;
    private Long dur = null;
    private MultiRunService multiRunService;
    private InfluxDB influxDB;

    public RunnableSaveToInfluxDB(
            int num,
            long start,
            Long stop,
            MultiRunService multiRunService
    ) {
        this.num = num;
        this.start = start;
        this.multiRunService = multiRunService;
        this.influxDB = multiRunService.getInfluxDB();
        if (stop != null){
            this.dur = stop - start;
        }
    }

    @Override
    public void run() {
        if (influxDB != null) {
            Point point = null;
            if (dur != null) {
                point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                        .time(start, TimeUnit.MILLISECONDS)
                        .tag("num", String.valueOf(num))
                        .tag("api", multiRunService.getName())
                        .tag("key", multiRunService.getKeyBpm())
                        .addField("i", 1)
                        .addField("dur", dur)
                        .build();
            } else {
                point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                        .time(start, TimeUnit.MILLISECONDS)
                        .tag("num", String.valueOf(num))
                        .tag("api", multiRunService.getName())
                        .tag("key", multiRunService.getKeyBpm())
                        .addField("i", 1)
                        .build();
            }
            BatchPoints batchPoints = BatchPoints
                    .database(multiRunService.getInfluxDbBaseName())
//                    .retentionPolicy("defaultPolicy")
                    .build();
            batchPoints.point(point);
            influxDB.write(batchPoints);
        }
    }
}
