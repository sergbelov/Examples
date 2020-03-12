package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.utils.MultiRunService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Belov Sergey
 * Количество записей по заданному SQL - запросу
 */
public class RunnableSqlSelectCount implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableSqlSelectCount.class);

    private final String name;                      // наименование потока
    private final String type;                      // тип метрики
    private final String sql;                       // sql - запрос
    private final long timeStep;                    // временной интервал между снятиями метрик (мс)
    private MultiRunService multiRunService;        //
    private List<DateTimeValue> sqlSelectCountList; // list для сохранеиня метрик
    private final int countForBreak;                // произойдет прерываени нагрузки при достижении данного значения ( > 0)
    private InfluxDB influxDB;

    public RunnableSqlSelectCount(
            String name,
            String type,
            String sql,
            long timeStep,
            MultiRunService multiRunService,
            List<DateTimeValue> sqlSelectCountList,
            int countForBreak,
            InfluxDB influxDB
    ) {
        this.type = type;
        this.name = name + " " + type;
        this.sql = sql;
        this.timeStep = timeStep; // 15 * 1000; // опрос каждые 15 секунд
        LOG.debug("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
        this.sqlSelectCountList = sqlSelectCountList;
        this.countForBreak = countForBreak;
        this.influxDB = influxDB;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
        if (multiRunService.getDbService() != null) {
            Connection connection = multiRunService.getDbService().getConnection();
            Statement statement = multiRunService.getDbService().createStatement(connection);
            long start = System.currentTimeMillis() + timeStep;
            while (multiRunService.getThreadCount() > 0) {
                if (System.currentTimeMillis() > start) {
                    try {
                        ResultSet resultSet = multiRunService.getDbService().executeQuery(statement, sql);
                        if (resultSet.next()) {
                            int cnt = resultSet.getInt("cnt");
                            long time = System.currentTimeMillis();
                            sqlSelectCountList.add(new DateTimeValue(time, cnt));
                            LOG.info("{} (VU:{} Threads:{}): {} - {}",
                                    name,
                                    multiRunService.getVuCount(),
                                    multiRunService.getThreadCount(),
                                    cnt,
                                    sql);

                            if (influxDB != null) {
                                try {
                                    Point point = Point.measurement(multiRunService.getInfluxDbMeasurement())
                                            .time(time, TimeUnit.MILLISECONDS)
                                            .tag("type", type)
                                            .tag("thread", String.valueOf(1))
                                            .tag("api", multiRunService.getName())
                                            .tag("key", multiRunService.getKeyBpm())
                                            .addField("i", 1)
                                            .addField("count", cnt)
                                            .build();
                                    influxDB.write(point);
                                } catch (Exception e) {
                                    LOG.error("", e);
                                }
                            }
                            if (countForBreak > 0 && cnt > countForBreak) {
                                multiRunService.stop(sql + ": " + cnt);
                            }
                        }
                        resultSet.close();
                    } catch (SQLException e) {
                        LOG.error("", e);
                    }
                    start = System.currentTimeMillis() + timeStep;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
        LOG.info("Остановка потока {}", name);
    }
}
