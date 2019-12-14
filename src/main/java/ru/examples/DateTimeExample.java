package ru.examples;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeExample {

    public static void main(String[] args) {

        System.out.println("==============\r\nDate to String");

        DateFormat dateFormatDef = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        DateFormat dateFormatEng = DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH);
        System.out.println(dateFormatDef.format(new Date()));
        System.out.println(dateFormatEng.format(new Date()));

        DateFormat datetimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        long startDateTime = System.currentTimeMillis();
        System.out.println("Start: " + datetimeFormat.format(startDateTime));
        for (int i = 0; i < 9999999; i++) {
        }
        long stopDateTime = System.currentTimeMillis();
        System.out.println("Stop: " + datetimeFormat.format(stopDateTime));
        System.out.println("delay: " + datetimeFormat.format(stopDateTime - startDateTime - (3 * 1000 * 60 * 60)));


        System.out.println("\r\n==============\r\nString to Date");
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "07/04/2018";
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(date);
    }
}
