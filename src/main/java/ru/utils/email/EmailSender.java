package ru.utils.email;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

public class EmailSender {

    private String ENCODING = "UTF-8";
    private Properties properties = new Properties();
    private Authenticator authenticator = null;
    private Session session = null;
    private Message message = null;

    /**
     *
     * @param smtp_server
     * @param smtp_port
     * @param smtp_auth_user
     * @param smtp_auth_password
     */
    public EmailSender(
            String smtp_server,
            int    smtp_port,
            final  String smtp_auth_user,
            final  String smtp_auth_password) {

        properties.put("mail.mime.charset", ENCODING);
        properties.put("mail.smtp.host", smtp_server);
        properties.put("mail.smtp.port", smtp_port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.mime.encodefilename", "true");

//        properties.put("mail.smtp.ssl.enable", "false");

//        properties.put("mail.smtp.ssl.enable", "true");
//        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");


        authenticator = new EmailAuthenticator(smtp_auth_user, smtp_auth_password);
        session = Session.getInstance(properties, authenticator);
        session.setDebug(false);
        message = new MimeMessage(session);
    }


    /*
    отправляем сообщение
     */
    public String send(
            String from,
            String to,
            Address[] replyToList,
            String priority,
            String theme,
            String text,
            String contentType,
            String fileAttachment) {

        String result = "";

        //to = "Belov-SeA@mail.ru";

        try {
            InternetAddress email_from = new InternetAddress(from);
            InternetAddress email_to = new InternetAddress(to);
            if (replyToList != null && replyToList.length > 0) {
//                InternetAddress email_replyTo = new InternetAddress(replyToList);
//                message.setReplyTo (new InternetAddress[] {email_replyTo});
                message.setReplyTo(replyToList);
            }

            message.setFrom(email_from);
            message.setRecipient(Message.RecipientType.TO, email_to);
            message.setSubject(theme);
            message.setHeader("X-Priority", priority); // важность сообщения


            // Текст сообщения
            BodyPart bodyPart = new MimeBodyPart();
//            bodyPart.setContent(text, "text/plain; charset=utf-8");
            bodyPart.setContent(text, "text/" + contentType + "; charset=" + ENCODING);
            // Содержимое сообщения
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            // Определение контента сообщения
            message.setContent(multipart);

            // Вложение файла в сообщение
            if (fileAttachment != null && !fileAttachment.isEmpty()) {
                MimeBodyPart bodyPartAttachment = new MimeBodyPart();
                DataSource source = new FileDataSource(fileAttachment);
                bodyPartAttachment.setDataHandler(new DataHandler(source));
                try {
                    bodyPartAttachment.setFileName(MimeUtility.encodeText(source.getName()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                multipart.addBodyPart(bodyPartAttachment);
            }

            // Отправка сообщения
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            result = e.getMessage();
/*
            // Ошибка отправки сообщения
            result = e.getMessage();
            System.err.println(e);
*/
        }

        return result;
    }

}
