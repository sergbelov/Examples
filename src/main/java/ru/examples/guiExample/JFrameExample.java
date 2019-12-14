package ru.examples.guiExample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;

public class JFrameExample {

    /**
     * точка входа
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrameExample fe = new JFrameExample();
            }
        });
    }


    public JFrameExample() {

        final int COUNT = 3;
        Boolean[] arrSelected = {false, false, false};

        ButtonGroup bGRB;
        JRadioButton[] arrRB = new JRadioButton[COUNT];
        JCheckBox[] arrCB = new JCheckBox[COUNT];
        JPanel windowContent, p1, p2;
        JFrame mainFrame;

        // основная панель
        windowContent = new JPanel();
        // Задаём схему для этой панели
        BorderLayout bl = new BorderLayout();
        windowContent.setLayout(bl);

        // группа RadioButton
        bGRB = new ButtonGroup();

        JFramseExampleEngine listner = new JFramseExampleEngine();

        for (int i = 0; i < COUNT; i++) {
            // создаем компоненты JRadioButton + слушатель
            arrRB[i] = new JRadioButton();
            arrRB[i].setName("RB" + i);
            arrRB[i].setText("JRadioButton" + i);
            arrRB[i].addActionListener(listner);
            bGRB.add(arrRB[i]);

            // создаем компоненты JCheckBox + слушатель
            arrCB[i] = new JCheckBox();
            arrCB[i].setName("CB" + i);
            arrCB[i].setText("JCheckBox" + i);
            arrCB[i].addActionListener(listner);
        }

        // Создаём панель p1 с GridLayout
        p1 = new JPanel();
        GridLayout gl1 = new GridLayout(COUNT, 1, 1, 1);
        p1.setLayout(gl1);
        // Помещаем панель p1 в северную область окна
        windowContent.add("North", p1);
        // Добавляем компоненты на панель p1



        // Создаём панель p2 с GridLayout
        p2 = new JPanel();
        GridLayout gl2 = new GridLayout(COUNT * 2, 1, 1, 2);
        p2.setLayout(gl2);
        // Помещаем панель p2 в центральную область окна
        windowContent.add("Center", p2);
        // Добавляем компоненты на панель p2
        for (int i = 0; i < COUNT; i++) {
            p2.add(arrCB[i]);
        }
        for (int i = 0; i < COUNT; i++) {
            p2.add(arrRB[i]);
        }

        //Создаём фрейм и задаём его основную панель
        mainFrame = new JFrame("JFRame");
        mainFrame.setContentPane(windowContent);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //иконка для приложения
//        ImageIcon Checkers = new ImageIcon("/res/test3.jpg");
/*
        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/test3.jpg"));
        Image image = icon.getImage();
        mainFrame.setIconImage(image);
*/

        // делаем размер окна достаточным для того, чтобы вместить все компоненты
//		mainFrame.pack();
        // Задаем размер окна
        mainFrame.setSize(800, 600);
        mainFrame.setVisible(true);

    }


    /**
     * Белов С.А.
     * обработка событий
     */
    public class JFramseExampleEngine implements ActionListener {

        Object[] options = {"Да", "Нет"};
//        JFrameExample formProgressBar; // ссылка на JFrameExample

        // Конструктор сохраняет ссылку на окно JFrameExample в переменной класса “formProgressBar”
//        JFramseExampleEngine(JFrameExample formProgressBar){
//            this.formProgressBar = formProgressBar;
//        }

        public void actionPerformed(ActionEvent evt) {

            // Получаем источник события
            Object eventSource = evt.getSource();

/*        if (eventSource instanceof JButton) {
            clButton = (JButton) eventSource;
        }
        if (eventSource instanceof JRadioButton) {
            selRB = (JRadioButton) eventSource;
        }
        if (eventSource instanceof JCheckBox) {
            selCB = (JCheckBox) eventSource;
        }
*/

/*
            if (eventSource == formProgressBar.bExit) { // выход из программы
                if (JOptionPane.showOptionDialog(formProgressBar.mainFrame,
                        "Уверены, что хотите выйти?",
                        "Подтверждение",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]) == 0) {
                    System.exit(0);
                }
            }
*/
        }
    }

}
