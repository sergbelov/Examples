package ru.utils.load.utils;

import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
            String dbPassword){

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
    public void end(){
        if (dbService != null) {
            dbService.disconnect();
        }
    }


    /**
     * Сбор статистики по выполнению процессов в БПМ
     * @param startTime
     * @param stopTime
     * @param callList
     * @param bpmProcessStatisticList
     * @return
     */
    public String getStatisticsFromBpm(
            String key,
            long startTime,
            long stopTime,
            List<Call> callList,
            List<DateTimeValue> bpmProcessStatisticList) {

        int sent = 0;
        int complete = 0;
        int running = 0;

        LOG.debug("BPM {} - {}", sdf1.format(startTime), sdf1.format(stopTime));

        // запрос к БД БПМ для получения статистики
        String sql = "select count(1) as cnt,\n";

        if (dbService != null && dbService.isConnection()) {

            sent = (int) callList
                    .stream()
                    .filter(x -> (x.getTimeBegin() >= startTime && x.getTimeBegin() <= stopTime))
                    .count();
            try {
                LOG.trace("Обработка данных SQL БПМ...\n{}", sql);
                ResultSet resultSet = dbService.executeQuery(sql);
                while (resultSet.next()) {
                    LOG.trace("processstate = {}, cnt = {}",
                            resultSet.getString("processstate"),
                            resultSet.getInt("cnt"));

                    switch (resultSet.getString("processstate")) {
                        case "COMPLETED":
                            complete = resultSet.getInt("cnt");
                            break;
                        case "RUNNING":
                            running = resultSet.getInt("cnt");
                            break;
                        default:
                            LOG.warn(" не обрабатывается статус {}", resultSet.getString("processstate"));
                    }
                }
                resultSet.close();
            } catch (Exception e) {
                LOG.error("", e);
            }
            LOG.debug("Обработка данных SQL БПМ завершена.");

        } else {

            sent = (int) (Math.random() * 1000) + 100;
            complete = (int) (Math.random() * sent);
            running = sent - complete;
        }

        bpmProcessStatisticList.add(new DateTimeValue(
                startTime,
                stopTime,
                sent,
                complete,
                running));
        return sql;
    }

}
