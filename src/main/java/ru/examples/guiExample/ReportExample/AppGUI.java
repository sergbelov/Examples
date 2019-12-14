package ru.examples.guiExample.ReportExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppGUI {

    static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {

        FormAuthorization formAuthorization = new FormAuthorization();
        formAuthorization.run("UserName", "UserPassword");
    }
}
