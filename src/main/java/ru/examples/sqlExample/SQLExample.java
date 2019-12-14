package ru.examples.sqlExample;

import ru.examples.sqlExample.dbService.DBException;
import ru.examples.sqlExample.dbService.DBService;
import ru.examples.sqlExample.dbService.dataSets.UsersDataSet;

public class SQLExample {
    public static void main(String[] args) {
        DBService dbService = new DBService();
        dbService.printConnectInfo();
        try {
            long userId = dbService.addUser("tully");
            System.out.println("Added user id: " + userId);

            UsersDataSet dataSet = dbService.getUser(userId);
            System.out.println("User data set: " + dataSet);

            dbService.cleanUp();
        } catch (DBException e) {
            e.printStackTrace();
        }
    }
}
