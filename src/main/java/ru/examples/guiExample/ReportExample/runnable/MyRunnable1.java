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
    private int countThreadMax;
    private ExecutorService es;
    private FormReport formReport;
    private FormProgressBar formProgressBar;
    List<String> listSource;
    List<String> listTarget;
    private int threadInfo;

    public MyRunnable1(
            int countThread,
            ExecutorService es,
            List<String> listSource,
            List<String> listTarget,
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.countThreadMax = countThread;
        this.es = es;
        this.listSource = listSource;
        this.listTarget = listTarget;
        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
        LOG.info("Инициализация " + name);
    }


    @Override
    public void run() {
        this.threadInfo = formProgressBar.getJLabelsInfoFree();
        formProgressBar.getJLabelsInfo(threadInfo).setText("Старт " + name);
        formProgressBar.pictLabelSetVisible(true);
        int count = 0;
        for (int i = 0; i < 1000; i++) {
            formProgressBar.getJLabelsInfo(threadInfo).setText("Процесс " + name + " " + i);
            try {
                synchronized (listSource) {
                    listSource.add("запись " + i);
                }
                LOG.info("Запрос: {}", i);
                formProgressBar.getJLabelsDur(0).setText("Запрос: " + i);
//                formProgressBar.getJLabelsDur(0).repaint();
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        formProgressBar.pictLabelSetVisible(false);
        formProgressBar.getJLabelsDur(0).setText(formProgressBar.getDurationTimeString());
        formProgressBar.getJLabelsStage(0).setText("Количество записей: " + count);

        formProgressBar.getJLabelsInfo(threadInfo).setText("");

        int t = 0;
        int c = 0;
        int countThread = Math.min(countThreadMax, count);
        int step = (int) Math.ceil(count * 1.00 / countThread);
        countThread = (int) Math.ceil(count * 1.00 / step); // корректировка из-за округления

        LOG.info("countThread: {}", countThread);
        CountDownLatch cdl = new CountDownLatch(countThread);
        ExecutorService es4 = Executors.newFixedThreadPool(countThread);

        // 4 обработка полученных данных в несколько потоков
        while (c < count) {
            t++;
            es4.submit(
                    new MyRunnable4(
                            t,
                            listSource,
                            listTarget,
                            c,
                            Math.min(c + step, count),
                            cdl,
                            es,
                            formReport,
                            formProgressBar
                    ));
            c = Math.min(c + step, count);
        }
        es4.shutdown();
    }
}
