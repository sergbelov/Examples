package ru.examples.guiExample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * File New Dialog
 * DialogFormGUI.form - форма в формате XML
 */
public class DialogFormGUI extends JDialog {
    private static DialogFormGUI dialog;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonStyle;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextArea textArea1;
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;

    private Object[] options = {"Да", "Нет"};


    public DialogFormGUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonStyle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onStyle(); }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        JOptionPane.showMessageDialog(this,
                "Label1 " + textField1.getText() + textField2.getText() + textField3.getText()+"\n"+
                "Label2 " + textField4.getText() + "\n" +
                textArea1.getText());
    }

    private void onCancel() {
        if (JOptionPane.showOptionDialog(this,
                "Уверены, что хотите выйти?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]) == 0) {

            dispose();
        }
    }

    private void onStyle() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        for(Window window : JFrame.getWindows()) { // применяем новый стиль для всех компонентов
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    public static void main(String[] args) {
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        dialog = new DialogFormGUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
