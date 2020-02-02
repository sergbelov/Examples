package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.CountDownLatch;

/**
 * Created by SBT-Belov-SeA on 24.01.2020
 * Проверяем наличие не выполненных задач
 */
public class RunnableAddVUAwait implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableAddVUAwait.class);

    private final String name = "RunnableAwait";
    private CountDownLatch countDownLatch;
    private MultiRunService multiRunService;

    public RunnableAddVUAwait(
            CountDownLatch countDownLatch,
            MultiRunService multiRunService
    ) {
        LOG.debug("Инициализация потока {}", name);
        this.countDownLatch = countDownLatch;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
        while (multiRunService.isRunning() && (
                System.currentTimeMillis() < multiRunService.getTestStopTime() ||
                multiRunService.getThreadCount() > 0)) {

            multiRunService.startGroupVU();

/*
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
        }
        countDownLatch.countDown();
        LOG.info("Остановка потока {}", name);
    }
}
