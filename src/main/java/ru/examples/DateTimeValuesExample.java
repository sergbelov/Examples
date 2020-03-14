package ru.examples;

import ru.utils.load.data.DateTimeValues;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DateTimeValuesExample {
    public static void main(String[] args) {
        final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        List<DateTimeValues> dateTimeValuesList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long date = System.currentTimeMillis();
            Map<String, Number> map = new LinkedHashMap<>();
            for (int j = 0; j < 10; j++) {
                map.put("key" + j, i*100 + j);
            }
            dateTimeValuesList.add(new DateTimeValues(date, map));
            if ((int) (Math.random() * 10) > 8) {
                dateTimeValuesList.get(i).setValue("key9", dateTimeValuesList.get(i).getValue("key8"));
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (DateTimeValues dateTimeValues : dateTimeValuesList) {
            System.out.println("============================");
            System.out.println(sdf.format(dateTimeValues.getPeriodEnd()));
            for (Map.Entry<String, Number> map : dateTimeValues.getValues().entrySet() ) {
                System.out.println(map.getKey() + ": " + map.getValue());
            }
            for (int i = 0; i < dateTimeValues.size(); i++) {
                String key = "key"+i;
                System.out.println(key + ": " + (int) dateTimeValues.getValue(key));
            }

//            System.out.println((int) dateTimeValues.getValueSum(new String[]{"key8", "key9"}));
            System.out.println(dateTimeValues.compare("key8", "key9"));
        }

    }
}
