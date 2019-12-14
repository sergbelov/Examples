package ru.examples;

import java.util.EmptyStackException;

public class ThrowsExample {

    public static void main(String[] args) throws Throwable{
        int r;

        try {
            r = metod(8, 2, 0);
            System.out.println(r);
        } catch (Exception e) {
            System.out.println("Возникла ошибка");
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
//            e.printStackTrace();
        }
    }

    private static int metod(int x, int y, int d) throws Throwable {
        switch (d) {
            case 1: // +
                return x + y;
            case 2: // -
                return x - y;
            case 3: // *
                return x * y;
            case 4:
                return x / y;
            default:
//                throw new Throwable("не определено действие [1,2,3,4]");
//                throw new EmptyStackException();
//                throw new NullPointerException("не определенное действие [1,2,3,4]");
                throw new UnsupportedOperationException("не определено действие [1,2,3,4]");
        }
    }

}
