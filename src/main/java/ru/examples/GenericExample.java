package ru.examples;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GenericExample {

    enum jsonType {Object, Array}

    ;

    public static void main(String[] args) {
        String[] strList = {"s1", "s2", "s3"};
        Integer[] intList = {1, 2, 3, 4, 5};
        System.out.println(getRandomElement(strList));
        System.out.println(getRandomElement(intList));

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = null;

        jsonObject = requestSync(
                        jsonObject,
                        jsonType.Object);

        jsonArray = requestSync(
                        jsonArray,
                        jsonType.Array);


        requestSync2(jsonObject);

        System.out.println(jsonObject.toString());
        System.out.println(jsonArray.toString());
    }

    private static <T> T getRandomElement(T[] list) {
        return list[(int) (Math.random() * list.length)];
    }

    private static <T> T requestSync(
            T object,
            jsonType type
    ) {
        if (object instanceof org.json.JSONObject){
            System.out.println("org.json.JSONObject");
        }
        if (object instanceof org.json.JSONArray){
            System.out.println("org.json.JSONArray");
        }
        try {
            switch (type) {
                case Object:
                    object = (T) new JSONObject("{\"field1\":\"value1\", \"field2\":\"value2\"}");
                    break;
                case Array:
                    object = (T) new JSONArray("[{\"field1\":\"value1\", \"field2\":\"value2\"},{\"field1\":\"value1\", \"field2\":\"value2\"}]");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        System.out.println(object.toString());
        return (T) object;
    }

    private static boolean requestSync2(
            JSONObject jsonObject
    ){
        try {
            jsonObject = new JSONObject("{\"field1\":\"value1\", \"field2\":\"value2\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}
