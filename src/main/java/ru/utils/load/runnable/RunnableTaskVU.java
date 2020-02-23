package ru.utils.load.runnable;

import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by Belov Sergey
 */
public class RunnableTaskVU implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableTaskVU.class);

    private final String name;
    private ScriptRun baseScript;
    private List<Call> callList;
    private MultiRunService multiRunService;
    private ExecutorService executorService;

    public RunnableTaskVU(
            String name,
            ScriptRun baseScript,
            List<Call> callList,
            MultiRunService multiRunService,
            ExecutorService executorService
    ) {
        this.name = name + "_Task";
        LOG.trace("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.callList = callList;
        this.multiRunService = multiRunService;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        LOG.debug("Старт потока {}, Threads: {}", name, threadNum);
        long start = System.currentTimeMillis();
        multiRunService.callListAdd(start, callList);
        threadNum = multiRunService.stopThread();
        LOG.debug("Остановка потока {}, Treads: {}", name, threadNum);
    }
}
