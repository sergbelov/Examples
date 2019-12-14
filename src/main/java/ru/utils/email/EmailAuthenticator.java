package ru.utils.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class EmailAuthenticator extends Authenticator{
    private String user;
    private String password;

    public EmailAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(user, password);
    }
}
