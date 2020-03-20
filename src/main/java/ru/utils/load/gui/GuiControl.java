package ru.utils.load.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.utils.MultiRun;
import ru.utils.load.utils.MultiRunService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GuiControl extends JFrame {
    private static final Logger LOG = LogManager.getLogger(GuiControl.class);

    private MultiRun multiRun;
    private JButton bVUDec;
    private JButton bVUInc;
    private JButton bDurationDec;
    private JButton bDurationInc;
    private JButton bStop;

    public GuiControl(MultiRun multiRun) {
        this.multiRun = multiRun;
//        setType(Type.UTILITY);
        setTitle("Нагрузка...");
        setSize(400, 200);
//        setLocation(100, 50);
        setLocationRelativeTo(null); // по центру экрана
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel jPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        jPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GuiControlEngine listener = new GuiControlEngine(this);
        bVUDec = new JButton("Остановить один поток");
        bVUDec.addActionListener(listener);
        bVUInc = new JButton("Добавить один поток");
        bVUInc.addActionListener(listener);
        bDurationDec = new JButton("Уменьшить продолжительность теста на 5 минут");
        bDurationDec.addActionListener(listener);
        bDurationInc = new JButton("Увеличить продолжительность теста на 5 минут");
        bDurationInc.addActionListener(listener);
        bStop = new JButton("Прекратить подачу нагрузки");
        bStop.addActionListener(listener);
        jPanel.add(bVUDec);
        jPanel.add(bVUInc);
        jPanel.add(bDurationDec);
        jPanel.add(bDurationInc);
        jPanel.add(bStop);
        setContentPane(jPanel);
        setIcon("load.png");
        setVisible(true);
    }

    public class GuiControlEngine implements ActionListener {
        Object[] options = {"Да", "Нет"};
        GuiControl guiControl;

        GuiControlEngine(GuiControl guiControl) {
            this.guiControl = guiControl;
        }

        public void actionPerformed(ActionEvent evt) {
            Object eventSource = evt.getSource(); // Получаем источник события

            if (eventSource == bVUDec) { // остановка потока
                for (MultiRunService multiRunService : multiRun.getMultiRunServiceList()) {
                    multiRunService.vuDec();
                }

            } else if (eventSource == bVUInc) { // добавление потока
                for (MultiRunService multiRunService : multiRun.getMultiRunServiceList()) {
                    multiRunService.vuInc();
                }

            } else if (eventSource == bDurationDec) { // уменьшение длительности теста
                for (MultiRunService multiRunService : multiRun.getMultiRunServiceList()) {
                    multiRunService.durationDec(5 * 60 * 1000);
                }

            } else if (eventSource == bDurationInc) { // увеличение длительности теста
                for (MultiRunService multiRunService : multiRun.getMultiRunServiceList()) {
                    multiRunService.durationInc(5 * 60 * 1000);
                }

            } else if (eventSource == bStop) { // прерываем нагрузку
                if (JOptionPane.showOptionDialog(guiControl,
                        "Прекратить подачу нагрузки?",
                        "Подтверждение",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]) == 0) {

                    if (multiRun.isWarmingCompleted()) {
                        bVUDec.setEnabled(false);
                        bVUInc.setEnabled(false);
                        bDurationDec.setEnabled(false);
                        bDurationInc.setEnabled(false);
                        bStop.setEnabled(false);
                        bStop.setText("Остановка нагрузки...");
                        for (MultiRunService multiRunService : multiRun.getMultiRunServiceList()) {
                            multiRunService.stop("Прерывание нажатием на кнопку");
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                guiControl,
                                "Во время прогрева остановка невозможна",
                                "Предупреждение",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Устанавливаем ico для формы
     * @param icoFile
     */
    public void setIcon(String icoFile){
        try {
            ImageIcon icon = new ImageIcon(icoFile);
            Image image = icon.getImage();
            this.setIconImage(image);
        } catch ( Exception e){
            LOG.info("Ошибка при загрузке файла {}", icoFile, e);
        }
    }

}
