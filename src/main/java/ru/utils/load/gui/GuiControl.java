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
    private JButton bStopButton;

    public GuiControl(MultiRun multiRun) {
        this.multiRun = multiRun;
//        setType(Type.UTILITY);
        setTitle("Нагрузка...");
        setSize(250, 70);
//        setLocation(100, 50);
        setLocationRelativeTo(null); // по центру экрана
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel jPanel = new JPanel(new GridLayout(1, 1, 10, 10));
        jPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GuiControlEngine listener = new GuiControlEngine(this);
        bStopButton = new JButton("Прекратить подачу нагрузки");
        bStopButton.addActionListener(listener);
        jPanel.add(bStopButton);
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

            if (eventSource == bStopButton) { // прерываем нагрузку
                if (JOptionPane.showOptionDialog(guiControl,
                        "Прекратить подачу нагрузки?",
                        "Подтверждение",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]) == 0) {

                    if (multiRun.isWarmingCompleted()) {
                        bStopButton.setEnabled(false);
                        bStopButton.setText("Остановка нагрузки...");
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
