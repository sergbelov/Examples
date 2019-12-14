package ru.examples.fileIOExample.fileIterator;

/**
 * Created by Сергей on 30.04.2018.
 */
public class FileIteratorExample {
    public static void main(String[] args) {
        FileIterator fileIterator = new FileIterator("c:\\TEMP\\");

        while (fileIterator.hasNext()){
            fileIterator.next();
        }
    }
}
