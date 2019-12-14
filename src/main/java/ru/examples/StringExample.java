package ru.examples;

public class StringExample {
    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder("test string");
        char[] buff = new char[6];
        stringBuilder.getChars(5,11, buff, 0);
        System.out.println(buff);

        stringBuilder.setCharAt(1, 'E');
        System.out.println(stringBuilder);

        stringBuilder.insert(4, "123");
        System.out.println(stringBuilder);

        stringBuilder.replace(4, 7, "");
        System.out.println(stringBuilder);
    }
}
