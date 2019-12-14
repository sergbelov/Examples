package ru.examples.fileIOExample;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScanDirectoryExample {
    public static List<String> arrDirectory = new ArrayList<>();

    public static void main(String[] args) {
        ScanDirectory file = new ScanDirectory();

        file.fileScan("C:\\TEMP", "", "");

        System.out.println("Каталоги");
        arrDirectory.stream().forEach((x) -> System.out.println(x));

/*
        for (String dir : arrDirectory) {
            System.out.println(dir);
        }
*/

    }

    static class ScanDirectory {

        public void fileScan(String path, String pathS, String pathT) {
            File file = new File(path);
            if (file.exists()) {
                File[] listfiles = file.listFiles();
                for (File f : listfiles) {
                    if (!f.isDirectory()) {
//                    System.out.println(s[j].toString());
                    } else if (f.isDirectory()) {
                        ScanDirectoryExample.arrDirectory.add(f.toString());
                        fileScan(f.getPath(), pathS, pathT);
                    }
                }
            }
        }
    }

}