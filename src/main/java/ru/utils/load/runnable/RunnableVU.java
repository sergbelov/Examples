package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.ExecutorService;

public class RunnableVU implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableVU.class);
    private final int thread;
    private final String name;
    private MultiRunService multiRunService;

    public RunnableVU(int thread, MultiRunService multiRunService) {
        this.thread = thread;
        this.name = multiRunService.getName() + " RunnableVU" + thread;
        LOG.debug("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        LOG.info("Старт потока {}, Threads: {}", name, threadNum);
        ExecutorService executorService = multiRunService.getExecutorService();
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.getPacingType() == 0) { // не ждем завершения выполнения
                executorService.submit(new RunnableTaskVU(thread, multiRunService));
            } else {
                multiRunService.callListAdd(start, thread);
            }

            if (multiRunService.getPacingType() == 0 || multiRunService.getPacingType() == 2) {
                sleep(multiRunService.getPacing()); // задержка перед запуском следующей итерации
            } else {
                long curDur = System.currentTimeMillis() - start;
                if (multiRunService.getPacing() > curDur) {
                    sleep(multiRunService.getPacing() - curDur); // задержка перед запуском следующей итерации
//                } else if (curDur > multiRunService.getPacing()) {
//                    LOG.warn("Длительность выполнения API {} превышает pacing ({} > {})", name, curDur, multiRunService.getPacing());
                }
            }
        }
        threadNum = multiRunService.stopThread();
        multiRunService.stopVU();
        multiRunService.vuListAdd();
        LOG.info("Остановка потока {}, Threads: {}", name, threadNum);
    }

    /**
     * задержка в мс
     * @param delay
     */
    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
