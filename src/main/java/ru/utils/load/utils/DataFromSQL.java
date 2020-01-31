package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.Call;
import ru.utils.load.data.DateTimeValue;
import ru.utils.db.DBService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Сбор информации из БД
 */
public class DataFromSQL {
    private static final Logger LOG = LogManager.getLogger(DataFromSQL.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private DBService dbService = null;
    private Long bpmPrevStartTime;

    public DataFromSQL() {
    }

    public void init(
            long testStartTime,
            String dbUrl,
            String dbUser,
            String dbPassword){

        this.bpmPrevStartTime = testStartTime;

        // подключаемся к БД
        dbService = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUser)
                .dbPassword(dbPassword)
                .build();
        dbService.connect();
    }

    public void end(){
        if (dbService != null) {
            dbService.disconnect();
        }
    }

    /**
     * Сбор статистики по выполнению процессов в
     * @param callList
     * @param bpmProcessStatisticList
     * @return
     */
    public String getStatisticsFromBpm(
            long stopTime,
            List<Call> callList,
            List<DateTimeValue> bpmProcessStatisticList) {
        long startTime = bpmPrevStartTime + 1;
        LOG.info("BPM {} - {}", sdf1.format(startTime), sdf1.format(stopTime));
        String sql = getStatisticsFromBpm(
                callList,
                bpmProcessStatisticList,
                startTime,
                stopTime);
        synchronized (bpmPrevStartTime) {
            bpmPrevStartTime = stopTime;
        }
        return sql;
    }

    /**
     * Сбор статистики по выполнению процессов в
     * @param callList
     * @param bpmProcessStatisticList
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getStatisticsFromBpm(
            List<Call> callList,
            List<DateTimeValue> bpmProcessStatisticList,
            long startTime,
            long stopTime) {

        // запрос к БД для получения статистики
        String sql = "select count(1) as cnt,\n";

        int sent = (int) callList
                .stream()
                .filter(x -> (x.getTimeBegin() >= startTime && x.getTimeBegin() <= stopTime))
                .count();

        int complete = 0;
        int running = 0;

        if (dbService != null && dbService.isConnection()) {

            try {
                LOG.trace("Обработка данных SQL...\n{}", sql);
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
            } catch (SQLException e) {
                LOG.error("", e);
            }
            LOG.debug("Обработка данных SQL завершена.");

        } else {

            complete = (int) (Math.random() * sent);
            running = sent - complete;
        }

        synchronized (bpmProcessStatisticList) {
            bpmProcessStatisticList.add(new DateTimeValue(
                    startTime,
                    stopTime,
                    sent,
                    complete,
                    running));
        }
        return sql;
    }

}
