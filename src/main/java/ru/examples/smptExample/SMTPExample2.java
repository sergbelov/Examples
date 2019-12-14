package ru.examples.smptExample;

import ru.utils.email.EmailSender;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

public class SMTPExample2 {

    public static void main(String[] args)
    {
        String SMTP_SERVER = "Outlook.ru";
        int SMTP_PORT = 25;
        String EMAIL_FROM = "Belov-SeA@mail.ru";
        String SMTP_AUTH_USER = "Belov-SeA";
        String SMTP_AUTH_PASSWORD = "";
        String fileAttachment = "";

        EmailSender es = new EmailSender(SMTP_SERVER, SMTP_PORT, SMTP_AUTH_USER, SMTP_AUTH_PASSWORD);

        String emailTo = EMAIL_FROM;
        String message = "Тестовое сообщение<br>Тело тестового сообщения";
        Address[] replyToList = new Address[0];
        try {
            replyToList = new Address[]{new InternetAddress("Simashev-AV@mail.ru")};
        } catch (AddressException e) {
            e.printStackTrace();
        }

        String resSend;
        resSend = es.send(
                EMAIL_FROM,
                emailTo,
                replyToList,
                "1",
                "Тестовое сообщение",
                message,
                "html",
                fileAttachment);

        if (resSend.length() != 0){
            System.out.println(resSend);
        }
    }
}
