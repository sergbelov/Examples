package ru.examples.fileIOExample;

import java.io.*;

public class FileObjectExample {

    public static void main(String[] args) {
        String fileName = "ObjectString.bin";
        String sw = "qwertyuiop";
        write(sw, fileName);
        String sr = (String) read(fileName);
        System.out.println(sr);

        fileName = "ObjectA.bin";
        A aw = new A(1, 2, "3 строка");
        write(aw, fileName);
        A ar = (A) read(fileName);
        System.out.println(ar.getA());
        System.out.println(ar.getB());
        System.out.println(ar.getC());
    }

    public static void write(Object object, String fileName){
        try(FileOutputStream fileOutputStream = new FileOutputStream(fileName)){
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Object read(String fileName){
        try(FileInputStream fileInputStream = new FileInputStream(fileName)){
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class A implements Serializable{
        int a;
        int b;
        String c;

        public A(int a, int b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public String getC() {
            return c;
        }
    }
}