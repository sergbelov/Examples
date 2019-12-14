package ru.examples.jsonExample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Сергей on 14.04.2018.
 */
public class GsonExample {

    static final String fileName = "ClassAList.json";
    static List<A> aList = new ArrayList<>();

    public static void main(String[] args) {

        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                for (int c = 0; c < 3; c++) {
                    aList.add(new A(a, b, c, new int[] {1, 2, 3, 4, 5}));
                }
            }
        }
        // запишем массив с вопросами в Json-файл
//        Gson gson = new Gson(); // без форматирования

        Gson gson = new GsonBuilder()
                .setPrettyPrinting() // с форматированием
                .create();

        String aJson = gson.toJson(aList);
//        System.out.println(aJson.toString());
        try (
//            FileWriter fw = new FileWriter(fileName, false);
                BufferedWriter fw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(fileName, false),
                                "UTF-8"));
        ) {

//            fw.write("{\"classA\":"+ aJson +"}"); // для последующего чтения через Object
            fw.write(aJson); // для чтения через Gson.fromJson() (сразу в List<XML_element>)
            fw.flush();
            fw.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;

//        readJson(); // читаем данные из Json
        readJson2();
    }


    private static void readJson() {

        StringBuilder jsonSB = new StringBuilder();
        String currEncoding = "UTF-8";
        int bytesRead = -1;
        byte[] buffer = new byte[1024];
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                jsonSB.append(new String(Arrays.copyOf(buffer, bytesRead), currEncoding));
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonSB.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("classA");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectFromArray = jsonArray.getJSONObject(i);

                System.out.println(jsonObjectFromArray);
                System.out.println(jsonObjectFromArray.get("a"));
                System.out.println(jsonObjectFromArray.get("b"));
                System.out.println(jsonObjectFromArray.get("c"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static void readJson2() {

        Gson gson = new GsonBuilder() // с форматированием
                .setPrettyPrinting()
                .create();

        try(
            JsonReader reader = new JsonReader(new InputStreamReader(
                                                new FileInputStream(fileName),
                                                "UTF-8"));
           )
        {
            aList = gson.fromJson(reader, new TypeToken<List<A>>() {
            }.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        aList.add(new A(9, 9, 9, new int[] {3, 4, 5, 6, 7, 8, 9, 0}));
        String aJson = gson.toJson(aList);
        // можно записать в файл
        System.out.println(aJson);
/*
        System.out.println("Подождемс...");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Ожидание окончено");
*/
    }

    static class A {
        int a;
        int b;
        int c;
        int[] d;

        public A(int a, int b, int c, int[] d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

/*
        public int getX() { return a; }

        public int getY() {
            return b;
        }

        public int getZ() {
            return c;
        }
*/
    }
}
