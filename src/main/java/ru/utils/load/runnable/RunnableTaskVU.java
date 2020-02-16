package ru.utils.load.runnable;

import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by Belov Sergey
 */
public class RunnableTaskVU implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableTaskVU.class);

    private final String name;
    private ScriptRun baseScript;
    private List<Call> callList;
    private MultiRunService multiRunService;

    public RunnableTaskVU(
            String name,
            ScriptRun baseScript,
            List<Call> callList,
            MultiRunService multiRunService
    ) {
        this.name = name + "_Task";
        LOG.trace("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.callList = callList;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        int threadNum = multiRunService.startThread(); // счетчик активных потоков
        LOG.debug("Старт потока {}, Threads: {}", name, threadNum);
        long start = System.currentTimeMillis();
        if (multiRunService.isAsync()){ // асинхронный вызов, не ждем завершения выполнения
            callList.add(new Call(start)); // фиксируем вызов
            try {
                baseScript.start(multiRunService.getApiNum());
            } catch (Exception e) {
                multiRunService.errorListAdd(name, e);
            }
        } else { // синхронный вызов, ждем завершения выполнения
            try {
                baseScript.start(multiRunService.getApiNum());
                callList.add(new Call(start, System.currentTimeMillis())); // фиксируем вызов
            } catch (Exception e) {
                callList.add(new Call(start)); // фиксируем вызов
                multiRunService.errorListAdd(name, e);
            }
        }
        threadNum = multiRunService.stopThread();
        LOG.debug("Остановка потока {}, Treads: {}", name, threadNum);
    }
}
