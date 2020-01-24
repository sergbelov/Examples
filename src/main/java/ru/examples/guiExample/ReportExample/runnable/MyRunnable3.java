package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

public class MyRunnable3 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name = "MyRunnable3";
    FormReport formReport;
    FormProgressBar formProgressBar;
    int threadInfo;

    public MyRunnable3(
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
        LOG.info("Инициализация " + name);
    }


    @Override
    public void run() {
        threadInfo = formProgressBar.getJLabelsInfoFree();
        formProgressBar.getJLabelsInfo(threadInfo).setText("Старт " + name);
        String data;
        for (int i = 0; i < 1000; i++) {
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
                Thread.sleep((int) (Math.random() * 10));
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        formProgressBar.getJLabelsInfo(threadInfo).setText("");
    }
}
