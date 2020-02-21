package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.utils.MultiRunService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by Belov Sergey
 * Проверяем возникновение тротлинга
 */
public class RunnableThrottlingState implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableThrottlingState.class);

    private final String name;
    private MultiRunService multiRunService;
    private List<DateTimeValue> bpmsJobEntityImplCountList;

    public RunnableThrottlingState(
            String name,
            MultiRunService multiRunService,
            List<DateTimeValue> bpmsJobEntityImplCountList
    ) {
        this.name = name;
        LOG.debug("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
        this.bpmsJobEntityImplCountList = bpmsJobEntityImplCountList;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
        String sqlForLog =  "select count(1) from BPMS.BPMSJOBENTITYIMPL";
        String sql = "select count(1) as cnt " +

                "and pdi.key = '" + multiRunService.getKeyBpm() + "'";
        if (multiRunService.getDbService() != null) {
            Connection connection = multiRunService.getDbService().getConnection();
            Statement statement = multiRunService.getDbService().createStatement(connection);
            long step = 15 * 1000; // опрос каждые 15 секунд
            long start = System.currentTimeMillis() + step;
//            while (multiRunService.isRunning() && (
//                    System.currentTimeMillis() < multiRunService.getTestStopTime() ||
//                            multiRunService.getThreadCount() > 0)) {
            while (multiRunService.getThreadCount() > 0) {

                if (System.currentTimeMillis() > start) {
                    try {
                        ResultSet resultSet = multiRunService.getDbService().executeQuery(statement, sql);
                        if (resultSet.next()) {
                            int cnt = resultSet.getInt("cnt");
//                            if (cnt > 0) {
                                bpmsJobEntityImplCountList.add(new DateTimeValue(System.currentTimeMillis(), cnt));
                                LOG.info("{} ({}) (VU:{}) (Threads:{}): {}: {}",
                                        name,
                                        multiRunService.getKeyBpm(),
                                        multiRunService.getVuCount(),
                                        multiRunService.getThreadCount(),
                                        sqlForLog,
                                        cnt);
//                            }
                        }
                        resultSet.close();
                    } catch (SQLException e) {
                        LOG.error("", e);
                    }
                    start = System.currentTimeMillis() + step;
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
