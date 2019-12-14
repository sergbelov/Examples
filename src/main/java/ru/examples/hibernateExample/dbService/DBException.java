package ru.examples.hibernateExample.dbService;

public class DBException extends Exception {
    public DBException(Throwable throwable) {
        super(throwable);
    }
}
