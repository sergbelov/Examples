package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * Created by SBT-Belov-SeA on 24.01.2020
 * Проверяем наличие не выполненных задач
 */
public class RunnableForMultiLoadAwait implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableForMultiLoadAwait.class);

    private final String name = "RunnableForMultiLoadAwait";
    private CountDownLatch countDownLatch;
    private ExecutorService executorService;

    public RunnableForMultiLoadAwait(
            CountDownLatch countDownLatch,
            ExecutorService executorService
    ) {
        LOG.info("Инициализация потока {}", name);
        this.countDownLatch = countDownLatch;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
//        while (countDownLatch.getCount() > 1 | !executorService.isTerminated()) {
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
        LOG.info("Остановка потока {}", name);
    }
}
