package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyRunnable5 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name = "MyRunnable5";
    List<String> listTarget;
    CountDownLatch cdl;
    FormReport formReport;
    FormProgressBar formProgressBar;
    int threadInfo;

    public MyRunnable5(
            List<String> listTarget,
            CountDownLatch cdl,
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.listTarget = listTarget;
        this.cdl = cdl;
        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
        LOG.info("Инициализация " + name);
    }


    @Override
    public void run() {
        threadInfo = formProgressBar.getJLabelsInfoFree();
        formProgressBar.getJLabelsInfo(threadInfo).setText("Старт " + name);
        for (int i = 0; i < listTarget.size(); i++) {
            LOG.info(listTarget.get(i));
            formProgressBar.getJLabelsInfo(threadInfo).setText("Процесс " + name + " " + i);
            try {
                Thread.sleep((int) (Math.random() * 10));
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }

        formProgressBar.getJLabelsInfo(threadInfo).setText("");
        cdl.countDown();

        if (cdl.getCount() == 0) {
            formReport.getBCreateReport().setEnabled(true);
//            formProgressBar.getBReport().setVisible(true);
//            formProgressBar.getBClose().setVisible(true);
        }
    }
}
