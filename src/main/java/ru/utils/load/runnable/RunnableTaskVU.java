package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;

import java.util.List;
import java.util.UUID;

/**
 * Created by SBT-Belov-SeA on 24.01.2020
 */
public class RunnableTaskVU implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableTaskVU.class);

    private final String name;
    private ScriptRun baseScript;
    private List<Call> callList;
//    private MultiRunService multiRunService;

    public RunnableTaskVU(
            String threadName,
            ScriptRun baseScript,
            List<Call> callList
//            MultiRunService multiRunService
    ) {
        this.name = threadName + "_Task";
        this.baseScript = baseScript;
        this.callList = callList;
//        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        String rqUid = UUID.randomUUID().toString().replaceAll("-", "");
        if (baseScript.start()) {
            synchronized (callList) {
                callList.add(new Call(
                        rqUid,
                        start,
                        System.currentTimeMillis())); // фиксируем вызов
            }
        } else {
            synchronized (callList) {
                callList.add(new Call(
                        rqUid,
                        start)); // фиксируем вызов
            }
        }

/*
        multiRunService.callListAdd(new Call(rqUid, start)); // фиксируем вызов
        if (baseScript.start()) {
            multiRunService.setTimeEndInCall(rqUid, System.currentTimeMillis()); // сохраняем длительность выполнения
        }
*/
    }
}
