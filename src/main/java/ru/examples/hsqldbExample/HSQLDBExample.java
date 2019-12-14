package ru.examples.hsqldbExample;

import java.sql.*;

//PreperedStatement;
//CallableStatement;

public class HSQLDBExample {

    static Connection connection = null;
    static Statement statement = null;
    static PreparedStatement preparedStatement = null;
    static ResultSet resultSet = null;


    static private boolean loadDriver() {
        try {
            Class.forName("org.hsqldb.jdbcDriver"); //jdbcDriver -
        } catch (ClassNotFoundException e) {
            System.out.println("Драйвер не найден");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static private boolean setConnection() {
        try {
            StringBuilder connectionString = new StringBuilder();
            connectionString
                    .append("jdbc:hsqldb:file:")
                    .append("myhsqldb/")
                    .append("mydb");

            String login = "admin";
            String password = "admin";
            connection = DriverManager.getConnection(connectionString.toString(), login, password);
//                connection.setAutoCommit(false); // для обработки транзакций
//                connection.commit();
//                connection.rollback();
        } catch (SQLException e) {
            System.out.println("Соединение не создано");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static private void closeConnection() throws SQLException {
//            try {
//                this.connection.rollback();
//                this.connection.setAutoCommit(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

        Statement statement = connection.createStatement();
        statement.execute("SHUTDOWN");
    }






    public static void main(String[] args) throws SQLException {

        if (!loadDriver() || !setConnection()) {return;}

        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS testTable (id IDENTITY , value VARCHAR(255))");

//        Executor executor = new Executor(test.connection);
//        executor.execute("CREATE TABLE IF NOT EXISTS testTable (id IDENTITY , value VARCHAR(255))");

        preparedStatement = connection.prepareStatement("INSERT INTO testTable (value) VALUES(?)");
        for (int i = 0; i < 10; i++){
            preparedStatement.setString(1, "String"+i);
            preparedStatement.executeUpdate();
        }

        resultSet = statement.executeQuery("select * from testTable order by id");

//        preparedStatement = connection.prepareStatement("select * from testTable where id > ? order by id");
//        preparedStatement.setInt(1, 50);
//        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            System.out.println(resultSet.getString("id") + " "+ resultSet.getString("value"));
        }

        closeConnection();
    }

}
