package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyRunnable6 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name = "MyRunnable6";
    List<String> listTarget;
    CountDownLatch cdl;
    FormReport formReport;
    FormProgressBar formProgressBar;
    int threadInfo;

    public MyRunnable6(
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

        LOG.info("\n======================================\n" +
                "========== Вывод результата ==========\n" +
                "size: {}", listTarget.size());

        for (String string : listTarget) {
            formProgressBar.getJLabelsInfo(threadInfo).setText("Процесс " + name + " " + string);
            LOG.info("Результат: {}", string);
            formProgressBar.getJProgressBars(2).setValue(formProgressBar.getJProgressBars(2).getValue()+1);
            formProgressBar.getJProgressBars(2).repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        formProgressBar.getJLabelsDur(2).setText(formProgressBar.getDurationTimeString());
        formProgressBar.getJLabelsStage(2).setText("Количество 2 этап: " + listTarget.size());
        formProgressBar.getJLabelsInfo(threadInfo).setText("");
        cdl.countDown();
        if (cdl.getCount() == 0) {
            formReport.getBCreateReport().setEnabled(true);
//            formProgressBar.getBReport().setVisible(true);
//            formProgressBar.getBClose().setVisible(true);
        }
    }
}
