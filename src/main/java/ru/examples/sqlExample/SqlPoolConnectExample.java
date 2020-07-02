package ru.examples.sqlExample;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mchange.v2.c3p0.ComboPooledDataSource;
/*
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*/
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlPoolConnectExample {
    private static final Logger LOG = LoggerFactory.getLogger(SqlPoolConnectExample.class);

    private ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();

    public SqlPoolConnectExample() {
    }

    public ComboPooledDataSource getComboPooledDataSource() {
        return comboPooledDataSource;
    }

    /**
     * Инициализация пула подключений к БД
     */
    public void init() {
        try {
            comboPooledDataSource.setDriverClass("org.hsqldb.jdbcDriver");
            comboPooledDataSource.setJdbcUrl("jdbc:hsqldb:file:myhsqldbTest/dbTest;hsqldb.lock_file=false");
            comboPooledDataSource.setUser("admin");
            comboPooledDataSource.setPassword("admin");

            Properties properties = new Properties();
            properties.setProperty("user", "admin");
            properties.setProperty("password", "admin");
            properties.setProperty("useUnicode", "true");
            properties.setProperty("characterEncoding", "UTF8");
            comboPooledDataSource.setProperties(properties);

            // set options
            comboPooledDataSource.setMaxStatements(180);
            comboPooledDataSource.setMaxStatementsPerConnection(180);
            comboPooledDataSource.setMinPoolSize(50);
            comboPooledDataSource.setAcquireIncrement(10);
            comboPooledDataSource.setMaxPoolSize(60);
            comboPooledDataSource.setMaxIdleTime(30);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public void end(){
        comboPooledDataSource.close();
    }

    /**
     * Свободное подключение
     *
     * @return
     */
    public Connection getConnection() throws SQLException {
        // Получить подключение из пула
        Connection connection = null;
        connection = comboPooledDataSource.getConnection();
        LOG.debug("idleConnections = {}; busyConnections = {}",
                comboPooledDataSource.getNumIdleConnections(),
                comboPooledDataSource.getNumBusyConnections());
        return connection;
    }

    public void disconnect(Connection connection) throws SQLException {
        connection.close();
        LOG.debug("idleConnections = {}; busyConnections = {}",
                comboPooledDataSource.getNumIdleConnections(),
                comboPooledDataSource.getNumBusyConnections());
    }

    public ResultSet executeQuery(Statement statement, String sql) throws SQLException {
        return statement.executeQuery(sql);
    }




    public static void main(String[] args) throws SQLException {
        Configurator.setRootLevel(Level.INFO);

        SqlPoolConnectExample sqlPoolConnect = new SqlPoolConnectExample();
        sqlPoolConnect.init();

        int threadCount = 10;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(new RunnableSQL(i, countDownLatch, sqlPoolConnect));
        }
        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        LOG.info("Работа всех потоков завершена");
//        sqlPoolConnect.end();

    }

    static class RunnableSQL implements Runnable {
        final String name;
        CountDownLatch countDownLatch;
        SqlPoolConnectExample poolConnect;

        public RunnableSQL(
                int i,
                CountDownLatch countDownLatch,
                SqlPoolConnectExample poolConnect) {
            this.name = "RunnableSQL" + (i+1);
            this.countDownLatch = countDownLatch;
            this.poolConnect = poolConnect;
        }

        @Override
        public void run() {
            LOG.info("Старт потока {}", name);
            try {
                Connection connection = poolConnect.getConnection();
                Statement statement = connection.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);

                ResultSet resultSet = poolConnect.executeQuery(
                        statement,
                        "select * from SESSIONS");
                while (resultSet.next()) {
                    System.out.println(name + " " + resultSet.getString(1));
                }
                statement.close();
                resultSet.close();
                poolConnect.disconnect(connection);
            } catch (Exception e) {
               LOG.error("\n", e);
            }

            countDownLatch.countDown();
            LOG.info("Остановка потока {}", name);
        }
    }

}
