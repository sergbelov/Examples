package ru.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArraySortExample {

    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("String" + i);
        }


        Random rnd= new Random();
        for (int i = 1; i < list.size(); i++){
            int r = rnd.nextInt(i);
            String tmp = list.get(i);
            list.set(i, list.get(r));
            list.set(r, tmp);
        }

        list.forEach(x -> System.out.println(x));


        System.out.println("=================================");
        IntStream
            .generate(() -> rnd.nextInt(list.size()))
            .distinct()
            .limit(5)
//            .mapToObj(list::get)
//            .collect(Collectors.toList());
            .forEach((x) -> System.out.println(x));


        System.out.println("=================================");

        Collections.shuffle(list);
        list.forEach((x) -> System.out.println(x));
    }
}
