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
import java.util.concurrent.CopyOnWriteArrayList;
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
        LOG.debug("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.executorService = executorService;
        this.multiRunService = multiRunService;
    }

    @Override
    public List<Call> call() throws Exception {
        multiRunService.threadInc(); // счетчик активных потоков
        List<Call> callListVU = new CopyOnWriteArrayList<>();
        LOG.info("Старт потока {}, всего активных потоков {}", name, multiRunService.getThreadCount());
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.getPacingType() == 0) { // не ждем завершения выполнения
                executorService.submit(new RunnableTaskVU(
                        name,
                        baseScript,
                        callListVU,
                        multiRunService));
            } else {
                if (baseScript.start()) {
                    callListVU.add(new Call(
                            start,
                            System.currentTimeMillis())); // фиксируем вызов
                } else {
                    callListVU.add(new Call(start)); // фиксируем вызов
                }
            }

            if (multiRunService.getPacingType() == 0 || multiRunService.getPacingType() == 2) {
                sleep(multiRunService.getPacing()); // задержка перед запуском следующей итерации
            } else {
                long curDur = System.currentTimeMillis() - start;
                if (multiRunService.getPacing() > curDur) {
                    sleep(multiRunService.getPacing() - curDur); // задержка перед запуском следующей итерации
                }
            }
        }
        multiRunService.threadDec();
        multiRunService.stopVU();
        LOG.info("Остановка потока {}, осталось {}",
                name,
                multiRunService.getThreadCount());
        return callListVU;
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
