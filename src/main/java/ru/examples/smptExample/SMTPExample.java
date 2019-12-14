package ru.examples.smptExample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SMTPExample {
    private final static String PROPS_FILE = "smtp.properties";

    public static void main(String[] args)
    {
        try {
            InputStream is = new FileInputStream(PROPS_FILE);
            if (is != null) {
                Properties pr = new Properties();
//                Reader reader = new InputStreamReader(is, "UTF-8");
//                pr.load(new FileInputStream(new File("email.properties")));
                pr.load(is);
                String SMTP_SERVER = pr.getProperty ("server");
                int SMTP_PORT = Integer.parseInt(pr.getProperty ("port"));
                String EMAIL_FROM = pr.getProperty ("from");
                String SMTP_AUTH_USER = pr.getProperty ("user");
                String SMTP_AUTH_PWD = pr.getProperty ("pass");
                String REPLY_TO = pr.getProperty ("replyto");
                String fileAttachment = "";
                is.close();

                /*
                String smtp_server,
                int    smtp_port,
                final String smtp_auth_user,
                final String smtp_auth_pwd
                */

                EmailSender es = new EmailSender(SMTP_SERVER, SMTP_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);

                String emailTo = EMAIL_FROM;
                String message = "Тестовое сообщение<br>Тело тестового сообщения";

                String resSend;
                resSend = es.send(
                        EMAIL_FROM,
                        emailTo,
                        "1",
                        "Тестовое сообщение",
                        message,
                        "html",
                        fileAttachment);

                if (resSend.length() != 0){
                    System.out.println(resSend);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
