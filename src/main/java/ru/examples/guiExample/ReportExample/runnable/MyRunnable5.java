package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

import java.util.concurrent.CountDownLatch;

public class MyRunnable5 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name = "MyRunnable5";
    FormReport formReport;
    FormProgressBar formProgressBar;
    int threadInfo;

    public MyRunnable5(
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
        this.threadInfo = formProgressBar.getJLabelsInfoFree();
        formProgressBar.getJLabelsInfo(threadInfo).setText("Старт " + name);
    }


    @Override
    public void run() {
        int delay;
        String data;
        for (int i = 1; i <= 1000; i++) {
            formProgressBar.getJLabelsInfo(threadInfo).setText("Процесс " + name + " " + i);
            data = name + "_" + i;
            LOG.info(data);

            try {
                delay = (int) (Math.random() * 10);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        formProgressBar.getJLabelsInfo(threadInfo).setText("");
        formReport.getBCreateReport().setEnabled(true);
//            formProgressBar.getBReport().setVisible(true);
//            formProgressBar.getBClose().setVisible(true);
    }
}
