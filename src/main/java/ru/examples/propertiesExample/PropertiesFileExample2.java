package ru.examples.propertiesExample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.utils.files.PropertiesService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Map.Entry.comparingByKey;

public class PropertiesFileExample2 {

    public static void main(String[] args) {
        DateFormat datetimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        String propertiesFileName = "smtp.properties";
        Map<String, String> paramsMap = new LinkedHashMap<String, String>(){{
            put("server","smtp.rambler.ru");
            put("port","465");
            put("user","123");
            put("TEST_DATA", "20/05/2018 08:29");
            put("JSON", "{\"Tag1\":\"val1\",\"Tag2\":\"val2\"}");
            put("JSON_ARRAY", "[{\"Tag11\":\"val11\",\"Tag12\":\"val12\"}, {\"Tag21\":\"val21\",\"Tag22\":\"val22\"}]");
            put("STRING_LIST", "s1,s2");
            put("INT_LIST", "1,2,3,4,5,6,7,8,9,0");
        }};

//        PropertiesService propertiesService = new PropertiesService(); // список параметров не задан, берем все из файла
//        PropertiesService propertiesService = new PropertiesService(propertiesFileName, paramsMap); // список параметров задан

        PropertiesService propertiesService = new PropertiesService(paramsMap); // список параметров задан

        JSONObject jsonObject = propertiesService.getJSONObject("JSON");
        if (jsonObject != null) {
            try {
                System.out.println("Tag1 = " + jsonObject.getString("Tag1"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Объект JSON заданный по умолчаию перечитан из файла\r\n");

        propertiesService.readProperties(propertiesFileName);


        System.out.println("==========================");
        System.out.println(propertiesService.getString("user"));

        System.out.println(propertiesService.getString("TEST_DATA"));
        System.out.println(propertiesService.getDate("TEST_DATA"));
        System.out.println(propertiesService.getDate("TEST_DATA", "dd/MM/yyyy HH:mm"));


        JSONObject jsonObject2 = propertiesService.getJSONObject("JSON");
        if (jsonObject2 != null) {
            try {
                System.out.println(jsonObject2.getString("FirstName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONArray jsonArray = propertiesService.getJSONArray("JSON_ARRAY");
        if (jsonArray != null){
            try {
                System.out.println(jsonArray);
                System.out.println(jsonArray.getJSONObject(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


//        List<MyClass> myClassList = propertiesService.getJsonList("MY_CLASS_JSON", new TypeToken<List<MyClass>>(){});

        for (String s: propertiesService.getStringList("STRING_LIST")){
            System.out.println(s);
        }

        for (int i: propertiesService.getIntList("INT_LIST")){
            System.out.println(i);
        }

        propertiesService.setProperty("NEW_PROPERTY", "NEW_VALUE " + datetimeFormat.format(System.currentTimeMillis()));
    }

}
