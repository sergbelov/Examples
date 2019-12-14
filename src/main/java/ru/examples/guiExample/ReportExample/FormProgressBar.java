package ru.examples.guiExample.ReportExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FormProgressBar {

    private static final Logger LOG = LogManager.getLogger();

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private JFrame progressBarFrame = new JFrame("Процесс обработки данных");
    private JPanel progressBarPanel = new JPanel(new GridBagLayout());
    private GridBagConstraints gbc = new GridBagConstraints();

    private JProgressBar[] jProgressBars;
    private JLabel[] jLabelsDur;
    private JLabel[] jLabels;
    private JLabel pictLabel;

    private FormProgressBarEngine etEngine = new FormProgressBarEngine(this);

    private int countStage;
    private String[] stages;
    private long startTime = System.currentTimeMillis();

    public void run(String period, String[] stages) {

        this.stages = stages;
        countStage = stages.length;
        jProgressBars = new JProgressBar[countStage];
        jLabelsDur = new JLabel[countStage];
        jLabels = new JLabel[countStage];

        progressBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 5, 5));

        for (int i = 0; i < countStage; i++) {
            jProgressBars[i] = new JProgressBar();
            jProgressBars[i].setStringPainted(true);
            jProgressBars[i].setMinimum(0);
            jProgressBars[i].setMaximum(100);
            jProgressBars[i].setValue(0);
            jProgressBars[i].setForeground(new Color(100, 200, 100));

            jLabelsDur[i] = new JLabel();

            jLabels[i] = new JLabel();
            jLabels[i].addMouseListener(etEngine);

            gbc.insets = new Insets(5, 2, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.0; // размер не изменяется
            progressBarPanel.add(new JLabel(stages[i]), gbc);

            gbc.insets = new Insets(5, 5, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.weightx = 1.0;
            if (i == 0) {
                progressBarPanel.add(new Label(period), gbc);
            } else {
                progressBarPanel.add(jProgressBars[i], gbc);
            }

            gbc.insets = new Insets(5, 5, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 2;
            gbc.gridy = i;
            gbc.weightx = 0.0;
            progressBarPanel.add(jLabelsDur[i], gbc);

            gbc.insets = new Insets(5, 5, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 3;
            gbc.gridy = i;
            gbc.weightx = 0.0;
            progressBarPanel.add(jLabels[i], gbc);

        }



        ImageIcon icon0 = new ImageIcon("loading.gif");
        ImageIcon icon = new ImageIcon(icon0.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT));
        pictLabel = new JLabel(icon);
//        pictLabel.setOpaque(false); // прозрачный
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
//        gbc.gridwidth = 1;
        progressBarPanel.add(pictLabel, gbc);



        //Создаём фрейм и задаём его основную панель
        progressBarFrame.setContentPane(progressBarPanel);
        progressBarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //иконка для приложения
//        ImageIcon icon = new ImageIcon("Jira.png");
////        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/Jira.png"));
//        Image image = icon.getImage();
//        progressBarFrame.setIconImage(image);

        // делаем размер окна достаточным для того, чтобы вместить все компоненты
//        progressBarFrame.pack();
        progressBarFrame.setSize(800, countStage * 27 + 30);
        progressBarFrame.setResizable(false);
//        progressBarFrame.setLocationRelativeTo(null); // по центру экрана
        progressBarFrame.setVisible(true);
    }

    public JFrame getProgressBarFrame() {
        return progressBarFrame;
    }

    public JLabel getJLabelsDur(int num) {
        return jLabelsDur[num];
    }

    public JLabel getJLabels(int num) {
        return jLabels[num];
    }

    public JProgressBar getJProgressBars(int num) {
        return jProgressBars[num];
    }

    public void pictLabelHide(){
        pictLabel.setVisible(false);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime() {
        setStartTime(System.currentTimeMillis());
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDurationTime() {
        long r = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        return r;
    }

    public String getDurationTimeString() {
        long r = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        return r + " ms";
    }

}
