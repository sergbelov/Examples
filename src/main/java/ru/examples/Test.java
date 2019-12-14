package ru.examples;

import ru.utils.Utils;
import ru.utils.files.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Сергей on 08.03.2019.
 */
public class Test {

    public static void main(String[] args) {

        FileUtils fu = new FileUtils();
        List<String> list = new ArrayList<>();
        fu.scanFiles("c:/Common/Prog/Java/GitHub/Examples/", list);
        System.out.println(list.size());
        for (String file: list) {
            System.out.println(file);
        }
        System.exit(0);
/*
        FileUtils fu = new FileUtils();
        String data = "sep=;\r\n11данные;12;13\r\n21данные;22;23\r\n31данные;32;33\r\n";
        fu.writeFile("test.csv", data, "cp1251", true);
*/



        Utils utils = new Utils();
/*
        int[] percent = new int[]{70, 15, 15};
        int[] date = utils.getPercentageDistribution(percent, 9);

        System.out.println("==========");
        for (int i = 0; i < date.length; i++){
            System.out.println(i + " " + date[i]);
        }
*/

        String[] srtringArray = {"1", "2", "3", "4", "5"};
        Integer[] intArray = {1, 2, 3, 4, 5};
        Integer[] intArray2 = {10,};
        System.out.println(utils.getRandomElementFromArray(srtringArray));
        System.out.println(utils.getRandomElementFromArray(intArray));
        System.out.println(utils.getRandomElementFromArray(intArray2));

    }

}
