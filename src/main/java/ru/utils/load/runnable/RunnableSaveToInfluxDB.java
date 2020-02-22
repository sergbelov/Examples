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
    private final String name;
    private long start;
    private Long stop;
    private List<Call> callList;
    private MultiRunService multiRunService;
    private InfluxDB influxDB;

    public RunnableSaveToInfluxDB(
            String name,
            long start,
            Long stop,
            List<Call> callList,
            MultiRunService multiRunService
    ) {
        this.name = name + "_SaveToInfluxDB";
        LOG.trace("Инициализация потока {}", name);
        this.start = start;
        this.stop = stop;
        this.callList = callList;
        this.multiRunService = multiRunService;
        this.influxDB = multiRunService.getInfluxDB();
    }

    @Override
    public void run() {
        LOG.debug("Старт потока {}", name);
        Point point = null;
        if (stop != null) {
            if (influxDB != null) {
                point = Point.measurement(multiRunService.getInfluxDbMeasurement())
//                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .time(start, TimeUnit.MILLISECONDS)
                        .addField("stop", stop)
                        .addField("api", multiRunService.getName())
                        .addField("key", multiRunService.getKeyBpm())
                        .build();
            }
            callList.add(new Call(start, stop));
        } else {
            if (influxDB != null) {
                point = Point.measurement(multiRunService.getInfluxDbMeasurement())
//                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .time(start, TimeUnit.MILLISECONDS)
                        .addField("api", multiRunService.getName())
                        .addField("key", multiRunService.getKeyBpm())
                        .build();
            }
            callList.add(new Call(start));
        }
        if (point != null) {
            BatchPoints batchPoints = BatchPoints
                    .database(multiRunService.getInfluxDbBaseName())
//                .retentionPolicy("defaultPolicy")
                    .build();
            batchPoints.point(point);
            influxDB.write(batchPoints);
        }
        LOG.debug("Остановка потока {}", name);
    }
}
