package ru.examples.jsonExample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JacksonExample {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String fileName = "ClassA.json";

        List<ClassA> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new ClassA(i, "s"+i, Arrays.asList(i+"0", i+"1")));
        }
        mapper.writeValue(new File(fileName), list);

        List<ClassA> list2 = mapper.readValue(new File(fileName), new TypeReference<List<ClassA>>() {} );
        list2.forEach(x -> {
            System.out.println(x.getS());
        });
    }

    static class ClassA{
        int x;
        String s;
        List<String> list;

        public ClassA() {
        }

        public ClassA(int x, String s, List<String> list) {
            this.x = x;
            this.s = s;
            this.list = list;
        }

        public int getX() {
            return x;
        }

        public String getS() {
            return s;
        }

        public List<String> getList() {
            return list;
        }
    }
}
