package ru.examples.guiExample;

import javax.swing.*;
import java.awt.*;

public class GridBagLayoutExample {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton button;

        button = new JButton("Button 1");
        gbc.weightx = 0.0; // размер не изменяется
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(button, gbc);

        button = new JButton("Button 2");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(button, gbc);

        button = new JButton("Button 3");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0; // размер не изменяется
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(button, gbc);

        button = new JButton("Button 4");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 40;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 0, 0, 0);  //top padding
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(button, gbc);

        button = new JButton("Button 5");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.gridy = 2;
        panel.add(button, gbc);

        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setVisible(true);
    }

}
