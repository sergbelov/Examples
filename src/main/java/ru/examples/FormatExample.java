package ru.examples;

import java.text.DecimalFormat;

public class FormatExample {
    public static void main(String[] args) {

        DecimalFormat decimalFormat = new DecimalFormat("##.####");
        float d = (float) (100/6.00);
        System.out.println(d);
        System.out.println(decimalFormat.format(d));

        System.out.printf("%-5s|%-5d|%20s|%10.4f%n", "USD", 1, "Американский доллар", 25.2345);
        System.out.printf("%-5s|%-5d|%20s|%10.4f%n", "EUR", 1, "Евро", 30.1523);

        String fString = String.format("%10.2f", d);
        System.out.println(fString);
    }
}
