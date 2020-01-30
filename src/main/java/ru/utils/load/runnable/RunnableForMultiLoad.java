package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;
import ru.utils.load.ScriptRun;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * Created by SBT-Belov-SeA on 23.01.2020
 */
public class RunnableForMultiLoad implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableForMultiLoad.class);

    private final String name;
    private ScriptRun baseScript;
    private CountDownLatch countDownLatch;
    private ExecutorService executorService;
    private MultiRunService multiRunService;

    public RunnableForMultiLoad(
            int threadNum,
            ScriptRun baseScript,
            CountDownLatch countDownLatch,
            ExecutorService executorService,
            MultiRunService multiRunService
    ) {
        this.name = "RunnableForMultiLoad" + threadNum;
        LOG.info("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.countDownLatch = countDownLatch;
        this.executorService = executorService;
        this.multiRunService = multiRunService;
    }

    @Override
    public void run() {
        LOG.info("Старт потока {}", name);
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.getPacingType() == 0) { // не ждем завершения выполнения
                executorService.submit(new RunnableForMultiLoadNotWait(
                        name,
                        baseScript,
                        multiRunService));
            } else {
                String rqUid = UUID.randomUUID().toString().replaceAll("-", "");
                multiRunService.callListAdd(new Call(rqUid, start)); // фиксируем вызов
                if (baseScript.start()) {
                    multiRunService.setTimeEndInCall(rqUid, System.currentTimeMillis()); // сохраняем длительность выполнения
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
        countDownLatch.countDown();
    }


    /**
     * задержка в мс
     *
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
