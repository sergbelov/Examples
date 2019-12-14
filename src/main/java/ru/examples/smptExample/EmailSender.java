package ru.examples.smptExample;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {

    private Message message       = null;
    private Properties properties = new Properties();
    private Session session;


    /**
     *
     * @param smtp_server
     * @param smtp_port
     * @param smtp_auth_user
     * @param smtp_auth_pwd
     */
    public EmailSender(
            String smtp_server,
            int    smtp_port,
            final  String smtp_auth_user,
            final  String smtp_auth_pwd) {

        properties.put("mail.smtp.host", smtp_server);
        properties.put("mail.smtp.port", smtp_port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.mime.encodefilename", "true");
//        properties.put("mail.smtp.ssl.enable", "false");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtp_auth_user, smtp_auth_pwd);
            }
        });

        session.setDebug(false);

        message = new MimeMessage(session);
    }


    /*
    отправляем сообщение
     */
    public String send(
            String from,
            String to,
            String priority,
            String thema,
            String text,
            String contentType,
            String fileAttachment) {

        String result = "";

        //to = "Belov-SeA@mail.ru";

        try {
            InternetAddress email_from = new InternetAddress(from);
            InternetAddress email_to = new InternetAddress(to);

            message.setFrom(email_from);
            message.setRecipient(Message.RecipientType.TO, email_to);
            message.setSubject(thema);
            message.setHeader("X-Priority", priority); // важность сообщения

//            InternetAddress reply_to = new InternetAddress("Kamalov-AN@mail.ru");
//            message.setReplyTo (new Address[] {reply_to});

            // Содержимое сообщения
            Multipart mp = new MimeMultipart();
            // Текст сообщения
            MimeBodyPart bodyPart = new MimeBodyPart();
//            bodyPart.setContent(text, "text/plain; charset=utf-8");
            bodyPart.setContent(text, "text/" + contentType + "; charset=utf-8");
            mp.addBodyPart(bodyPart);
            // Определение контента сообщения
            message.setContent(mp);

            // Вложение файла в сообщение
            if (fileAttachment != null && fileAttachment.isEmpty()) {
                MimeBodyPart mbr = createFileAttachment(fileAttachment);
                mp.addBodyPart(mbr);
            }

            // Отправка сообщения
            Transport.send(message);
        } catch (MessagingException e) {
            // Ошибка отправки сообщения
            result = e.getMessage();
            System.err.println(e);
        }

        return result;
    }

    /*
    Вложение файла в сообщение, MimeBodyPart

    Для вложения файла в сообщение необходимо создать объект типа MimeBodyPart,
    в котором в качестве параметров указать путь к файлу и наименование файла.
    Следующий листинг представляет функцию формирования объекта MimeBodyPart с содержимом файла,
    который можно вкладывать в сообщение.
*/
    private MimeBodyPart createFileAttachment(String filepath)
            throws MessagingException
    {
        // Создание MimeBodyPart
        MimeBodyPart mbp = new MimeBodyPart();

        // Определение файла в качестве контента
        FileDataSource fds = new FileDataSource(filepath);
        mbp.setDataHandler(new DataHandler(fds));
        mbp.setFileName(fds.getName());
        return mbp;
    }


}
