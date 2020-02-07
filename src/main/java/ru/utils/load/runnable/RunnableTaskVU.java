package ru.utils.load.runnable;

import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by SBT-Belov-SeA on 24.01.2020
 */
public class RunnableTaskVU implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableTaskVU.class);

    private final String name;
    private ScriptRun baseScript;
    private List<Call> callListVU;
    private MultiRunService multiRunService;

    public RunnableTaskVU(
            String name,
            ScriptRun baseScript,
            List<Call> callListVU,
            MultiRunService multiRunService
    ) {
        this.name = name + "_Task";
        LOG.trace("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.callListVU = callListVU;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        LOG.trace("Старт потока {}, всего потоков {}", name, multiRunService.getThreadCount());
        multiRunService.threadInc(); // счетчик активных потоков
        long start = System.currentTimeMillis();
        if (baseScript.start(multiRunService.getApiNum())) {
            long stop = System.currentTimeMillis();
            long curDur = stop - start;
//            if (curDur > multiRunService.getPacing()) {
//                LOG.warn("Длительность выполнения {} превышает pacing ({} > {})", name, curDur, multiRunService.getPacing());
//            }
            synchronized (callListVU) {
                callListVU.add(new Call(
                        start,
                        stop)); // фиксируем вызов
            }
        } else {
            synchronized (callListVU) {
                callListVU.add(new Call(start)); // фиксируем вызов
            }
        }
        multiRunService.threadDec();
        LOG.trace("Остановка потока {}, осталось {}", name, multiRunService.getThreadCount());
    }
}
