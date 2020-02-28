package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Created by Belov Sergey
 * Поток для получения данных из БД БПМ
 */
public class RunnableDbSelectTransitionsTime implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableDbSelectTransitionsTime.class);
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    private final String name;
    private String key;
    private long startTime;
    private long stopTime;
    private CountDownLatch countDownLatch;
    private DBService dbService;
    private AtomicInteger countMain;
    private AtomicLong durMin;
    private AtomicLong durMax;
    private AtomicDouble durAvg;
    private List<Double> durList;

    public RunnableDbSelectTransitionsTime(
            int cnt,
            String key,
            long startTime,
            long stopTime,
            CountDownLatch countDownLatch,
            DBService dbService,
            AtomicInteger countMain,
            AtomicLong durMin,
            AtomicLong durMax,
            AtomicDouble durAvg,
            List<Double> durList
    ) {
        this.name = "SQL Select TransitionsTime_" + cnt + " (" + sdf1.format(startTime) + " - " + sdf1.format(stopTime) + ")";
        LOG.trace("Инициализация потока {}", name);
        this.key = key;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.countDownLatch = countDownLatch;
        this.dbService = dbService;
        this.countMain = countMain;
        this.durMin = durMin;
        this.durMax = durMax;
        this.durAvg = durAvg;
        this.durList = durList;
    }

    @Override
    public void run() {
        LOG.debug("Старт потока {}", name);
        String sql = "select \n" +
                "pd.name as MAIN_PROCESS,\n" +
                "pi.ID as MAIN_ID,\n" +
                "pi.DURATIONINMILLIS as MAIN_DUR, \n" +
                "pa.ACTIVITYNAME as PROCESS, \n" +
                "pa.DURATIONINMILLIS as DUR \n" +
                "from " +
                "where pi.processdefinitionkey = '" + key + "' " +
                "and pi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
//                "and pi.endtime < to_timestamp('" + sdf1.format(stopTime + 60000) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and pi.PROCESSSTATE = 'COMPLETED'\n" +
                "order by pd.name, pi.id, pa.ACTIVITYNAME";
        try {
            String idMainMem = "";
            long durMainMem = 0L;
            long durSteps = 0L;
            int countStep = 0;
            double avg = 0.00;

            LOG.debug("Обработка данных SQL БПМ (Время затраченное на переходы между задачами процесса)...\n{}", sql);
            Connection connection = dbService.getConnection();
            Statement statement = dbService.createStatement(connection);
            ResultSet resultSet = dbService.executeQuery(statement, sql);
            while (resultSet.next()) {
                String idMain = resultSet.getString("MAIN_ID");
//                String process = resultSet.getString("PROCESS");
                long durMain = resultSet.getLong("MAIN_DUR");
                long dur = resultSet.getLong("DUR");
//                LOG.info("{}: {}, {}, {}, {}", name, idMain, process, durMain, dur);
                if (!idMain.equals(idMainMem)) {
                    if (countStep > 0) {
                        double durMainTransitions = (durMainMem - durSteps) / (countStep + 1);
                        LOG.trace("{}: Длительность основного процесса ({}): {}, Длительность шагов: {}, Количество переходов: {}, Длительность переходов: {}",
                                name,
                                idMain,
                                durMainMem,
                                durSteps,
                                countStep + 1,
                                durMainTransitions);

                        countMain.incrementAndGet();
                        durList.add(durMainTransitions);
                        durMin.getAndAccumulate((long) durMainTransitions, Math::min);
                        durMax.getAndAccumulate((long) durMainTransitions, Math::max);
                        avg = avg + durMainTransitions;
                    }
                    durSteps = 0;
                    idMainMem = idMain;
                    durMainMem = durMain;
                    countStep = 0;
                }
                countStep++;
                durSteps = durSteps + dur;
            }
            if (countStep > 0) { // последняя группа
                double durMainTransitions = (durMainMem - durSteps) / (countStep + 1);
                countMain.incrementAndGet();
                durList.add(durMainTransitions);
                durMin.getAndAccumulate((long) durMainTransitions, Math::min);
                durMax.getAndAccumulate((long) durMainTransitions, Math::max);
                avg = avg + durMainTransitions;
                durAvg.set(durAvg.get() + avg); // avg
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            LOG.error("{}\n", name, e);
        }
        LOG.debug("Остановка потока {}", name);
        countDownLatch.countDown();
    }
}
