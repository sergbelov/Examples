package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyRunnable1 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name = "MyRunnable1";
    private int countThread;
    private FormReport formReport;
    private FormProgressBar formProgressBar;
    private CountDownLatch cdl;
    List<String> list;
    int max;
    private int threadInfo;

    public MyRunnable1(
            int countThread,
            CountDownLatch cdl,
            List<String> list,
            int max,
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.countThread = countThread;
        this.cdl = cdl;
        this.list = list;
        this.max = max;
        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
        this.threadInfo = formProgressBar.getJLabelsInfoFree();
        formProgressBar.getJLabelsInfo(threadInfo).setText("Старт " + name);
    }


    @Override
    public void run() {
        int c = 0;
        for (int i = 0; i < 1000; i++) {
            formProgressBar.getJLabelsInfo(threadInfo).setText("Процесс " + name + " " + i);
            try {
                LOG.info("Запрос через UI: {}", i);
                formProgressBar.getJLabelsDur(0).setText("Запрос: " + i);
                formProgressBar.getJLabelsDur(0).repaint();
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c++;
        }
        formProgressBar.pictLabelHide();
        formProgressBar.getJLabelsDur(0).setText(formProgressBar.getDurationTimeString());
        formProgressBar.getJLabelsStage(0).setText("Количество записей: " + c);

        cdl.countDown();
        formProgressBar.getJLabelsInfo(threadInfo).setText("");

        CountDownLatch cdl2 = new CountDownLatch(countThread);
        ExecutorService es = Executors.newFixedThreadPool(countThread);

        // 2
        for (int t = 0; t < countThread; t++) {
            es.submit(
                    new MyRunnable4(
                            t,
                            max,
                            list,
                            cdl,
                            cdl2,
                            formReport,
                            formProgressBar
                    ));
        }
    }
}
