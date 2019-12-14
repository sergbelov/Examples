package ru.examples.hsqldbExample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//PreperedStatement;
//CallableStatement;

public class HSQLDBExample0 {

    public static void main(String[] args){

        HSQLDB test = new HSQLDB();
        if (!test.loadDriver()) return;
        if (!test.getConnection()) return;

        test.createTable();
//        test.deleteTable();
        test.fillTable();
        test.printTable();

        test.createTable2();
//        test.deleteTable2();
        test.fillTable2();
        test.printTable2();

        test.closeConnection();

//        resultSet.close();
//        statement.close();
//        connection.close();
    }

    static class HSQLDB {
        Connection connection = null;

        private boolean loadDriver() {
            try {
                Class.forName("org.hsqldb.jdbcDriver"); //JDBCDriver -
            } catch (ClassNotFoundException e) {
                System.out.println("Драйвер не найден");
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean getConnection() {

            try {
                String path = "myhsqldb/";
                String dbname = "mydb";
                String connectionString = "jdbc:hsqldb:file:" + path + dbname;
                String login = "admin";
                String password = "admin";
                connection = DriverManager.getConnection(connectionString, login, password);

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

        private void createTable() {
            try {
                Statement statement = connection.createStatement();
                String sql = "CREATE TABLE IF NOT EXISTS testTable (id IDENTITY , value VARCHAR(255))";
                statement.executeUpdate(sql);
            } catch (SQLException e) {

            }
        }

        private void deleteTable() {
            try {
                Statement statement = connection.createStatement();
                String sql = "delete from testTable";
                statement.executeUpdate(sql);
            } catch (SQLException e) {

            }
        }
        private void deleteTable2() {
            try {
                Statement statement = connection.createStatement();
                String sql = "delete from testTable2";
                statement.executeUpdate(sql);
            } catch (SQLException e) {

            }
        }


        private void fillTable() {
            Statement statement;
            try {
                statement = connection.createStatement();
                String sql = "INSERT INTO testTable (value) VALUES('Вася')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable (value) VALUES('Петя')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable (value) VALUES('Саша')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable (value) VALUES('Катя')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable (value) VALUES('Света')";
                statement.executeUpdate(sql);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        private void createTable2() {
            try {
                Statement statement = connection.createStatement();
                String sql = "CREATE TABLE IF NOT EXISTS testTable2 (id IDENTITY , value VARCHAR(255))";
                statement.executeUpdate(sql);
            } catch (SQLException e) {

            }
        }

        private void fillTable2() {
            Statement statement;
            try {
                statement = connection.createStatement();
                String sql = "INSERT INTO testTable2 (value) VALUES('Вася_2')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable2 (value) VALUES('Петя_2')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable2 (value) VALUES('Саша_2')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable2 (value) VALUES('Катя_2')";
                statement.executeUpdate(sql);
                sql = "INSERT INTO testTable2 (value) VALUES('Света_2')";
                statement.executeUpdate(sql);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void printTable() {
            Statement statement;
            try {
                statement = connection.createStatement();
                String sql = "SELECT id, value FROM testTable";
                ResultSet resultSet = statement.executeQuery(sql);

//                resultSet.next();
//                resultSet.previous();
//                resultSet.isLast();

                while (resultSet.next()) {
                    System.out.println(resultSet.getInt("id") + " "
                            + resultSet.getString("value"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void printTable2() {
            Statement statement;
            try {
                statement = connection.createStatement();
                String sql = "SELECT t1.id, t1.value, t2.value\n" +
                        "FROM testTable t1\n" +
                        "join testTable2 t2 on t2.id = t1.id\n" +
                        "order by 2";

                System.out.println(sql);

                ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()) {
                    System.out.println(resultSet.getInt(1) + " " +
                            resultSet.getString(2) + " " +
                            resultSet.getString(3));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void closeConnection() {
//            try {
//                this.connection.rollback();
//                this.connection.setAutoCommit(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

            Statement statement;
            try {
                statement = connection.createStatement();
                String sql = "SHUTDOWN";
                statement.execute(sql);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
