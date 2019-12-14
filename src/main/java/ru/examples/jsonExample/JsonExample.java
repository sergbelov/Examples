package ru.examples.jsonExample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class JsonExample {

    static StringBuilder jsonStringBuilder = new StringBuilder();
    static StringBuilder jsonStringBuilderFormat = new StringBuilder();

    public static void main(String[] args) {

        String fileName = "jsonExample.json";
        String currEncoding = "UTF-8";
        String newEncoding = "cp1251";

        int bytesRead = -1;
        byte[] buffer = new byte[1024];
        StringBuilder jsonSB = new StringBuilder();

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
            System.out.println(jsonObject);

            System.out.println("==========================");
            jsonPrintKeys(jsonObject, "");
            System.out.println(jsonStringBuilder.toString());
            System.out.println(jsonStringBuilderFormat.toString());
            System.out.println("==========================");

            JSONObject jsonObjectAddress = jsonObject.getJSONObject("Адрес");
            System.out.println(jsonObjectAddress);
            jsonObjectAddress.put("Город", "Московия");

            jsonObjectAddress.put("Город2", "Московия2");

            JSONArray jsonArrayChildrens = jsonObject.getJSONArray("Дети");
            for (int i = 0; i < jsonArrayChildrens.length(); i++) {
                JSONObject jsonObjectChildren = jsonArrayChildrens.getJSONObject(i);

                System.out.println(jsonObjectChildren);
                jsonObjectChildren.put("Имя", jsonObjectChildren.getString("Имя") + (i + 1));
                System.out.println(jsonObjectChildren.get("Имя"));
            }

            System.out.println(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    
    private static void jsonPrintKeys(JSONObject jsonObject, String level) throws JSONException {

        jsonStringBuilderFormat
                .append(level)
                .append("{")
                .append("\r\n");

        jsonStringBuilder.append("{");

        Iterator<?> keys = jsonObject.sortedKeys();
        while (keys.hasNext()){
            String key = (String)keys.next();

            jsonStringBuilderFormat
                    .append(level+"\t\"")
                    .append(key)
                    .append("\": ");

            jsonStringBuilder
                    .append("\"")
                    .append(key)
                    .append("\":");

            if (jsonObject.get(key) instanceof JSONObject){
                jsonStringBuilderFormat.append("\r\n");
                jsonPrintKeys((JSONObject)jsonObject.get(key), level+"\t");

            } else if (jsonObject.get(key) instanceof JSONArray){
                JSONArray jsonArray = jsonObject.getJSONArray(key);

                jsonStringBuilderFormat.append("[").append("\r\n");
                jsonStringBuilder.append("[");

                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (i>0){
                            jsonStringBuilderFormat.append(",").append("\r\n");
                            jsonStringBuilder.append(",");
                        }
                        jsonPrintKeys(jsonArray.getJSONObject(i), level + "\t");
                    }
                }
                jsonStringBuilderFormat.append("]");
                jsonStringBuilder.append("]");

            } else{
                if (jsonObject.getString(key) != "null" &&
                        jsonObject.getString(key) != "true" &&
                        jsonObject.getString(key) != "false" ) {

                    jsonStringBuilderFormat.append("\"");
                    jsonStringBuilder.append("\"");
                }
                jsonStringBuilderFormat.append(jsonObject.getString(key));
                jsonStringBuilder.append(jsonObject.getString(key));
                if (jsonObject.getString(key) != "null" &&
                        jsonObject.getString(key) != "true" &&
                        jsonObject.getString(key) != "false" ) {

                    jsonStringBuilderFormat.append("\"");
                    jsonStringBuilder.append("\"");
                }
            }

            if ((keys.hasNext())) {
                jsonStringBuilderFormat.append(",");
                jsonStringBuilder.append(",");
            }
            jsonStringBuilderFormat.append("\r\n");
        }

        jsonStringBuilderFormat.append(level).append("}");
        jsonStringBuilder.append("}");
    }
}
