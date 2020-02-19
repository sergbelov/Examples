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

public class SqlPoolConnectExample2 {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) throws SQLException {
        Configurator.setRootLevel(Level.INFO);

        String dbUrl = "jdbc:hsqldb:file:myhsqldbTest/dbTest";
        String dbUserName = "admin";
        String dbPassword = "admin";

        DBService dbService = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUserName)
                .dbPassword(dbPassword)
                .build();

        dbService.initPooledDataSource(
                100,
                100,
                3,
                50,
                2,
                10);


        int threadCount = 10;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(new RunnableSQL(i, countDownLatch, dbService));
        }
        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        LOG.info("Работа всех потоков завершена");
        dbService.close();
    }

    static class RunnableSQL implements Runnable {
        final String name;
        CountDownLatch countDownLatch;
        DBService dbService;

        public RunnableSQL(
                int i,
                CountDownLatch countDownLatch,
                DBService dbService) {
            this.name = "RunnableSQL" + (i + 1);
            this.countDownLatch = countDownLatch;
            this.dbService = dbService;
        }

        @Override
        public void run() {
            LOG.info("Старт потока {}", name);
            try {
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);

                ResultSet resultSet = dbService.executeQuery(statement,
                        "select * from SESSIONS");
                while (resultSet.next()) {
                    System.out.println(name + " " +
                            resultSet.getString(1) + " " +
                            resultSet.getString(2) + " " +
                            resultSet.getString(3));
                }
                statement.close();
                resultSet.close();
                dbService.closeConnectionFromPool(connection);
            } catch (Exception e) {
                LOG.error("\n", e);
            }
            countDownLatch.countDown();
            LOG.info("Остановка потока {}", name);
        }
    }

}
