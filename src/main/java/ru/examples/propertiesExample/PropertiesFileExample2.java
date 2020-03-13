package ru.examples.propertiesExample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.examples.jsonExample.JacksonExample;
import ru.utils.files.PropertiesService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Map.Entry.comparingByKey;

public class PropertiesFileExample2 {

    public static void main(String[] args) throws IOException {
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
            put("CLASSA_LIST", "[{\"x\":0,\"y\":1},{\"x\":1,\"y\":2},{\"x\":2,\"y\":3},{\"x\":3,\"y\":4},{\"x\":4,\"y\":5},{\"x\":5,\"y\":6},{\"x\":6,\"y\":7},{\"x\":7,\"y\":8},{\"x\":8,\"y\":9},{\"x\":9,\"y\":10}]");
        }};

//        PropertiesService propertiesService = new PropertiesService(); // список параметров не задан, берем все из файла
//        PropertiesService propertiesService = new PropertiesService(propertiesFileName, paramsMap); // список параметров задан

/*
        List<ClassA> list = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            list.add(new ClassA(i, i+1));
        }
        ObjectMapper mapper = new ObjectMapper();
        String fileName = "ClassA.json";
        mapper.writeValue(new File(fileName), list);
*/

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

        List<ClassA> list = propertiesService.getJsonList("CLASSA_LIST", new TypeReference<List<ClassA>>(){});
        list.forEach(x -> {
            System.out.println(x.getX());
        });


        for (String s: propertiesService.getStringList("STRING_LIST")){
            System.out.println(s);
        }

        for (int i: propertiesService.getIntList("INT_LIST")){
            System.out.println(i);
        }

        propertiesService.setProperty("NEW_PROPERTY", "NEW_VALUE " + datetimeFormat.format(System.currentTimeMillis()));
    }

    public static class ClassA{
        int x;
        int y;

        public ClassA() {
        }

        public ClassA(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
}
