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
    private JPanel progressBarPanelMain = new JPanel(new GridLayout(2, 1, 1, 1));
    private JPanel progressBarPanelProgress = new JPanel(new GridBagLayout());
    private JPanel progressBarPanelInfo;
    private GridBagConstraints gbcProgress = new GridBagConstraints();

    private JProgressBar[] jProgressBars;
    private JLabel[] jLabelsStage;
    private JLabel[] jLabelsDur;
    private JLabel[] jLabelsInfo;
    private JLabel pictLabel;

    private FormProgressBarEngine etEngine = new FormProgressBarEngine(this);

    private int countLabelInfo;
    private int countStage;
    private String[] stages;
    private long startTime = System.currentTimeMillis();

    public void run(
            String period,
            String[] stages,
            int countLabelInfo) {

        this.stages = stages;
        this.countLabelInfo = countLabelInfo;
        this.countStage = stages.length;
        jProgressBars = new JProgressBar[countStage];
        jLabelsDur = new JLabel[countStage];
        jLabelsStage = new JLabel[countStage];
        jLabelsInfo = new JLabel[countLabelInfo];

        progressBarPanelProgress.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));
        progressBarPanelInfo = new JPanel(new GridLayout(countLabelInfo, 1, 1, 1));
        progressBarPanelInfo.setBackground( new Color(192, 210, 192));

        for (int i = 0; i < countStage; i++) {
            jProgressBars[i] = new JProgressBar();
            jProgressBars[i].setStringPainted(true);
            jProgressBars[i].setMinimum(0);
            jProgressBars[i].setMaximum(100);
            jProgressBars[i].setValue(0);
            jProgressBars[i].setForeground(new Color(100, 200, 100));

            jLabelsDur[i] = new JLabel();

            jLabelsStage[i] = new JLabel();
            jLabelsStage[i].addMouseListener(etEngine);

            gbcProgress.insets = new Insets(2, 2, 0, 0);
            gbcProgress.fill = GridBagConstraints.HORIZONTAL;
            gbcProgress.gridx = 0;
            gbcProgress.gridy = i;
            gbcProgress.weightx = 0.0; // размер не изменяется
            progressBarPanelProgress.add(new JLabel(stages[i]), gbcProgress);

            gbcProgress.insets = new Insets(2, 5, 0, 0);
            gbcProgress.fill = GridBagConstraints.HORIZONTAL;
            gbcProgress.gridx = 1;
            gbcProgress.gridy = i;
            gbcProgress.weightx = 1.0;
            if (i == 0) {
                progressBarPanelProgress.add(new Label(period), gbcProgress);
            } else {
                progressBarPanelProgress.add(jProgressBars[i], gbcProgress);
            }

            gbcProgress.insets = new Insets(2, 5, 0, 0);
            gbcProgress.fill = GridBagConstraints.HORIZONTAL;
            gbcProgress.gridx = 2;
            gbcProgress.gridy = i;
            gbcProgress.weightx = 0.0;
            progressBarPanelProgress.add(jLabelsDur[i], gbcProgress);

            gbcProgress.insets = new Insets(2, 5, 0, 0);
            gbcProgress.fill = GridBagConstraints.HORIZONTAL;
            gbcProgress.gridx = 3;
            gbcProgress.gridy = i;
            gbcProgress.weightx = 0.0;
            progressBarPanelProgress.add(jLabelsStage[i], gbcProgress);
        }


        ImageIcon icon0 = new ImageIcon("loading.gif");
        ImageIcon icon = new ImageIcon(icon0.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT));
        pictLabel = new JLabel(icon);
//        pictLabel.setOpaque(false); // прозрачный
        gbcProgress.gridx = 4;
        gbcProgress.gridy = 0;
        gbcProgress.fill = GridBagConstraints.HORIZONTAL;
        gbcProgress.weightx = 0.0;
        gbcProgress.weighty = 0.0;
//        gbc.gridwidth = 1;
        progressBarPanelProgress.add(pictLabel, gbcProgress);

        for (int i = 0; i < countLabelInfo; i++){
            jLabelsInfo[i] = new JLabel();
            progressBarPanelInfo.add(jLabelsInfo[i]);
        }

        // размещаем две панели на основной
        progressBarPanelMain.add(BorderLayout.CENTER, progressBarPanelProgress);
        progressBarPanelMain.add(BorderLayout.SOUTH, progressBarPanelInfo);

        //Создаём фрейм и задаём его основную панель
        progressBarFrame.setContentPane(progressBarPanelMain);
        progressBarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //иконка для приложения
//        ImageIcon icon = new ImageIcon("Jira.png");
////        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/Jira.png"));
//        Image image = icon.getImage();
//        progressBarFrame.setIconImage(image);

        // делаем размер окна достаточным для того, чтобы вместить все компоненты
//        progressBarFrame.pack();
        progressBarFrame.setSize(800, countStage * 30 + countLabelInfo * 30 + 30);
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

    public JLabel getJLabelsStage(int num) {
        return jLabelsStage[num];
    }

    public JLabel getJLabelsInfo(int num) {
        return jLabelsInfo[num];
    }

    public int getJLabelsInfoFree(){
        int r = 0;
        for (int i = 0; i < countLabelInfo; i++){
            if (jLabelsInfo[i].getText().isEmpty()){
                synchronized (jLabelsInfo[i]){
                    jLabelsInfo[i].setText("*");
                }
                r = i;
                break;
            }
        }
        return r;
    }

    public JProgressBar getJProgressBars(int num) {
        return jProgressBars[num];
    }

    public void pictLabelSetVisible(boolean visible){
        pictLabel.setVisible(visible);
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
