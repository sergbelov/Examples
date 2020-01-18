package ru.examples.guiExample.ReportExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.examples.guiExample.ReportExample.runnable.MyRunnable4;
import ru.examples.guiExample.ReportExample.runnable.MyRunnable1;
import ru.examples.guiExample.ReportExample.runnable.MyRunnable2;
import ru.examples.guiExample.ReportExample.runnable.MyRunnable3;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;

public class FormReportEngine implements ActionListener {

    static final Logger LOG = LogManager.getLogger();

    static List<String> list = new CopyOnWriteArrayList<>();

    private Object[] options = {"Да", "Нет"};
    private FormReport formReport;

    FormReportEngine(FormReport formReport) {
        this.formReport = formReport;
    }

    public void actionPerformed(ActionEvent evt) {

        // Получаем источник события
        Object eventSource = evt.getSource();

        if (eventSource.equals(formReport.getBExit())) { // выход из программы
            if (JOptionPane.showOptionDialog(formReport.getReportFrame(),
                    "Уверены, что хотите выйти?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]) == 0) {

                System.exit(0);
            }

        } else if (eventSource == formReport.getBCreateReport()) { // формируем отчет

            formReport.getBCreateReport().setEnabled(false);


            LOG.info("startPeriodStr: {); startPeriodStr:{);",
                    formReport.getStartPeriodStr(),
                    formReport.getStopPeriodStr());

            int countThread = formReport.getCountThread();
            int max = 10;

/*
            FormProgressBar2 formProgressBar = new FormProgressBar2(list, countThread);
            formProgressBar.run();

            formProgressBar.getJProgressBar().setMaximum(countThread * max);

            CountDownLatch cdl = new CountDownLatch(countThread);

            ExecutorService es = Executors.newFixedThreadPool(countThread);
            for (int t = 0; t < countThread; t++) {
                es.submit(
                        new MyRunnable(
                                t,
                                max,
                                list,
                                cdl,
                                formReport,
                                formProgressBar
                        ));
            }
*/

            int c = 5;
            String[] stages = new String[c];
            for (int i = 0; i < c; i++) {
                stages[i] = "Этап " + i;
            }
            FormProgressBar formProgressBar = new FormProgressBar();
            formProgressBar.run(
                    formReport.getStartPeriodStr() + " - " + formReport.getStopPeriodStr(),
                    stages,
                    4);
            formProgressBar.getJProgressBars(1).setMaximum(countThread * max);
            formProgressBar.getJProgressBars(1).repaint();


            // все процессы обработки запускаем в фоновом потоке
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    list.clear();
                    formProgressBar.setStartTime();

                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            CountDownLatch cdl = new CountDownLatch(3);
                            ExecutorService es = Executors.newFixedThreadPool(3);

                            // 1
                            es.submit(
                                    new MyRunnable1(
                                            countThread,
                                            cdl,
                                            list,
                                            max,
                                            formReport,
                                            formProgressBar
                                    ));

                            // 2
                            es.submit(
                                    new MyRunnable2(
                                            cdl,
                                            formReport,
                                            formProgressBar
                                    ));

                            // 2
                            es.submit(
                                    new MyRunnable3(
                                            cdl,
                                            formReport,
                                            formProgressBar
                                    ));

                            es.shutdown();
                        }
                    });
                }
            });

//            } else {
//                JOptionPane.showMessageDialog(formAuthorization.authorizationFrame,
//                        "Введите данные для формирования отчета",
//                        "Ввод данных",
//                        JOptionPane.WARNING_MESSAGE);
//            }
        }
    }
}

