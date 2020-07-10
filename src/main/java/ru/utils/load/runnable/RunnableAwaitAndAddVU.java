package ru.utils.load.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Belov Sergey
 * Проверяем наличие не выполненных задач
 * Добавленние новой группы VU
 */
public class RunnableAwaitAndAddVU implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RunnableAwaitAndAddVU.class);

    private final String name;
    private CountDownLatch countDownLatch;
    private MultiRunService multiRunService;

    public RunnableAwaitAndAddVU(
            String name,
            CountDownLatch countDownLatch,
            MultiRunService multiRunService
    ) {
        this.name = name;
        LOG.debug("Инициализация потока {}", name);
        this.countDownLatch = countDownLatch;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
//        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
        while (System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            multiRunService.startGroupVU(); // старт новой группы VU (если нужно)
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
        }
        while (multiRunService.getThreadCount() > 0){ // ждем завершения работы заданий
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
        }
        countDownLatch.countDown();
        LOG.info("Остановка потока {}", name);
    }
}
