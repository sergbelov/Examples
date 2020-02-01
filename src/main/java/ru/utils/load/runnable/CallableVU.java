package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class CallableVU implements Callable<List<Call>> {

    private static final Logger LOG = LogManager.getLogger(CallableVU.class);

    private final String name;
    private ScriptRun baseScript;
    private ExecutorService executorService;
    private MultiRunService multiRunService;

    public CallableVU(
            int threadNum,
            ScriptRun baseScript,
            ExecutorService executorService,
            MultiRunService multiRunService
    ) {
        this.name = "CallableVU" + threadNum;
        LOG.info("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.executorService = executorService;
        this.multiRunService = multiRunService;
    }

    @Override
    public List<Call> call() throws Exception {
        List<Call> callList = new ArrayList<>();
        LOG.info("Старт потока {}", name);
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.getPacingType() == 0) { // не ждем завершения выполнения
                executorService.submit(new RunnableTaskVU(
                        name,
                        baseScript,
                        callList));
//                        multiRunService));
            } else {
                String rqUid = UUID.randomUUID().toString().replaceAll("-", "");
/*
                sleep((int) (Math.random() * 2000));
                callList.add(new Call(
                        rqUid,
                        start,
                        System.currentTimeMillis())); // фиксируем вызов
*/
                if (baseScript.start()) {
                    callList.add(new Call(
                            rqUid,
                            start,
                            System.currentTimeMillis())); // фиксируем вызов
                } else {
                    callList.add(new Call(
                            rqUid,
                            start)); // фиксируем вызов
                }
            }

            long dur = (long) (multiRunService.getPacing() * 1000);
            if (multiRunService.getPacingType() == 0 || multiRunService.getPacingType() == 2) {
                sleep(dur); // задержка перед запуском следующей итерации
            } else {
                long curDur = System.currentTimeMillis() - start;
                if (dur > curDur) {
                    sleep(dur - curDur); // задержка перед запуском следующей итерации
                }
            }
        }
        LOG.info("Остановка потока {}", name);
        return callList;
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
