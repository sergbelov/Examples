package ru.examples;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamExample {

    public static void main(String[] args) {

        List<TestClass> list = new ArrayList<>();

        for (int a = 1; a < 5; a++) {
            for (int b = 0; b < 5; b++) {
                for (int c = 0; c < 5; c++) {
                    list.add(new TestClass(
                            String.valueOf((int) (Math.random() * 10)),
                            String.valueOf((int) (Math.random() * 10)),
                            String.valueOf((int) (Math.random() * 10)),
                            (long) (Math.random() * 9999999l)));
                }
            }
        }


// ==========
        System.out.println("==== insert into array from array ====");

        List<Integer> newList = new ArrayList<>(
                list
                    .stream()
                    .map(TestClass::getIntA)
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList()));

        newList.forEach(x -> System.out.println(x));


// ==========
        System.out.println("==== select a,b,c,d from list order by a,b,c,d ====");

        list
                .stream()
                .sorted()
                .forEach((x) -> System.out.println(
                        " a=" + x.getA() +
                        " b=" + x.getB() +
                        " c=" + x.getC() +
                        " d=" + x.getD()));

        System.out.println("==== select a,b,c,d from list order by b,a,c,d ====");

        list
                .stream()
                .sorted(Comparator.comparing((TestClass tc) -> tc.getB())
                        .thenComparing(tc -> tc.getA())
                        .thenComparing(tc -> tc.getC())
                        .thenComparing(tc -> tc.getD()))
                .forEach((x) -> System.out.println(
                        " a=" + x.getA() +
                                " b=" + x.getB() +
                                " c=" + x.getC() +
                                " d=" + x.getD()));


// ==========
        System.out.println("==== select a from list group by a ====");

        List<String> list2 = new ArrayList<String>(
                list
                    .stream()
//                .map((x)->x.getX())
                    .map(TestClass::getA)
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList()));
/*
                    .collect(
                        Collectors.groupingBy(TestClass::getX,
                        Collectors.mapping(TestClass::getX,
                        Collectors.toList())))
                    .keySet());
*/
        list2.forEach(x -> System.out.println(x));


// список из 5 случайных элементов where a=2
        System.out.println("==== select top 5 recno() from list where a = 2 (random) ====");
        Random random = new Random();
        int[] numList = IntStream
                .generate(() -> random.nextInt(list.size()))
                .filter((n) -> list.get(n).getA().equals("2"))
                .distinct()
                .limit(5)
                .toArray();

        for (int i = 0; i < numList.length; i++) {
            System.out.println("a=" + list.get(numList[i]).getA() + " b=" + list.get(numList[i]).getB());
        }

// ==========
        System.out.println("==== array to stream ====");
        Arrays
                .stream(numList)
                .forEach(n -> System.out.println(n));


// insert into list3 select b from list where a=2
        System.out.println("==== select b from list where a=2 ====");
        List<String> list3 = new ArrayList<>(
                list
                        .stream()
                        .filter((x) -> x.getA().equals("2"))
                        .map(TestClass::getB)
                        .sorted()
//                        .distinct()
                        .collect(Collectors.toList()));

        list3.forEach((x) -> System.out.println(x));

// ==========
        System.out.println("==== select top 10 key,value from map where key%2=0 ====");

        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(i, "String" + String.valueOf(i));
        }

        map
                .entrySet()
                .stream()
                .filter((n) -> n.getKey() % 2 == 0)
                .limit(10)
                .forEach((n) -> System.out.println("key=" + n.getKey() + " value=" + n.getValue()));

// ==========
        System.out.println("==== select count() from list where key%2=0 ====");
        int cnt = (int) list
                .stream()
                .filter((x) -> x.getIntA() % 2 == 0)
                .count();
        System.out.println(cnt);


// ==========
        System.out.println("==== select a, b ,c from list where b=2 order by a ====");
        list
            .stream()
            .filter(e -> e.getB().equals("2"))
            .sorted(
                    new Comparator<TestClass>() {
                        @Override
                        public int compare(TestClass o1, TestClass o2) {
//                            return o1.getA().compareTo(o2.getA());
                            return (int) (o1.getD() - o2.getD());
                        }
                    })
            .forEach(x -> System.out.println(
                    x.getA() + " " +
                    x.getB() + " " +
                    x.getC() + " " +
                    x.getD()));


// ==========
        System.out.println("==== select sum(a) from list where b=2 ====");
        int sumA = list
                .stream()
                .filter(x -> x.getB().equals("2"))
                .mapToInt(TestClass::getIntA)
                .sum();
        System.out.println("Sum(a for b=2) = " + sumA);

// ==========
        System.out.println("==== select a, b, c, d from list where b=2 and d = (select top 1 d from list where b=2 order by d) ====");
        TestClass minTestClass = list
            .stream()
            .filter(x -> x.getB().equals("2"))
//            .min((o1, o2) -> Long.compare(o1.getD(), o2.getD()))
            .min(Comparator.comparingLong(TestClass::getD))
            .get();

        System.out.println(
                minTestClass.getA() + " " +
                minTestClass.getB() + " " +
                minTestClass.getC() + " " +
                minTestClass.getD());

// ==========
        System.out.println("==== select min(d) from list where b=2 ====");
        OptionalLong minD = list
                .stream()
                .filter(x -> x.getB().equals("2"))
                .mapToLong(TestClass::getD)
                .min();
        System.out.println("Min d for b=2= " + minD.getAsLong());

    }




// ========== class ==========
    static class TestClass implements Comparable<TestClass> {
        private String a;
        private String b;
        private String c;
        private long d;

        public TestClass() {
        }

        public TestClass(
                String a,
                String b,
                String c,
                long d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public int getIntA() {
            return Integer.parseInt(a);
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public long getD() { return d; }

        public void setD(long d) { this.d = d; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(this instanceof TestClass)) return false;

            TestClass tc = (TestClass) obj;
            return this.getA() == tc.getA() &&
                    this.getB() == tc.getB() &&
                    this.getC() == tc.getC() &&
                    this.getD() == tc.getD();
        }


        @Override
        public int compareTo(TestClass o) {
            if (this == o) return 0;

            int compare = this.getA().compareTo(o.getA());
            if (compare != 0) return compare;

            compare = this.getB().compareTo(o.getB());
            if (compare != 0) return compare;

            compare = this.getC().compareTo(o.getC());
            if (compare != 0) return compare;

            return (int) (this.getD() - o.getD());
        }

    }
}
