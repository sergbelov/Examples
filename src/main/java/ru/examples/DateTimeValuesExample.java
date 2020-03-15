package ru.examples;

import ru.utils.load.data.DateTimeValues;
import ru.utils.load.data.Metric;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DateTimeValuesExample {
    public static void main(String[] args) {
        final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        Metric[] metrics = {
                Metric.DurMin,
                Metric.DurAvg,
                Metric.Dur90,
                Metric.DurMax,
                Metric.Tps,
                Metric.TpsRs,
                Metric.CountCall,
                Metric.CountCallRs,
                Metric.DbCompleted,
                Metric.DbRunning,
                Metric.DbFailed,
                Metric.DbLost,
                Metric.DbDurMin,
                Metric.DbDurAvg,
                Metric.DbDur90,
                Metric.DbDurMax,
                Metric.Errors};

        List<DateTimeValues> dateTimeValuesList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long date = System.currentTimeMillis();
            Map<Metric, Number> map = new LinkedHashMap<>();
            for (int j = 0; j < 10; j++) {
                map.put(metrics[j], i*100 + j);
            }
            dateTimeValuesList.add(new DateTimeValues(date, map));
            if ((int) (Math.random() * 10) > 8) {
                dateTimeValuesList.get(i).setValue(metrics[9],
                        dateTimeValuesList.get(i).getValue(metrics[8]));
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
            for (Map.Entry<Metric, Number> map : dateTimeValues.getValues().entrySet() ) {
                System.out.println(map.getKey().name() + ": " + map.getValue());
            }
            System.out.println("----------------------------");
            for (int i = 0; i < dateTimeValues.size(); i++) {
                Metric key = metrics[i];
                System.out.println(key.name() + ": " + (int) dateTimeValues.getValue(key));
            }

            System.out.println((double) dateTimeValues.getDoubleValue(new Metric[]{metrics[8], metrics[9]}));
            System.out.println(dateTimeValues.compare(metrics[8], metrics[9]));
        }

        System.out.println((int) dateTimeValuesList.get(0).getDoubleValue(new Metric[]{metrics[7], metrics[8], metrics[9]}));
    }
}
