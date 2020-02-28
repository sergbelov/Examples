package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.TimeUnit;

/**
 * Created by Belov Sergey
 */
public class RunnableSaveToInfluxDbError implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableSaveToInfluxDbError.class);
    private long time;
    private MultiRunService multiRunService;
    private InfluxDB influxDB;
    private int thread;

    public RunnableSaveToInfluxDbError(
            long time,
            MultiRunService multiRunService,
            int thread
    ) {
        this.time = time;
        this.multiRunService = multiRunService;
        this.influxDB = multiRunService.getInfluxDB();
        this.thread = thread;
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        if (influxDB != null) {
            try {
                Point point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                        .time(time, TimeUnit.MILLISECONDS)
                        .tag("type", "error")
                        .tag("thread", String.valueOf(thread))
                        .tag("api", multiRunService.getName())
                        .tag("key", multiRunService.getKeyBpm())
                        .addField("i", 1)
                        .build();
                influxDB.write(point);
            } catch (Exception e) {
                LOG.error("Ошибка при сохранении метрик в InfluxDB\n", e);
            }
        }
        threadNum = multiRunService.stopThread();
    }
}
