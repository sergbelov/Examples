package ru.utils.load.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.utils.load.utils.MultiRunService;

/**
 * Created by Belov Sergey
 */
public class RunnableTaskVU implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RunnableTaskVU.class);
    private final int thread;
    private final String name;
    private MultiRunService multiRunService;

    public RunnableTaskVU(int thread, MultiRunService multiRunService) {
        this.thread = thread;
        this.name = multiRunService.getName() + " VU" + thread + "_Task";
        LOG.trace("Инициализация потока {}", name);
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        LOG.debug("Старт потока {}, Threads: {}", name, threadNum);
        long start = System.currentTimeMillis();
        multiRunService.callListAdd(start, thread);
        threadNum = multiRunService.stopThread();
        LOG.debug("Остановка потока {}, Treads: {}", name, threadNum);
    }
}
