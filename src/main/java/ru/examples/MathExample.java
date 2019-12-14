package ru.examples;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Сергей on 08.05.2018.
 */
public class MathExample {

    public static void main(String[] args) {

        // перцентиль
        Percentile percentile = new Percentile();
        List<Double> doubleList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            doubleList.add((double) i);
        }
        System.out.println(percentile.evaluate(doubleList.stream().mapToDouble(d -> d).toArray(), 90));
    }
}
