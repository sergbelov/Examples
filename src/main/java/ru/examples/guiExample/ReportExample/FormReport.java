package ru.examples.guiExample.ReportExample;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormReport {

    private static final Logger LOG = LogManager.getLogger();

    private SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private JFrame reportFrame = new JFrame("Параметры для отчета");
    private JPanel reportPanel = new JPanel(new GridBagLayout());
    private GridBagConstraints gbc = new GridBagConstraints();

    //    private JFormattedTextField tDateBegin = new JFormattedTextField(DateFormat.getDateInstance(DateFormat.SHORT));
    private JFormattedTextField tDateBegin = new JFormattedTextField(sdf1);
    private JFormattedTextField tDateEnd = new JFormattedTextField(sdf1);

    private JDateChooser dtChBegin = new JDateChooser();
    private JDateChooser dtChEnd = new JDateChooser();
    private JSpinner spBeginH = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
    private JSpinner spEndH = new JSpinner(new SpinnerNumberModel(23, 0, 23, 1));
    private JSpinner spBeginM = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private JSpinner spEndM = new JSpinner(new SpinnerNumberModel(59, 0, 59, 1));
    private JSpinner spCountThread = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));

    private JButton bCreateReport = new JButton("Сформировать отчет");
    private JButton bExit = new JButton("Выход");

    private FormReportEngine etEngine = new FormReportEngine(this);

    public void run() {

        dtChBegin.setDate(new Date());
        tDateBegin = (JTextFieldDateEditor) dtChBegin.getComponent(1);
        tDateBegin.setHorizontalAlignment(JTextField.LEFT);
        dtChBegin.add(tDateBegin);

        dtChEnd.setDate(new Date());
        tDateEnd = (JTextFieldDateEditor) dtChEnd.getComponent(1);
        tDateEnd.setHorizontalAlignment(JTextField.LEFT);
        dtChEnd.add(tDateEnd);

        bCreateReport.addActionListener(etEngine);
        bExit.addActionListener(etEngine);

        reportPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0; // размер не изменяется
        reportPanel.add(new JLabel("Начало периода"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 2.0;
        reportPanel.add(dtChBegin, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(spBeginH, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(spBeginM, gbc);


        gbc.insets = new Insets(5, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(new JLabel("Конец периода"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 0;
        gbc.weightx = 2.0;
        reportPanel.add(dtChEnd, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(spEndH, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(spEndM, gbc);


        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(new JLabel("Количество потоков"), gbc);

        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        reportPanel.add(spCountThread, gbc);


        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
//        gbc.gridwidth = 2;
        reportPanel.add(bCreateReport, gbc);

        gbc.insets = new Insets(10, 5, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
//        gbc.gridwidth = 2;
        reportPanel.add(bExit, gbc);


        //Создаём фрейм и задаём его основную панель
        reportFrame.setContentPane(reportPanel);
        reportFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //иконка для приложения
//        ImageIcon icon = new ImageIcon("Jira.png");
////        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/Jira.png"));
//        Image image = icon.getImage();
//        progressBarFrame.setIconImage(image);

        // делаем размер окна достаточным для того, чтобы вместить все компоненты
        reportFrame.setSize(370, 150);
//		reportFrame.pack();
        reportFrame.setResizable(false);
        reportFrame.setLocationRelativeTo(null); // по центру экрана
        reportFrame.setVisible(true);
    }

    public JFrame getReportFrame() {
        return reportFrame;
    }

    public JButton getBCreateReport() {
        return bCreateReport;
    }

    public JButton getBExit() {
        return bExit;
    }

    public String getStartPeriodStr() {
        return sdf1.format(dtChBegin.getDate()) + " " +
                String.format("%02d", spBeginH.getValue()) + ":" +
                String.format("%02d", spBeginM.getValue()) + ":00";
    }

    public String getStopPeriodStr() {
        return sdf1.format(dtChEnd.getDate()) + " " +
                String.format("%02d", spEndH.getValue()) + ":" +
                String.format("%02d", spEndM.getValue()) + ":00";
    }

    public long getStartPeriodLong() {
        long startPeriodLong = 0l;
        try {
            startPeriodLong = sdf2.parse(getStartPeriodStr()).getTime();
        } catch (ParseException e) {
            LOG.warn(e);
        }
        return startPeriodLong;
    }

    public long getStopPeriodLong() {
        long stopPeriodLong = 0l;
        try {
            stopPeriodLong = sdf2.parse(getStartPeriodStr()).getTime();
        } catch (ParseException e) {
            LOG.warn(e);
        }
        return stopPeriodLong;
    }

    public int getCountThread() {
        return (int) spCountThread.getValue();
    }

}