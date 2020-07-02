package ru.examples.sqlExample;

/*
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Level;

import ru.utils.db.DBService;
import ru.utils.db.DBType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLExample2 {

//    private static final Logger LOG = LogManager.getLogger();
    private static final Logger LOG = LoggerFactory.getLogger(SQLExample2.class);

    public static void main(String[] args) throws SQLException {

        String dbUrl = "jdbc:hsqldb:file:myhsqldbTest/dbTest";
        String dbHost = "myhsqldbTest";
        String dbBase = "dbTest";
        String dbUserName = "admin";
        String dbPassword = "admin";

        LOG.info("\n\n==== 1 вариант создания подключения - через Builder()");
        DBService dbService = new DBService.Builder()
                .dbUrl(dbUrl)
                .dbUserName(dbUserName)
                .dbPassword(dbPassword)
                .build();

        if (!dbService.connect()) {
            System.exit(1);
        }


        LOG.info("\n\n==== 2 вариант создания подключения - через конструктор");
        dbService = new DBService(
                Level.TRACE,
                dbUrl,
                dbUserName,
                dbPassword);

        if (!dbService.connect()) {
            System.exit(1);
        }


        LOG.info("\n\n==== переподключение к другой БД");
        if (!dbService.connect(
                DBType.HSQLDB,
                dbHost,
                dbBase,
                0,
                dbUserName,
                dbPassword)) {
            System.exit(1);
        }
        ;

//        dbService.setLoggerLevel(Level.TRACE);

        Statement statement = null;
        ResultSet resultSet = null;

        LOG.info("\n\n==== подключение уже имеется");
        dbService.connect();

        dbService.getConnectInfo();

        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "ID IDENTITY, " +
                "USERNAME VARCHAR(25), " +
                "FULLUSERNAME VARCHAR(100), " +
                "PASSWORD VARCHAR(50))";

        if (!dbService.execute(sql)) {
            System.exit(1);
        }


        if (!dbService.execute(
                "CREATE TABLE IF NOT EXISTS tmpTable (" +
                        "ID NUMERIC(10,0), " +
                        "NAME VARCHAR(25))")) {
            System.exit(1);
        }
/*
        PreparedStatement preparedStatementInsert = dbService.connection().prepareStatement(
                "insert into TMPTABLE (ID, NAME) values (?, ?)");

        for (int i = 0; i < 100000; i++){
           preparedStatementInsert.setInt(1, i);
           preparedStatementInsert.setString(2, "name"+i);
           preparedStatementInsert.execute();
        }
*/
        int count = 0, c = 0;
        long timeStart = System.currentTimeMillis();
        for (int s = 0; s < 5; s++) {
            ResultSet rsTmpTable;

/*
            rsTmpTable = dbService.executeQuery("select count(1) from tmpTable");
            if (rsTmpTable.next()) {
                count = rsTmpTable.getInt(1);
            }
            rsTmpTable.close();
*/

            rsTmpTable = dbService.executeQuery("select * from tmpTable");
            c = 0;
            count = dbService.getCountResultSet(rsTmpTable);
            while (rsTmpTable.next()) {
                c++;
            }
            rsTmpTable.close();
        }
        System.out.println("count: " + count + ", " + c);
        System.out.println("Длительность выполнения: " + (System.currentTimeMillis() - timeStart));





/*
        sql = "CREATE TABLE IF NOT EXISTS tableJson (" +
                "ID IDENTITY, " +
                "json VARCHAR(1000), " +
                "comment VARCHAR(100))";

        if (!dbService.execute(sql)) {
            System.exit(1);
        }

        for (int i = 0; i < 10; i++) {
            dbService.execute("INSERT INTO tableJson (json, comment) VALUES('{\"field\":\"" + i + "\"}', '" + i + "')");
        }
*/

        resultSet = dbService.executeQuery("select * from users");
        System.out.println("ResultSet count: " + dbService.getCountResultSet(resultSet));
        while (resultSet.next()) {
            System.out.println(resultSet.getString("USERNAME") + " " + resultSet.getString("FULLUSERNAME"));
        }

        System.out.println("===========================================");
        PreparedStatement preparedStatement = dbService.connection().prepareStatement("select FULLUSERNAME from users where LOWER(USERNAME) = ?");

        String[] userList = new String[]{"user1", "user2", "user3"};

        for (String u : userList) {
            preparedStatement.setString(1, u);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
        }


/*        if (!dbService.connect(
                DBService.DBType.ORACLE,
                "192.168.1.1",
                1521,
                "base",
                "username",
                "userpas")){
            System.exit(1);
        }*/

/*        if (!dbService.connect(
                DBService.DBType.SQLSERVER,
                "192.168.1.1",
                "base",
                "username",
                "userpas")){
            System.exit(1);
        }
        ;

        resultSet = dbService.executeQuery("select lower(EMAIL) as EMAIL from JIRA_RESPONSIBLES_COMMON order by EMAIL");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("email") );
        }*/


        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }

        dbService.close();

    }
}
