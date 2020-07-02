package ru.utils;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.OptionalInt;

public class Utils {

//    private static final Logger LOG = LogManager.getLogger();
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /**
     * Генерация последовательности с заданной пропорциональностью
     * @param percents
     * @param size
     * @return
     */
    public int[] getPercentageDistribution(int[] percents, int size){
        int used = 0;
/*
        int percentMax = 0;
        for (int i = 1; i < percents.length; i++){
            if (percents[i] > percents[percentMax]){
                percentMax = i;
            }
        }
*/
        int[] percentsCount = new int[percents.length];
        for (int p = 1; p < percents.length; p++) {
            if (used < size) {
//                percentsCount[p] = (int) Math.ceil(size * percents[p] / 100.00);
                percentsCount[p] = size * percents[p] / 100;
                used = used + percentsCount[p];
            }
        }
        percentsCount[0] = size - used;

        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < percentsCount.length; p++){
            sb.append("(")
                    .append(percents[p])
                    .append("%): ")
                    .append(percentsCount[p])
                    .append("; ");
        }
        LOG.trace("Количество элементов каждого ранга: {}", sb);

        int n;
        int[] data = new int[size];
        Arrays.fill(data, -1);
        for (int p = 0; p < percentsCount.length; p++){
            for (int i = 0; i < percentsCount[p]; i++){
                do{
                    n = (int) (Math.random() * size);
                } while (data[n] != -1);
                data[n] = p;
            }
        }
        return data;
    }


    /**
     * Случайные элемент массива
     * @param array
     * @param <T>
     * @return
     */
    public <T> T getRandomElementFromArray(T [] array){
        return array[(int) (Math.random() * array.length)];
    }
}
