package ru.examples.builderExample;

public class BuilderExample {
    public static void main(String[] args) {
        BuilderClass testClass = new BuilderClass.Builder()
//                .dbDriver("driver1")
                .dbHost("host1")
                .dbBase("base1")
                .dbUserName("userName1")
                .dbPassword("password1")
                .build();

        testClass.connect();
    }
}
