package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

import java.util.concurrent.CountDownLatch;

public class MyRunnable3 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name = "MyRunnable4";
    FormReport formReport;
    FormProgressBar formProgressBar;
    CountDownLatch cdl;
    int threadInfo;

    public MyRunnable3(
            CountDownLatch cdl,
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.cdl = cdl;
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

/*
            synchronized (formProgressBar.getJProgressBars(1)){
                formProgressBar.getJProgressBars(1).setValue(formProgressBar.getJProgressBars(1).getValue() + 1);
                formProgressBar.getJProgressBars(1).repaint();
            }
*/


            try {
                delay = (int) (Math.random() * 10);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        cdl.countDown();
        formProgressBar.getJLabelsInfo(threadInfo).setText("");
    }
}
