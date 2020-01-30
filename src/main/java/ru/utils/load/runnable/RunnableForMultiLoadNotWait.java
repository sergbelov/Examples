package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;

import java.util.UUID;

/**
 * Created by SBT-Belov-SeA on 24.01.2020
 */
public class RunnableForMultiLoadNotWait implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableForMultiLoadNotWait.class);

    private final String name;
    private ScriptRun baseScript;
    private MultiRunService multiRunService;

    public RunnableForMultiLoadNotWait(
            String threadName,
            ScriptRun baseScript,
            MultiRunService multiRunService
    ) {
        this.name = threadName + "_NotWait";
        this.baseScript = baseScript;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        String rqUid = UUID.randomUUID().toString().replaceAll("-", "");
        multiRunService.callListAdd(new Call(rqUid, start)); // фиксируем вызов
        if (baseScript.start()) {
            multiRunService.setTimeEndInCall(rqUid, System.currentTimeMillis()); // сохраняем длительность выполнения
        }
    }
}
