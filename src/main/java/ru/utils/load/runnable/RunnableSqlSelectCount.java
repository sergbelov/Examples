package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.DateTimeValue;
import ru.utils.load.utils.MultiRunService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by Belov Sergey
 * Количество записей по заданному SQL - запросу
 */
public class RunnableSqlSelectCount implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableSqlSelectCount.class);

    private final String name;                      // наименование потока
    private final String sql;                       // sql - запрос
    private final long timeStep;                    // временной интервал между снятиями метрик (мс)
    private MultiRunService multiRunService;        //
    private List<DateTimeValue> sqlSelectCountList; // list для сохранеиня метрик
    private final int countForBreak;                // произойдет прерываени нагрузки при достижении данного значения ( > 0)

    public RunnableSqlSelectCount(
            String name,
            String sql,
            long timeStep,
            MultiRunService multiRunService,
            List<DateTimeValue> sqlSelectCountList,
            int countForBreak
    ) {
        this.name = name;
        this.sql = sql;
        this.timeStep = timeStep; // 15 * 1000; // опрос каждые 15 секунд
        LOG.debug("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
        this.sqlSelectCountList = sqlSelectCountList;
        this.countForBreak = countForBreak;
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
                            sqlSelectCountList.add(new DateTimeValue(System.currentTimeMillis(), cnt));
                            LOG.info("{} (VU:{} Threads:{}): {} - {}",
                                    name,
                                    multiRunService.getVuCount(),
                                    multiRunService.getThreadCount(),
                                    cnt,
                                    sql);
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
