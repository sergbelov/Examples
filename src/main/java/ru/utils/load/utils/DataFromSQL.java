package ru.utils.load.utils;

import ru.utils.load.data.Call;
import ru.utils.load.data.sql.DBData;
import ru.utils.load.data.sql.DBResponse;
import ru.utils.load.data.DateTimeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Сбор информации из БД БПМ
 */
public class DataFromSQL {
    private static final Logger LOG = LogManager.getLogger(DataFromSQL.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private DBService dbService = null;

    public DataFromSQL() {
    }

    public void init(
            String dbUrl,
            String dbUser,
            String dbPassword) {

        // подключаемся к БД БПМ
        dbService = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUser)
                .dbPassword(dbPassword)
                .build();
        dbService.connect();
    }

    /**
     * Отключаемся от БД
     */
    public void end() {
        if (dbService != null) {
            dbService.disconnect();
        }
    }


    /**
     * Сбор статистики по выполнению процессов в БПМ
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public DBResponse getStatisticsFromBpm(
            String key,
            long startTime,
            long stopTime,
            int sent // для демо,  при отсутсвии БД
    ) {

        List<DBData> dbDataList = new ArrayList<>();

        LOG.debug("BPM {} - {}", sdf1.format(startTime), sdf1.format(stopTime));

        // запрос к БД БПМ для получения статистики
        String sql = "select 1 as value, count(1) as cnt\n";

        if (dbService != null && dbService.isConnection()) {
            try {
                LOG.trace("Обработка данных SQL БПМ...\n{}", sql);
                ResultSet resultSet = dbService.executeQuery(sql);
                while (resultSet.next()) {
                    LOG.trace("{} = {}",
                            resultSet.getString(1),
                            resultSet.getInt(2));

                    dbDataList.add(new DBData(
                            resultSet.getString(1),
                            resultSet.getInt(2)));
                }
                resultSet.close();
            } catch (Exception e) {
                LOG.error("", e);
            }
            LOG.debug("Обработка данных SQL БПМ завершена.");

        } else { // нет подключения к БД (нагенерим случайных значений)

            int complete = (int) (Math.random() * sent);
            int running = (int) (Math.random() * (complete));
            dbDataList.add(new DBData("COMPLETE", complete));
            dbDataList.add(new DBData("RUNNING", running));
        }

        return new DBResponse(sql, dbDataList);
    }

}
