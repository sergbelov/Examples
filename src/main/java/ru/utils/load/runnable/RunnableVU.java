package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRunService;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class RunnableVU implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RunnableVU.class);

    private final String name;
    private ScriptRun baseScript;
    private List<Call> callList;
    private MultiRunService multiRunService;
    private ExecutorService executorService;

    public RunnableVU(
            String name,
            ScriptRun baseScript,
            List<Call> callList,
            MultiRunService multiRunService,
            ExecutorService executorService
    ) {
        this.name = name;
        LOG.debug("Инициализация потока {}", name);
        this.baseScript = baseScript;
        this.callList = callList;
        this.multiRunService = multiRunService;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        multiRunService.startThread(); // счетчик активных потоков
        LOG.info("Старт потока {}, всего потоков {}", name, multiRunService.getThreadCount());
        while (multiRunService.isRunning() && System.currentTimeMillis() < multiRunService.getTestStopTime()) {
            long start = System.currentTimeMillis();
            if (multiRunService.getPacingType() == 0) { // не ждем завершения выполнения
                executorService.submit(new RunnableTaskVU(
                        name,
                        baseScript,
                        callList,
                        multiRunService));
            } else {
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
            }

            if (multiRunService.getPacingType() == 0 || multiRunService.getPacingType() == 2) {
                sleep(multiRunService.getPacing()); // задержка перед запуском следующей итерации
            } else {
                long curDur = System.currentTimeMillis() - start;
                if (multiRunService.getPacing() > curDur) {
                    sleep(multiRunService.getPacing() - curDur); // задержка перед запуском следующей итерации
//                } else if (curDur > multiRunService.getPacing()) {
//                    LOG.warn("Длительность выполнения {} превышает pacing ({} > {})", name, curDur, multiRunService.getPacing());
                }
            }
        }
        multiRunService.stopThread();
        multiRunService.stopVU();
        multiRunService.vuListAdd();
        LOG.info("Остановка потока {}, осталось {}", name, multiRunService.getThreadCount());
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
