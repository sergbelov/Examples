package ru.examples.threadExample;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorServiceExample3 {

    private int threadCount = 10;
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public int getAtomicInteger() {
        return atomicInteger.get();
    }

    public int inc() {
        return atomicInteger.incrementAndGet();
    }

    public int dec() {
        return atomicInteger.decrementAndGet();
    }

    public void run() {
        CountDownLatch countDownLatch = new CountDownLatch(threadCount * 2);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(new RunnableInc(i, countDownLatch, this));
            executorService.submit(new RunnableDec(i, countDownLatch, this));
        }
        try {
            countDownLatch.await(); // ждем завершения работы всех потоков
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        System.out.println(atomicInteger.get());
    }



    public static void main(String[] args) {
        ExecutorServiceExample3 executorServiceExample3 = new ExecutorServiceExample3();
        executorServiceExample3.run();
    }

}




class RunnableInc implements Runnable {
    final String name;
    CountDownLatch countDownLatch;
    ExecutorServiceExample3 executorServiceExample3;

    public RunnableInc(
            int i,
            CountDownLatch countDownLatch,
            ExecutorServiceExample3 executorServiceExample3) {
        this.name = "RunnableInc" + i;
        this.countDownLatch = countDownLatch;
        this.executorServiceExample3 = executorServiceExample3;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            int atomicIntegerValue = executorServiceExample3.inc();
            int atomicIntegerGetValue = executorServiceExample3.getAtomicInteger();
            System.out.println(name + " " +
                    atomicIntegerValue + " " +
                    atomicIntegerGetValue + " " +
                    (atomicIntegerValue != atomicIntegerGetValue ? " !!!" : ""));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }
}

class RunnableDec implements Runnable {
    final String name;
    CountDownLatch countDownLatch;
    ExecutorServiceExample3 executorServiceExample3;

    public RunnableDec(
            int i,
            CountDownLatch countDownLatch,
            ExecutorServiceExample3 executorServiceExample3) {
        this.name = "RunnableDec" + i;
        this.countDownLatch = countDownLatch;
        this.executorServiceExample3 = executorServiceExample3;
    }

    @Override
    public void run() {
        for (int i = 0; i < 50; i++) {
            int atomicIntegerValue = executorServiceExample3.dec();
            int atomicIntegerGetValue = executorServiceExample3.getAtomicInteger();
            System.out.println(name + " " +
                    atomicIntegerValue + " " +
                    atomicIntegerGetValue + " " +
                    (atomicIntegerValue != atomicIntegerGetValue ? " !!!" : ""));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }
}

