package ru.examples.threadExample;

import ru.utils.db.DBService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorServiceExample {

    public static void main(String[] args) throws InterruptedException, SQLException {

        DBService dbService = new DBService.Builder()
                .dbType(DBService.DBType.HSQLDB)
                .dbHost("STORE")
                .dbBase("db_store")
                .dbUserName("admin")
                .dbPassword("admin")
                .build();

        dbService.connect();
        String sql = "CREATE TABLE IF NOT EXISTS STORE (" +
                "TYPE int, " +
                "VAL int)";
        if (!dbService.execute(sql)) {
            System.exit(1);
        }

        int buyS = 0, saleS = 0;
        ResultSet resultSet = dbService.executeQuery("select sum(val) from store where type = 1");
        if (resultSet.next()){
            buyS = resultSet.getInt(1);
        }
        resultSet = dbService.executeQuery("select sum(val) from store where type = 2");
        if (resultSet.next()){
            saleS = resultSet.getInt(1);
        }


        AtomicInteger productCount = new AtomicInteger(0);

        int countThread = 3;
        CountDownLatch cdl = new CountDownLatch(countThread * 2);

        ExecutorService es = Executors.newFixedThreadPool(countThread * 2);
//        ExecutorService es = Executors.newCachedThreadPool();
        for (int t = 1; t <= countThread; t++) {

            es.submit(new RunnableBuy("Поставщик  " + t,
                    productCount,
                    cdl,
                    dbService));

            es.submit(new RunnableSale("Потребитель " + t,
                    productCount,
                    cdl,
                    dbService));
        }

        es.shutdown();
//        while (!es.isTerminated()) {} // ждем выполнения потоков

        System.out.println("Ожидаем выполнения потоков...");
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(5000);

        int buy = 0, sale = 0;
        resultSet = dbService.executeQuery("select sum(val) from store where type = 1");
        if (resultSet.next()){
            buy = resultSet.getInt(1);
        }
        resultSet = dbService.executeQuery("select sum(val) from store where type = 2");
        if (resultSet.next()){
            sale = resultSet.getInt(1);
        }

        dbService.disconnect();
        System.out.println("Работа всех потоков завершена.");
        System.out.println("productCount.get() = " + productCount.get());
        System.out.println("SQL (buy - sale): " + (buy - sale - (buyS - saleS)));
    }


    public static class RunnableSale implements Runnable {

        private String name;
        private CountDownLatch cdl;
        private AtomicInteger productCount;
        private DBService dbService;

        private RunnableSale(
                String name,
                AtomicInteger productCount,
                CountDownLatch cdl,
                DBService dbService) {
            this.name = name;
            this.productCount = productCount;
            this.cdl = cdl;
            this.dbService = dbService;
            System.out.println("Запущен поток " + name);
        }

        @Override
        public void run() {
            for (int r = 0; r < 10; r++) {
                int prevCount = productCount.get();
                int value = (int) (Math.random() * 100) + 1;
                if (prevCount < value) {
                    System.out.println(r+1 + " | [" + name + "] остаток " + prevCount + " недостаточен для совершения операции - " + value);
                } else {
                    for (int i = 0; i < value; i++) {
                        productCount.decrementAndGet();
                    }
                    System.out.println(r+1 + " | [" + name + "] " + prevCount + " - " + value + " = " + productCount.get());
                    dbService.executeUpdate("insert into store (type, val) values (2, " + value + ")");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cdl.countDown();
            if (cdl.getCount() == 0) { // последний поток закончил свою работу
                System.out.println("последний поток RunnableSale закончил свою работу");
            } else {
                System.out.println("Поток [" + name + "] закончил свою работу, осталось потоков " + cdl.getCount());
            }
        }
    }


    public static class RunnableBuy implements Runnable {

        private String name;
        private CountDownLatch cdl;
        private AtomicInteger productCount;
        private DBService dbService;

        private RunnableBuy(
                String name,
                AtomicInteger productCount,
                CountDownLatch cdl,
                DBService dbService) {
            this.name = name;
            this.productCount = productCount;
            this.cdl = cdl;
            this.dbService = dbService;
            System.out.println("Запущен поток " + name);
        }

        @Override
        public void run() {
            for (int r = 0; r < 10; r++) {
                int prevCount = productCount.get();
                int value = (int) (Math.random() * 100) + 1;
                for (int i = 0; i < value; i++) {
                    productCount.incrementAndGet();
                }
                System.out.println(r+1 + " | [" + name + "] " + prevCount + " + " + value + " = " + productCount.get());
                dbService.executeUpdate("insert into store (type, val) values (1, " + value + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cdl.countDown();
            if (cdl.getCount() == 0) { // последний поток закончил свою работу
                System.out.println("последний поток RunnableBuy закончил свою работу");
            } else {
                System.out.println("Поток [" + name + "] закончил свою работу, осталось потоков " + cdl.getCount());
            }
        }
    }

}
