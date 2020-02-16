package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Belov Sergey
 * Проверяем наличие не выполненных задач
 * Добавленние новой группы VU
 */
public class RunnableAwaitAndAddVU implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableAwaitAndAddVU.class);

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

        //ToDo отладить при прерывании теста
        while (multiRunService.isRunning() && (
                System.currentTimeMillis() < multiRunService.getTestStopTime() ||
                multiRunService.getThreadCount() > 0)) {

            multiRunService.startGroupVU(); // старт новой группы VU (если нужно)
        }
        while (multiRunService.getThreadCount() > 0){} // ждем завершения работы заданий
        countDownLatch.countDown();
        LOG.info("Остановка потока {}", name);
    }
}
