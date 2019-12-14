package ru.examples;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilesSortingExample {

    public static void main(String[] args) {

        File[] files = {
                new File("file.log"),
                new File("file_2019_10_29_998.log"),
                new File("file_2019_10_29_31.log"),
                new File("file_2019_10_29_32.log"),
                new File("file_2019_10_29_0.log"),
                new File("file_2019_10_29_1.log"),
                new File("file_2019_10_29_100.log"),
                new File("file_2019_10_29_2.log"),
                new File("file_2019_10_29_10.log"),
                new File("file_2019_10_29_11.log"),
                new File("file_2019_10_29_20.log"),
                new File("file_2019_10_29_21.log")
        };

        Arrays
            .stream(files)
            .sorted(new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    int n1 = 9999, n2 = 9999;
                    String fn1 = f1.toString();
                    String fn2 = f2.toString();

                    Pattern pattern = Pattern.compile("_([0-9]+)\\.log", Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(fn1);

                    if (matcher.find()) {
                        n1 = Integer.parseInt(matcher.group(1));
                    }

                    matcher = pattern.matcher(fn2);

                    if (matcher.find()) {
                        n2 = Integer.parseInt(matcher.group(1));
                    }

/*
                    int b = fn1.lastIndexOf("_") + 1;
                    int e = fn1.lastIndexOf(".");

                    if (b > 0) {
                        n1 = Integer.parseInt(fn1.substring(b, e));
                    }

                    b = fn2.lastIndexOf("_") + 1;
                    e = fn2.lastIndexOf(".");
                    if (b > 0) {
                        n2 = Integer.parseInt(fn2.substring(b, e));
                    }
*/

                    return n1 - n2;
                }
            })
            .forEach(f -> System.out.println(f.getName()));
    }
}
