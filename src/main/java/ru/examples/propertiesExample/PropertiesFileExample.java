package ru.examples.propertiesExample;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class PropertiesFileExample {

    public static void main(String[] args) {

        DateFormat datetimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        String propertiesFileName = "propertiesExample.properties";
        Properties properties = new Properties();

        // setProperty
        try (OutputStream outputStream = new FileOutputStream(propertiesFileName)) {
            properties.setProperty("param1", "12345 " + String.valueOf(System.currentTimeMillis()));
            properties.setProperty("param2", "67890");
            properties.setProperty("DateStore", datetimeFormat.format(System.currentTimeMillis()));
            properties.store(outputStream, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // getProperty
        try (InputStream inputStream = new FileInputStream(propertiesFileName)) {
            properties.load(inputStream);
            System.out.println(properties.getProperty("param1", "not param1"));
            System.out.println(properties.getProperty("param2", "not param2"));
            System.out.println(properties.getProperty("param3", "not param3"));
            System.out.println(properties.getProperty("DateStore", "not DataStore"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
