package ru.examples.builderExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuilderClass {

    private static final Logger LOG = LogManager.getLogger();

    private String dbDriver = null;
    private String dbHost;
    private String dbBase;
    private int    dbPort = 1521;
    private String dbUrl = null;
    private String dbUserName;
    private String dbPassword;


    public static class Builder{
        private String dbDriver = null;
        private String dbHost;
        private String dbBase;
        private int    dbPort   = 1251;
        private String dbUrl = null;
        private String dbUserName;
        private String dbPassword;

        public Builder dbDriver(String val){
            dbDriver = val;
            return this;
        }
        public Builder dbHost(String val){
            dbHost = val;
            return this;
        }
        public Builder dbBase(String val){
            dbBase = val;
            return this;
        }
        public Builder dbPort(int val){
            dbPort = val;
            return this;
        }
        public Builder dbUrl(String val){
            dbUrl = val;
            return this;
        }
        public Builder dbUserName(String val){
            dbUserName = val;
            return this;
        }
        public Builder dbPassword(String val){
            dbPassword = val;
            return this;
        }
        public BuilderClass build(){
            return new BuilderClass(this);
        }
    }

    private BuilderClass(Builder builder){
        dbDriver   = builder.dbDriver;
        dbHost     = builder.dbHost;
        dbBase     = builder.dbBase;
        dbPort     = builder.dbPort;
        dbUserName = builder.dbUserName;
        dbPassword = builder.dbPassword;
    }

    public boolean connect(){
        System.out.printf("Connect %s %s %s %s %s %s",
                dbDriver,
                dbHost,
                dbBase,
                dbPort,
                dbUserName,
                dbPassword);
        return true;
    }

}
