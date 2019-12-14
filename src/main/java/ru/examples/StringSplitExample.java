package ru.examples;

import java.util.Arrays;

/**
 * Created by Сергей on 29.04.2018.
 */
public class StringSplitExample {
    public static void main(String[] args) {
        String dataString = "{id0=0, id1='1', id2='2', id3=3, id4=4}";
        String[] dataArray = dataString.split(",");
        MyData myData = new MyData();
        Arrays.stream(dataArray)
                .map(x -> x.replaceAll("}","")
                        .replaceAll("'","")
                        .replaceAll(" ",""))
                .forEach(x -> {
//                    System.out.println(x);
                    String[] tagAndValue = x.split("=");
                    switch (tagAndValue[0]){
                        case "id1":
                            myData.setId1(tagAndValue[1]);
                        case "id2":
                            myData.setId2(tagAndValue[1]);
                        case "id3":
                            myData.setId3(Long.parseLong(tagAndValue[1]));
                        case "id4":
                            myData.setId4(Long.parseLong(tagAndValue[1]));
                    }
//                    System.out.print("tag=" + tagAndValue[0]);
//                    System.out.println(", value=" + tagAndValue[1]);

                });

        System.out.println(
                "id1="+myData.getId1()+
                ", id2="+myData.getId2()+
                ", id3="+myData.getId3()+
                ", id4="+myData.getId4());
    }

    static class MyData {
        private String id1;
        private String id2;
        private long id3;
        private long id4;

        public String getId1() {
            return id1;
        }

        public void setId1(String id1) {
            this.id1 = id1;
        }

        public String getId2() {
            return id2;
        }

        public void setId2(String id2) {
            this.id2 = id2;
        }

        public long getId3() {
            return id3;
        }

        public void setId3(long id3) {
            this.id3 = id3;
        }

        public long getId4() {
            return id4;
        }

        public void setId4(long id4) {
            this.id4 = id4;
        }
    }
}
