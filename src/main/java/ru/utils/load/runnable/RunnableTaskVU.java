package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;

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
        this.baseScript = baseScript;
        this.callListVU = callListVU;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        multiRunService.threadInc(); // счетчик активных потоков
        long start = System.currentTimeMillis();
        if (baseScript.start()) {
            synchronized (callListVU) {
                callListVU.add(new Call(
                        start,
                        System.currentTimeMillis())); // фиксируем вызов
            }
        } else {
            synchronized (callListVU) {
                callListVU.add(new Call(start)); // фиксируем вызов
            }
        }
        multiRunService.threadDec();
    }
}
