package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.utils.MultiRunService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class RunnableVU implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableVU.class);
    private final int thread;
    private final String name;
    private MultiRunService multiRunService;

    public RunnableVU(int thread, MultiRunService multiRunService) {
        this.thread = thread;
        this.name = multiRunService.getName() + " VU" + thread;
        LOG.debug("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
        multiRunService.vuListActiveAdd(thread);
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        LOG.info("Старт потока {}, Threads: {}", name, threadNum);
        ExecutorService executorService = multiRunService.getExecutorService();
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.isActiveVU(thread)) { // разрешена активность данного VU
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
            } else { // ToDo:
                break;
//                sleep(1000);
            }
        }
        threadNum = multiRunService.stopThread();
        multiRunService.stopVU();
        multiRunService.vuListAdd();
        multiRunService.vuListActiveDel(thread);
        LOG.info("Остановка потока {}, Threads: {}", name, threadNum);
    }

    /**
     * задержка в мс
     * @param delay
     */
    private void sleep(long delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
    }

}
