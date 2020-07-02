package ru.examples.sqlExample;

/*
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*/
import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.utils.authorization.UserAuthorizationService;
import ru.utils.db.DBService;
import ru.utils.db.DBType;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SQLExample3 {

//    private static final Logger LOG = LogManager.getLogger();
    private static final Logger LOG = LoggerFactory.getLogger(SQLExample3.class);

    public static void main(String[] args) throws SQLException {
        DateFormat dateFormatTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        ResultSet resultSet;

/*
// DBService уже создан

        DBService dbService = new DBService.Builder()
                .dbType(DBService.DBType.HSQLDB)
                .dbHost("myhsqldbTest")
                .dbBase("dbTest")
                .dbUserName("admin")
                .dbPassword("admin")
                .loggerLevel(Level.DEBUG)
                .build();

        UserAuthorizationService userAuthorizationService = new UserAuthorizationService.Builder()
                .dbService(dbService)
                .loggerLevel(Level.DEBUG)
                .build();
*/


// DBService создается внутри

        UserAuthorizationService userAuthorizationService = new UserAuthorizationService.Builder()
                .dbType(DBType.HSQLDB)
                .dbHost("myhsqldbTest")
                .dbBase("dbTest")
                .dbUserName("admin")
                .dbPassword("admin")
                .loggerLevel(Level.TRACE)
                .build();


/*
// база для пользователей не указана создадим по умолчанию

        UserAuthorizationService userAuthorizationService = new UserAuthorizationService.Builder()
                .loggerLevel(Level.DEBUG)
                .build();

*/



/*
        Locale.setDefault(Locale.ENGLISH);
        UserAuthorizationService userAuthorizationService = new UserAuthorizationService.Builder()
                .dbType(DBService.DBType.ORACLE)
//                .dbType(DBService.DBType.SQLSERVER)
                .dbHost("localhost")
                .dbBase("XE")
                .dbPort(1521)
                .dbUserName("system")
                .dbPassword("system")
                .loggerLevel(Level.TRACE)
                .build();
*/

/*
        UserAuthorizationService userAuthorizationService = new UserAuthorizationService.Builder()
                .dbType(DBService.DBType.SQLSERVER)
                .dbHost("localhost")
                .dbBase("computer")
                .dbUserName("user1")
                .dbPassword("user1")
                .loggerLevel(Level.TRACE)
                .build();
*/

        userAuthorizationService.setSessionDuration(12000);

        if (!userAuthorizationService.connect()){
            userAuthorizationService.disconnect();
            System.exit(1);
        }

/*
        resultSet = userAuthorizationService.getDbService().executeQuery("select * from SESSIONS");
        while (resultSet.next()) {
            System.out.println(
                    resultSet.getString(1) +" "+
                    resultSet.getString(2) +" "+
                    resultSet.getLong(3) +" до "+
                    dateFormatTime.format(resultSet.getLong(3)));
        }
*/

        List<String> sessionList = new ArrayList<>();
        String userName = "user";
        String userPass = "user";
        for (int u = 0; u < 5; u++) {
            userName = "user" + u;
            userPass = "user" + u;

            if (!userAuthorizationService.isUserCorrect(userName, userPass)) {
                if (userAuthorizationService.getError().equals(UserAuthorizationService.Error.LOGIN))
                    if (!userAuthorizationService.userAdd(userName, userName, userPass, userPass)) {
                        userAuthorizationService.disconnect();
                        System.exit(1);
                    } else {
                        userAuthorizationService.isUserCorrect(userName, userPass);
                    }
            }

            sessionList.add(userAuthorizationService.getSession());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!userAuthorizationService.getError().equals(UserAuthorizationService.Error.NO_ERROR)){
            System.out.println(userAuthorizationService.getErrorMessage());
            System.exit(1);
        }

        // дублируем сессию
        userAuthorizationService.isUserCorrect(userName, userPass);

        // продлим сессии
        System.out.println(sessionList.get(0) + " было до  " + dateFormatTime.format(userAuthorizationService.getTimeEnd(sessionList.get(0))));
        userAuthorizationService.isSessionCorrect(sessionList.get(0));
        System.out.println(sessionList.get(0) + " стало до " + dateFormatTime.format(userAuthorizationService.getTimeEnd(sessionList.get(0))));

        System.out.println(sessionList.get(2) + " было до  " + dateFormatTime.format(userAuthorizationService.getTimeEnd(sessionList.get(2))));
        userAuthorizationService.isSessionCorrect(sessionList.get(2));
        System.out.println(sessionList.get(2) + " стало до " + dateFormatTime.format(userAuthorizationService.getTimeEnd(sessionList.get(2))));


        System.out.println("Пользователь "
                + userAuthorizationService.getFullUserName()
                + " корректен, session = "
                + userAuthorizationService.getSession() + " до "
                + dateFormatTime.format(userAuthorizationService.getTimeEnd()));
/*
        resultSet = userAuthorizationService.getDbService().executeQuery("select * from SESSIONS");
        while (resultSet.next()) {
            System.out.println(
                    resultSet.getString(1) +" "+
                    resultSet.getString(2) +" "+
                    resultSet.getLong(3) +" до "+
                    dateFormatTime.format(resultSet.getLong(3)));
        }
*/

        System.out.println("=============================================================");
        System.out.println("Техническая пауза...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        userAuthorizationService.disconnect();
        userAuthorizationService.connect();

//        userAuthorizationService.getDbService().connect(); // проверка двойного подключения
//        userAuthorizationService.connect();

        resultSet = userAuthorizationService.getDbService().executeQuery("select * from SESSIONS");
        while (resultSet.next()) {
            System.out.println(
                    resultSet.getString(1) +" "+
                    resultSet.getString(2) +" до "+
                    dateFormatTime.format(resultSet.getLong(3)) +" "+
                    userAuthorizationService.isSessionCorrect(resultSet.getString(2)) + " текущее время: " +
                    dateFormatTime.format(System.currentTimeMillis()));
        }

        userAuthorizationService.endSession();
        userAuthorizationService.disconnect();
    }
}
