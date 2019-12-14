package ru.examples.guiExample.ReportExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class FormAuthorization {

    static final Logger LOG = LogManager.getLogger();

    private JFrame authorizationFrame = new JFrame("Авторизация");;
    private JPanel authorizationPanel = new JPanel();
    private GridLayout gl1 = new GridLayout(3, 2, 1, 1);

    private JTextField tUserName = new JTextField("Name");
    private JPasswordField tUserPassword = new JPasswordField("Password");

    private JButton bAuthorization = new JButton("Авторизация");
    private JButton bExit = new JButton("Выход");

    private FormAuthorizationEngine etEngine = new FormAuthorizationEngine(this);

    public void run(
            String userName,
            String userPassword) {

        tUserPassword.setEchoChar('*');

        tUserName.setText(userName);
        tUserPassword.setText(userPassword);

        bAuthorization.addActionListener(etEngine);
        bExit.addActionListener(etEngine);

        authorizationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        authorizationPanel.setLayout(gl1);

        authorizationPanel.add(new JLabel("Имя пользователя"));
        authorizationPanel.add(tUserName);

        authorizationPanel.add(new JLabel("Пароль"));
        authorizationPanel.add(tUserPassword);

        authorizationPanel.add(bAuthorization);
        authorizationPanel.add(bExit);

        authorizationFrame.setContentPane(authorizationPanel);
        authorizationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //иконка для приложения
//        ImageIcon icon = new ImageIcon("personal-100.png");
//        Image image = icon.getImage();
//        authorizationFrame.setIconImage(image);

        // делаем размер окна достаточным для того, чтобы вместить все компоненты
//		authorizationFrame.pack();
        authorizationFrame.setSize(300, 120);
		authorizationFrame.setResizable(false);
        authorizationFrame.setLocationRelativeTo(null); // по центру экрана
        authorizationFrame.setVisible(true);
    }

    public JFrame getAuthorizationFrame() {
        return authorizationFrame;
    }

    public String getUserName() {
        return tUserName.getText();
    }

    public String getUserPassword() {
//        return tUserPassword.getPassword().toString();
        return tUserPassword.getText();
    }

    public JButton getBAuthorization() {
        return bAuthorization;
    }

    public JButton getBExit() {
        return bExit;
    }
}
