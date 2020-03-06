package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;
import ru.utils.load.data.sql.DBData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Belov Sergey
 * Поток для получения данных из БД БПМ
 */
public class RunnableDbSelectData implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableDbSelectData.class);
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    private final String name;
    private String key;
    private long startTime;
    private long stopTime;
    private CountDownLatch countDownLatch;
    private DBService dbService;
    private List<DBData> dbDataList;

    public RunnableDbSelectData(
            int cnt,
            String key,
            long startTime,
            long stopTime,
            CountDownLatch countDownLatch,
            DBService dbService,
            List<DBData> dbDataList
    ) {
        this.name = "SQL Select Data_" + cnt + " (" + sdf1.format(startTime) + " - " + sdf1.format(stopTime) + ")";
        LOG.trace("Инициализация потока {}", name);
        this.key = key;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.countDownLatch = countDownLatch;
        this.dbService = dbService;
        this.dbDataList = dbDataList;
    }

    @Override
    public void run() {
        LOG.debug("Старт потока {}", name);
        if (dbService != null) {
        String sql = "select hpi.starttime, hpi.endtime, hpi.processstate " +
                "from " +
                "join " +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')";
            try {
                int row = 0;
                LOG.debug("{}: Запрос данных из БД БПМ...\n{}", name, sql);
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);
                ResultSet resultSet = dbService.executeQuery(statement, sql);
                LOG.debug("{}: Обработка данных из БД БПМ...", name);
                while (resultSet.next()) {
                    row++;
                    if (row % 10000 == 0) {
                        LOG.info("{}: {} - {}", name, sdf1.format(System.currentTimeMillis()), row);
                    }
                    LOG.trace("{}: {}, {}, {}, {}",
                            name,
                            row,
                            resultSet.getTimestamp(1),
                            resultSet.getTimestamp(2),
                            resultSet.getString(3));

                    if (resultSet.getTimestamp("ENDTIME") != null) {
                        dbDataList.add(new DBData(
                                resultSet.getTimestamp("STARTTIME").getTime(),
                                resultSet.getTimestamp("ENDTIME").getTime(),
                                resultSet.getString("PROCESSSTATE")));
                    } else {
                        dbDataList.add(new DBData(
                                resultSet.getTimestamp("STARTTIME").getTime(),
                                resultSet.getString("PROCESSSTATE")));
                    }
                }
                LOG.info("{}: {} - {}", name, sdf1.format(System.currentTimeMillis()), row);
                resultSet.close();
                statement.close();
                connection.close();
                LOG.debug("{}: Обработка данных из БД БПМ завершена.", name);
            } catch (Exception e) {
                LOG.error("{}\n", name, e);
            }
        }
        LOG.debug("Остановка потока {}", name);
        countDownLatch.countDown();
    }
}
