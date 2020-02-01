package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;
import ru.utils.load.ScriptRun;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Created by SBT-Belov-SeA on 23.01.2020
 */
public class RunnableVU implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableVU.class);

    private final String name;
    private ScriptRun baseScript;
    private ExecutorService executorService;
    private MultiRunService multiRunService;

    public RunnableVU(
            int threadNum,
            ScriptRun baseScript,
            ExecutorService executorService,
            MultiRunService multiRunService
    ) {
        this.name = "RunnableVU" + threadNum;
        LOG.info("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.executorService = executorService;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.getPacingType() == 0) { // не ждем завершения выполнения
                executorService.submit(new RunnableTaskVU(
                        name,
                        baseScript,
                        multiRunService.getCallList()));
//                        multiRunService));
            } else {
                String rqUid = UUID.randomUUID().toString().replaceAll("-", "");
//                multiRunService.callListAdd(new Call(rqUid, start)); // фиксируем вызов
                if (baseScript.start()) {
//                    multiRunService.setTimeEndInCall(rqUid, System.currentTimeMillis()); // сохраняем длительность выполнения
                    multiRunService.callListAdd(new Call(
                            rqUid,
                            start,
                            System.currentTimeMillis())); // фиксируем вызов
                } else {
                    multiRunService.callListAdd(new Call(
                            rqUid,
                            start)); // фиксируем вызов

                }
            }

            long dur = (long) (multiRunService.getPacing() * 1000);
            if (multiRunService.getPacingType() == 0 || multiRunService.getPacingType() == 2){
                sleep(dur); // задержка перед запуском следующей итерации
            } else {
                long curDur = System.currentTimeMillis() - start;
                if (dur > curDur) {
                    sleep(dur - curDur); // задержка перед запуском следующей итерации
                }
            }
        }
        LOG.info("Остановка потока {}", name);
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
