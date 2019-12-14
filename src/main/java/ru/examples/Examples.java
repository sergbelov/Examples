package ru.examples;

import com.google.common.base.Joiner;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Examples {

    public static void main(String[] args) {

// Color to HTML-color
        Color color = new Color(100,100,100);
        System.out.println("#" + Integer.toHexString(color.getRGB()).substring(2).toLowerCase());

// String to array
        String dataString = "1 2 3 4 5 6 7 8 9 0";
        String[] dataArray = dataString.split(" ");
        Arrays.stream(dataArray).forEach(x -> System.out.print(x + ", "));

// List to String
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++){ list.add(String.valueOf(i)); }
        System.out.println( Joiner.on("\t").join(list));

    }
}
