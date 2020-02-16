package ru.utils.load.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.ScriptRun;
import ru.utils.load.data.Call;
import ru.utils.load.utils.MultiRun;
import ru.utils.load.utils.MultiRunService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Belov Sergey
 */
public class RunnableLoadAPI implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RunnableLoadAPI.class);

    private final String name;
    private ScriptRun baseScript;
    private MultiRunService multiRunService;
    private CountDownLatch countDownLatch;

    public RunnableLoadAPI(
            String name,
            ScriptRun baseScript,
            MultiRunService multiRunService,
            CountDownLatch countDownLatch
    ) {
        this.name = name + "_Task";
        this.baseScript = baseScript;
        this.multiRunService = multiRunService;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            multiRunService.start(baseScript);
        } catch (Exception e) {
            LOG.error("", e);
        }
        countDownLatch.countDown();
    }
}
