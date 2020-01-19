package ru.examples.guiExample.ReportExample.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.FormProgressBar;
import ru.examples.guiExample.ReportExample.FormReport;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyRunnable4 implements Runnable {

    static final Logger LOG = LogManager.getLogger();

    private String name;
    int min, max;
    FormReport formReport;
    FormProgressBar formProgressBar;
    List<String> listSource;
    List<String> listTarget;
    CountDownLatch cdl;
    ExecutorService es;

    public MyRunnable4(
            int num,
            List<String> listSource,
            List<String> listTarget,
            int min,
            int max,
            CountDownLatch cdl,
            ExecutorService es,
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.name = "MyRunnable4 " + num;
        this.listSource = listSource;
        this.listTarget = listTarget;
        this.min = min;
        this.max = max;
        this.cdl = cdl;
        this.es = es;
        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
        LOG.info("Инициализация {}, ({}-{})", name, min, max);
    }

    @Override
    public void run() {
        LOG.info("Старт {}, ({}-{})", name, min, max);

        String data;
        for (int i = min; i < max; i++) {
            data = name + " " + listSource.get(i);
            LOG.info(data);
            synchronized (listSource) {
                listTarget.add(data);
            }

            synchronized (formProgressBar.getJProgressBars(1)){
                formProgressBar.getJProgressBars(1).setValue(formProgressBar.getJProgressBars(1).getValue() + 1);
                formProgressBar.getJProgressBars(1).repaint();
            }

            try {
                Thread.sleep((int) (Math.random() * 100));
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        cdl.countDown();
        LOG.info("Стоп {}, ({}-{}) {}", name, min, max, cdl.getCount());

        if (cdl.getCount() == 0) { // последний поток закончил свою работу
            formProgressBar.getJLabelsDur(1).setText(formProgressBar.getDurationTimeString());
            formProgressBar.getJLabelsStage(1).setText("Количество 1 этап: " + listTarget.size());
            formProgressBar.getJProgressBars(2).setMaximum(listTarget.size());

            CountDownLatch cdl = new CountDownLatch(2);
//            ExecutorService es = Executors.newFixedThreadPool(2);

            // 5
            es.submit(
                    new MyRunnable5(
                            listTarget,
                            cdl,
                            formReport,
                            formProgressBar
                    ));
            // 6
            es.submit(
                    new MyRunnable6(
                            listTarget,
                            cdl,
                            formReport,
                            formProgressBar
                    ));

            es.shutdown();
        }
    }
}
