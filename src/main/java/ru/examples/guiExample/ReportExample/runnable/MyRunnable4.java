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

    int num, max;
    FormReport formReport;
    FormProgressBar formProgressBar;
    List<String> list;
    CountDownLatch cdl;

    public MyRunnable4(
            int num,
            int max,
            List<String> list,
            CountDownLatch cdl,
            FormReport formReport,
            FormProgressBar formProgressBar) {

        this.num = num;
        this.max = max;
        this.list = list;
        this.cdl = cdl;
        this.formReport = formReport;
        this.formProgressBar = formProgressBar;
    }


    @Override
    public void run() {
        int delay;
        String data;
        for (int i = 1; i <= max; i++) {
            data = "Thread" + num + "_" + i;
            synchronized (list) {
                list.add(data);
            }
            LOG.info(data);

            synchronized (formProgressBar.getJProgressBars(1)){
                formProgressBar.getJProgressBars(1).setValue(formProgressBar.getJProgressBars(1).getValue() + 1);
                formProgressBar.getJProgressBars(1).repaint();
            }

            try {
                delay = (int) (Math.random() * 1000);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        cdl.countDown();

        if (cdl.getCount() == 0) { // последний поток закончил свою работу
            formProgressBar.getJLabelsDur(1).setText(formProgressBar.getDurationTimeString());
            formProgressBar.getJLabelsStage(1).setText("Количество 1 этап: " + list.size());
            formProgressBar.getJProgressBars(2).setMaximum(list.size());

            ExecutorService es = Executors.newFixedThreadPool(1);
            // 5
            es.submit(
                    new MyRunnable5(
                            formReport,
                            formProgressBar
                    ));

            LOG.info("\n======================================\n" +
                    "========== Вывод результата ==========\n" +
                    "size: {}", list.size());

            for (String string : list) {
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
            formProgressBar.getJLabelsStage(2).setText("Количество 2 этап: " + list.size());
        }
    }
}
