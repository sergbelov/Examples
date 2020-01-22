package ru.examples.guiExample;

import javax.swing.*;
import java.awt.*;

public class JFrameExampleExtends extends JFrame {

    public JFrameExampleExtends() {
        setTitle("Test Frame");
        setSize(200, 200);
        setLocation(100, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int count = 5;
        JPanel jPanel = new JPanel(new GridLayout(count, 1, 10, 10));
        JLabel[] jLabels = new JLabel[count];
        for (int i = 0; i < count; i++) {
            jLabels[i] = new JLabel("Test Message " + i);
            jPanel.add(jLabels[i]);
        }
        setContentPane(jPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        JFrameExampleExtends jFrameExampleExtends = new JFrameExampleExtends();
    }
}
