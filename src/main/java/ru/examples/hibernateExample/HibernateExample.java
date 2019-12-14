package ru.examples.hibernateExample;


import ru.examples.hibernateExample.dbService.DBException;
import ru.examples.hibernateExample.dbService.DBService;
import ru.examples.hibernateExample.dbService.dataSets.UsersDataSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HibernateExample {

    public static void main(String[] args) {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        DBService dbService = new DBService(
                "true",
//                "create");
                "update");

        dbService.printConnectInfo();

        long userId;
        UsersDataSet dataSet;

        for (int i = 1; i < 10; i++) {
            try {
                userId = dbService.addUser("user_" + i, "user_" + i);
                System.out.println("Added user id: " + userId);

// нужно доделать
                dbService.updateUser("user_" + i, "user_" + i + "_" + dateFormat.format(System.currentTimeMillis()));
                System.out.println("Update user : user_" + i);


                dataSet = dbService.getUser(userId);
                System.out.println("User data set: " + dataSet);

                System.out.println("=====================================");
            } catch (DBException e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }
}
