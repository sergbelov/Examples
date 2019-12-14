package ru.examples.threadExample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Сергей on 27.03.2019.
 */
public class CallableExample {

    public static void main(String[] args) {

        ExecutorService es = Executors.newFixedThreadPool(10);

        List<Future<String>> futureList = new ArrayList<>();
        List<Future<List<String>>> futureListList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Future<String> future1 = es.submit(new MyCallable(i + 1));
            futureList.add(future1);
        }

        for (int i = 5; i < 10; i++) {
            Future<List<String>> future2 = es.submit(new MyCallableList(i + 1));
            futureListList.add(future2);
        }


        System.out.println("========== Вывод результата String ==========");

        for (Future<String> future1p : futureList) {
            try {
                System.out.println(future1p.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        System.out.println("========== Вывод результата List<String> ==========");

        for (Future<List<String>> future2p : futureListList) {
            try {
                for (String s : future2p.get()) {
                    System.out.println(s);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        es.shutdown();
        while (es.isTerminated()) {
        }

    }
}

class MyCallable implements Callable<String> {

    int num;

    public MyCallable(int num) {
        this.num = num;
    }

    @Override
    public String call() throws Exception {
        StringBuilder sb = new StringBuilder(Thread.currentThread().getName());
        sb.append(" ").append(num);
        for (int i = 0; i < 10; i++) {
            sb.append("; ").append(i);
//            System.out.println(Thread.currentThread().getName() + " " + i);
        }
        return sb.toString();
    }
}


class MyCallableList implements Callable<List<String>> {

    int num;

    public MyCallableList(int num) {
        this.num = num;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(Thread.currentThread().getName() + " " + i);
//            System.out.println(Thread.currentThread().getName() + " " + i);
        }
        return list;
    }
}