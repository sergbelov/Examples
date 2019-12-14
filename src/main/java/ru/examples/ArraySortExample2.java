package ru.examples;

public class ArraySortExample2 {

    public static void main(String[] args) {

        int arraySize = 10;
        int[] array = new int[arraySize];
        for (int i = 0; i < arraySize; i++){
            array[i] = (int) (Math.random() * arraySize);
        }

        for (int i = 0; i < arraySize; i++){
            System.out.println(array[i]);
        }

        // сортируем
        long timeStart = System.currentTimeMillis();
        int mem;
        for (int i = 0; i < array.length; i++){
//            for (int j = i; j < array.length; j ++){
            for (int j = 0; j < i; j ++){
                if (i != j && array[i] < array[j]){ // возрастание
//                if (i != j && array[i] > array[j]){ // убывание
                    mem = array[i];
                    array[i] = array[j];
                    array[j] = mem;
                }
            }
        }
        System.out.println("Время сортировки: " + (System.currentTimeMillis() - timeStart));

        System.out.println("=======================================");
        for (int i = 0; i < array.length; i++){
            System.out.println(array[i]);
        }

    }
}
