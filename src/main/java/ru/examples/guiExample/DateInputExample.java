package ru.examples.guiExample;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateInputExample {

    private DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private JFrame jFrame;
    private JDateChooser jDateChooser;
    private JTextField jTextField;
//    private JFormattedTextField jTextField = new JFormattedTextField(sdf);
    private JPanel contentPane;
    private JPanel centerP;

    public static void main(String[] args) {
        new DateInputExample();
    }


    public DateInputExample() {

        //////////// Creating Frame
        jFrame = new JFrame("");
        jFrame.setSize(300, 200);
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setVisible(true);

        //////////// Creating contentPane
        contentPane = new JPanel(new GridLayout(0, 1));
        contentPane.setBackground(new java.awt.Color(255, 255, 255));
        jFrame.add(contentPane);
        jFrame.setVisible(true);

        //////////// Creating CenterP
        centerP = new JPanel();
        centerP.setBackground(new java.awt.Color(255, 255, 255));
        contentPane.add(centerP);
        jFrame.add(contentPane);
        jFrame.setVisible(true);

        // Available From Calendar
        JLabel availF = new JLabel("Available From:");
        centerP.add(availF);
        contentPane.add(centerP);
        jTextField = new JTextField(11);
        centerP.add(jTextField);
        contentPane.add(centerP);
        jFrame.add(contentPane);
        jFrame.setVisible(true);

        jDateChooser = new JDateChooser();
        jDateChooser.setDate(new Date());
        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) jDateChooser.getComponent(1);
        dateEditor.setHorizontalAlignment(JTextField.RIGHT);
        jDateChooser.add(jTextField);
        centerP.add(jDateChooser);
        contentPane.add(centerP);
        jFrame.add(contentPane);
        jFrame.setVisible(true);

        // Converting Date to String
        System.out.println(jDateChooser.getDate());
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(jDateChooser.getDate());
        jTextField.setText(date);
    }
}
