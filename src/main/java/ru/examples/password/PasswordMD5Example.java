package ru.examples.password;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Created by Сергей on 30.04.2018.
 */
public class PasswordMD5Example {
    public static void main(String[] args) {

//        String password = "123456789012345678901234567890";
        String password = "У лукоморья дуб зеленый";

        String encryptPassword = encryptMD5(password);
        System.out.println(encryptPassword);
        System.out.println(encryptMD5(password).equals("476d1d01aa2f2d1e69f312d59db99d64"));

        System.out.println(encryptMD5_2(password));

        System.out.println(encryptMD5_3(password));

        System.out.println(encryptMD5_X(password));
    }


    public static String encryptMD5(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(data.getBytes(Charset.forName("UTF8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] digest = md.digest();
        return new String(Hex.encodeHex(digest));
    }

    public static String encryptMD5_2(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(data.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }

    public static String encryptMD5_3(String data){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(StandardCharsets.UTF_8.encode(data));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.format("%032x", new BigInteger(1, md.digest()));
    }

    public static String encryptMD5_X(String data){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.reset();
//            md.update(data.getBytes(),0, data.length());
            md.update(data.getBytes(Charset.forName("UTF8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new BigInteger(1, md.digest()).toString(16);
    }

}
