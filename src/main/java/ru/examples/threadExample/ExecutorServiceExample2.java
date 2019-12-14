package ru.examples.threadExample;

import org.json.JSONArray;
import org.json.JSONException;
import sun.swing.BakedArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class ExecutorServiceExample2 {


    public static void main(String[] args) {

//        List<String> list = Collections.synchronizedList(new ArrayList<>()); // общий результат обработки
        List<String> list = new CopyOnWriteArrayList<>();
//        Queue<String> queue = new ConcurrentLinkedQueue<>();

        // Обработка массива данных несколькими потоками
        long startTime = System.currentTimeMillis();

        int p = 0;
        int c = 0;
        int count = 51;
        int maxThread = 10;
        int countThread = Math.min(maxThread, count);
        int step = /*ountThread < maxThread ? countThread :*/ (int) Math.ceil(count * 1.00 / countThread);
        countThread = (int) Math.ceil(count * 1.00 / step); // проверка округлений

        System.out.println("countThread: " + countThread);
        System.out.println("step: " + step);

        // генерим набор данных
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            sb
                .append("{")
                .append("\"data\":")
                .append("\"")
                .append(i)
                .append( "\"}");
            if (i < (count - 1)) sb.append(",");
        }
        sb.append("]");

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonArray);


        CountDownLatch cdl = new CountDownLatch(countThread);
        ExecutorService es = Executors.newFixedThreadPool(countThread);
//        ExecutorService es = Executors.newCachedThreadPool();
        while (c < count) {
            p++;
            es.submit(new RunnubleProcessingData(
                    "поток " + p,
                    jsonArray,
                    c,
                    Math.min(c + step, count),
                    list,
                    cdl));
            c = Math.min(c + step, count);
        }
        es.shutdown();
//        while (!es.isTerminated()) {} // ждем выполнения потоков
    }



    private static class RunnubleProcessingData implements Runnable {

        String name;
        JSONArray jsonArrayData;
        int startRow, stopRow;
        List<String> list;
        CountDownLatch cdl;

        private RunnubleProcessingData(
                String name,
                JSONArray jsonArrayData,
                int startRow,
                int stopRow,
                List<String> list,
                CountDownLatch cdl) {
            this.name = name;
            this.jsonArrayData = jsonArrayData;
            this.startRow = startRow;
            this.stopRow = stopRow;
            this.list = list;
            this.cdl = cdl;
        }

        @Override
        public void run() {
            for (int i = startRow; i < stopRow; i++) {
                try {
                    System.out.println(name + " (" + startRow + " - " + (stopRow - 1) + ") " + jsonArrayData.getJSONObject(i));
                    if (jsonArrayData.getJSONObject(i).getInt("data")%2 == 0){ // выбираем четные записи
                        synchronized (list) {
                            list.add(jsonArrayData.getJSONObject(i).toString());
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cdl.countDown();
            if (cdl.getCount() == 0){
                System.out.println("Пооследний поток закончил свою работу");
//                System.out.println("фактическое количество потоков: " + p);
//                System.out.println("длительность выполнения (mc): " + (System.currentTimeMillis() - startTime));

                // вывод общего результата
                for (int i = 0; i < list.size(); i++){
                    System.out.println((i+1) + ": " + list.get(i));
                }
            }
        }
    }


}
