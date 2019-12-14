package ru.examples.guiExample.ReportExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FormAuthorizationEngine implements ActionListener {

    static final Logger LOG = LogManager.getLogger();

    private Object[] options = { "Да", "Нет" };
    private FormAuthorization formAuthorization;

    FormAuthorizationEngine(FormAuthorization formAuthorization){
        this.formAuthorization = formAuthorization;
    }

    public void actionPerformed(ActionEvent evt) {

        // Получаем источник события
        Object eventSource = evt.getSource();

        if (eventSource.equals(formAuthorization.getBExit())) { // выход из программы

            if (JOptionPane.showOptionDialog(formAuthorization.getAuthorizationFrame(),
                    "Уверены, что хотите выйти?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]) == 0) {

                System.exit(0);
            }

        } else if (eventSource == formAuthorization.getBAuthorization()) { // авторизация

            // для отладки
            LOG.info("UserName: {}; UserPassword: {}",
                    formAuthorization.getUserName(),
                    formAuthorization.getUserPassword());

            formAuthorization.getAuthorizationFrame().dispose();
            FormReport formReport= new FormReport();
            formReport.run();

/*
                JOptionPane.showMessageDialog(formAuthorization.authorizationFrame,
                        "Не верный логин или пароль",
                        "Ошибка авторизации",
                        JOptionPane.ERROR_MESSAGE);
*/

        }
    }
}

