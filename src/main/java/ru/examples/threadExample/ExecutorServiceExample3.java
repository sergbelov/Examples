package ru.examples.threadExample;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorServiceExample3 {

    private int threadCount = 10;
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public AtomicInteger getAtomicInteger() {
        return atomicInteger;
    }

    public void inc(){
        atomicInteger.incrementAndGet();
    }

    public void dec(){
        atomicInteger.decrementAndGet();
    }

    public void run(){
        CountDownLatch countDownLatch = new CountDownLatch(threadCount * 2);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(new RunnableInc(i, countDownLatch, this));
            executorService.submit(new RunnableDec(i, countDownLatch, this));

//            executorService.submit(new RunnableInc(i, atomicInteger, countDownLatch));
//            executorService.submit(new RunnableDec(i, atomicInteger, countDownLatch));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        System.out.println(atomicInteger.get());
    }

    class RunnableInc implements Runnable {

        final String name;
        //    AtomicInteger atomicInteger;
        CountDownLatch countDownLatch;
        ExecutorServiceExample3 executorServiceExample3;

        //    public RunnableInc(int i, AtomicInteger atomicInteger, CountDownLatch countDownLatch) {
        public RunnableInc(int i, CountDownLatch countDownLatch, ExecutorServiceExample3 executorServiceExample3) {
            this.name = "RunnableInc" + i;
//        this.atomicInteger = atomicInteger;
            this.countDownLatch = countDownLatch;
            this.executorServiceExample3 = executorServiceExample3;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
//                atomicInteger.incrementAndGet();
//                System.out.println(name + " " + atomicInteger.get());
                executorServiceExample3.inc();
                System.out.println(name + " " + executorServiceExample3.getAtomicInteger());

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
        //    AtomicInteger atomicInteger;
        CountDownLatch countDownLatch;
        ExecutorServiceExample3 executorServiceExample3;

        //    public RunnableDec(int i, AtomicInteger atomicInteger, CountDownLatch countDownLatch) {
        public RunnableDec(int i, CountDownLatch countDownLatch, ExecutorServiceExample3 executorServiceExample3) {
            this.name = "RunnableDec" + i;
//        this.atomicInteger = atomicInteger;
            this.countDownLatch = countDownLatch;
            this.executorServiceExample3 = executorServiceExample3;
        }

        @Override
        public void run() {
            for (int i = 0; i < 50; i++) {
//                atomicInteger.decrementAndGet();
//                System.out.println(name + " " + atomicInteger.get());
                executorServiceExample3.dec();
                System.out.println(name + " " + executorServiceExample3.getAtomicInteger());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            countDownLatch.countDown();
        }
    }


    public static void main(String[] args) {
        ExecutorServiceExample3 executorServiceExample3 = new ExecutorServiceExample3();
        executorServiceExample3.run();
    }

}
