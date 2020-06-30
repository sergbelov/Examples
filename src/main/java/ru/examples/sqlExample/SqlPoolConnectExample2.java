package ru.examples.sqlExample;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import ru.utils.db.DBService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 SELECT t.SID, t.SERIAL#, t.osuser as "User", t.MACHINE as "PC", t.PROGRAM as "Program"
 FROM v$session t
 --WHERE (NLS_LOWER(t.PROGRAM) = 'cash.exe') -- посмотреть сессии от программы cash.exe
 --WHERE status='ACTIVE' and osuser!='SYSTEM' -- посмотреть пользовательские сессии
 --WHERE username = 'схема' -- посмотреть сессии к схеме (пользователь)
 ORDER BY 4 ASC;
 */
public class SqlPoolConnectExample2 {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) throws SQLException {
        Configurator.setRootLevel(Level.INFO);

        AtomicInteger count = new AtomicInteger(0);

        String dbUrl = "jdbc:hsqldb:file:myhsqldbTest/dbTest";
        String dbUserName = "admin";
        String dbPassword = "admin";

/*
        String dbUrl = "jdbc:oracle:thin:@localhost:1521:dbtest";
        String dbUserName = "SYSTEM";
        String dbPassword = "123456";
*/

        DBService dbService = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUserName)
                .dbPassword(dbPassword)
                .build();

        if (dbService.connectPooled(
                5,
                60,
                100,
                0
        )) {


            int threadCount = 100;
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(new RunnableSQL(i, countDownLatch, dbService, count));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            try {
                countDownLatch.await(); // ждем завершения работы всех потоков
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executorService.shutdown();
            LOG.info("Работа всех потоков завершена");
        }
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dbService.close();
        LOG.info("Общее количество шагов: {}", count.get());
    }

    static class RunnableSQL implements Runnable {
        final String name;
        CountDownLatch countDownLatch;
        DBService dbService;
        AtomicInteger count;

        public RunnableSQL(
                int i,
                CountDownLatch countDownLatch,
                DBService dbService,
                AtomicInteger count) {
            this.name = "RunnableSQL" + (i + 1);
            this.countDownLatch = countDownLatch;
            this.dbService = dbService;
            this.count = count;
        }

        @Override
        public void run() {
            LOG.info("Старт потока {}", name);
            try {
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);

                ResultSet resultSet = dbService.executeQuery(statement,
                        "select * from SESSIONS");
//                        "select * from TABLE1");
                while (resultSet.next()) {
                    LOG.info("{} {} {} {}",
                            name,
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3));
                    count.incrementAndGet();
                }
                Thread.sleep(1000 * 30);
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                LOG.error("\n", e);
            }
            countDownLatch.countDown();
            LOG.info("Остановка потока {}", name);
        }
    }

}
