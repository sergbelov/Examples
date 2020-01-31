package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.loadExample.MultiLoad;
import ru.utils.load.utils.MultiRunService;

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
    private MultiRunService multiRunService;

    public RunnableForMultiLoadAwait(
            CountDownLatch countDownLatch,
            ExecutorService executorService,
            MultiRunService multiRunService
    ) {
        LOG.info("Инициализация потока {}", name);
        this.countDownLatch = countDownLatch;
        this.executorService = executorService;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
//        while (countDownLatch.getCount() > 1 | !executorService.isTerminated()) {
        while (System.currentTimeMillis() < multiRunService.getTestStopTime() &&
                multiRunService.isRunning() &&
                !executorService.isTerminated()) {
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
