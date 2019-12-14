package ru.examples.threadExample;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Сергей on 24.02.2018.
 */
public class ThreadExample {

    //    static Map<Long, String> map = new HashMap<>();
//    static Map<Long, String> map = new TreeMap<>();
    static Map<Long, String> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {

        MyThread thread1 = new MyThread("Яйцо Thread");
        MyThread thread2 = new MyThread("Курица Thread");
        MyThread thread3 = new MyThread("Динозавр Thread");

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();
/*
        while (thread1.isAlive() && thread2.isAlive() && thread3.isAlive()){}
*/

        MyRunnable runnable1 = new MyRunnable("Яйцо Runnable");
        MyRunnable runnable2 = new MyRunnable("Курица Runnable");
        MyRunnable runnable3 = new MyRunnable("Динозавр Runnable");

        runnable1.thread.join();
        runnable2.thread.join();
        runnable3.thread.join();

        System.out.println("=====================");
        map.forEach((l, s) -> System.out.println(l + " : " + s));

        System.out.println("=====================");
        Map<Long, String> treeMap = new TreeMap<>(map);
        treeMap.forEach((l, s) -> System.out.println(l + " : " + s));
    }


    static class MyThread extends Thread {
        /*
                getName() - получить имя потока
                getPriority() - получить приоритет потока
                isAlive() - определить, выполняется ли поток
                join() - ожидать завершение потока
                run() - запуск потока. В нём пишите свой код
                sleep() - приостановить поток на заданное время
                start() - запустить поток
        */
        public MyThread(String name) {
            this.setName(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(this.getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                map.put(System.nanoTime(), this.getName());
            }
        }
    }


    static class MyRunnable implements Runnable {
        Thread thread;
        String name;

        MyRunnable(String name) {
            this.name = name;
            thread = new Thread(this, name);
            thread.start();
        }

        public String getName() {
            return this.name;
        }

        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(this.getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                map.put(System.nanoTime(), this.getName());
            }
        }
    }
}
