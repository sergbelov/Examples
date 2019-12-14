package ru.examples.guiExample;

import java.awt.*;

import javax.swing.*;

public class GUIExample extends JFrame
{
    public GUIExample()
    {
        init();
    }

    private void init()
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Grid Bag Layout Example");

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("First Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        detailPanel.add(label, gbc);

        JTextField firstNameTextField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        detailPanel.add(firstNameTextField, gbc);

        label = new JLabel("Last Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        detailPanel.add(label, gbc);

        JTextField lastNameTextField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        detailPanel.add(lastNameTextField, gbc);

        label = new JLabel("Telephone:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        detailPanel.add(label, gbc);

        JTextField telephoneTextField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        detailPanel.add(telephoneTextField, gbc);

        label = new JLabel("Priority Name:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        detailPanel.add(label, gbc);

        String[] data = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        JComboBox priorityComboBox = new JComboBox(data);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        detailPanel.add(priorityComboBox, gbc);


        label = new JLabel("RadioButton:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        detailPanel.add(label, gbc);

        label = new JLabel("CheckBox:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        detailPanel.add(label, gbc);

        int count = 3;
        ButtonGroup bGRB = new ButtonGroup();
        JRadioButton[] arrRB = new JRadioButton[3];
        JCheckBox[] arrCB = new JCheckBox[count];
        for (int i = 0; i < count; i++) {
            arrRB[i] = new JRadioButton();
            arrRB[i].setText("RB" + i);
            bGRB.add(arrRB[i]);

            arrCB[i] = new JCheckBox();
            arrCB[i].setText("CB" + i);

            gbc.gridx = i + 1;
            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridwidth = 1;
            detailPanel.add(arrRB[i], gbc);

            gbc.gridx = i + 1;
            gbc.gridy = 5;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridwidth = 1;
            detailPanel.add(arrCB[i], gbc);
        }


        label = new JLabel("Call Description:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 4;
        detailPanel.add(label, gbc);

        JTextArea descriptionTextArea = new JTextArea("JTextArea1\nJTextArea2\nJTextArea3");
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 4;
        detailPanel.add(descriptionTextArea, gbc);



        ImageIcon icon0 = new ImageIcon("loading.gif");
        ImageIcon icon = new ImageIcon(icon0.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT));
        JLabel pictLabel = new JLabel(icon);
//        pictLabel.setOpaque(false); // прозрачный
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
//        gbc.gridwidth = 1;
        detailPanel.add(pictLabel, gbc);


        getContentPane().add(detailPanel);
    }


    public static void main(String[] args)
    {
        GUIExample gui = new GUIExample();

        gui.setSize(800, 400);
        gui.setMinimumSize(new Dimension(600, 300));
        gui.setVisible(true);
    }
}