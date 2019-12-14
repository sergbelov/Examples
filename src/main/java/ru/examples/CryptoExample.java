package ru.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.RSACryptoService;
import ru.utils.files.PropertiesService;

import java.io.*;
import java.math.BigInteger;

public class CryptoExample {

    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {

        int keySize = 1024;
        String privateKeyFileName = "test_key.prv";
        String publicKeyFileName = "test_key.pub";

        RSACryptoService rsaCryptoService = new RSACryptoService();
//        rsaCryptoService.createKeyPair(keySize, "test_key");
        rsaCryptoService.initKeyPair(keySize, "test_key");

        String originalString = "1234567890\n" +
                "Скажи ка дядя, ведь не даром...\n" +
                "1234567890";
/*

        byte[] originalBytes = originalString.getBytes();

        byte[] encrypt = rsaCryptoService.encrypt(
                originalBytes,
                keySize,
                publicKeyFileName);

        System.out.println("После дешифрации\n" +
                new String(rsaCryptoService.decrypt(
                        encrypt,
                        keySize,
                        privateKeyFileName)).trim());
*/

        byte[] encrypt = rsaCryptoService.encrypt(
                originalString,
                keySize,
                publicKeyFileName);

        System.out.println("После дешифрации\n" +
                rsaCryptoService.decryptToString(
                        encrypt,
                        keySize,
                        privateKeyFileName));


//        String byteToBigIntegerToString = new BigInteger(1, encrypt).toString(16);
        String byteToBigIntegerToString = rsaCryptoService.byteArrayToString(encrypt);
        System.out.println("\nByteToBigIntegerToString\n" + byteToBigIntegerToString);

//        byte[] stringToBigIntegerToByte = new BigInteger(byteToBigIntegerToString, 16).toByteArray();
        byte[] stringToBigIntegerToByte = rsaCryptoService.stringToByteArray(byteToBigIntegerToString);

        System.out.println("\nStringToBigIntegerToByte (decrypt)\n" + new String(
                rsaCryptoService.decrypt(
                        stringToBigIntegerToByte,
                        keySize,
                        privateKeyFileName)).trim());

        System.out.println("\nStringToBigIntegerToByte (decryptToString)\n" +
                rsaCryptoService.decryptToString(
                        stringToBigIntegerToByte,
                        keySize,
                        privateKeyFileName));


        String encryptString = rsaCryptoService.encryptToString(
                originalString,
                keySize,
                publicKeyFileName);

        System.out.println(rsaCryptoService.decryptToString(
                encryptString,
                keySize,
                privateKeyFileName));


        System.out.println("\n==========================\n");
        String propertiesFileName = "propertiesExample.properties";
        PropertiesService propertiesService = new PropertiesService();
        propertiesService.readProperties(propertiesFileName);

/*
        System.out.println("\nДешифрация параметра из файла (шифрованные данные в формате ByteToBigIntegerToString)\n" +
                new String(rsaCryptoService.decrypt(
                        new BigInteger(
                                propertiesService.getString("EncryptString"),
                                16).toByteArray(),
                        keySize,
                        privateKeyFileName)).trim());
*/

        if (
//                propertiesService.containsKey("EncryptString")
            propertiesService.getString("EncryptString") != null
        ){
            System.out.println("\nДешифрация параметра из Properties (getString)\n" +
                    rsaCryptoService.decryptToString(
                            propertiesService.getString("EncryptString"),
                            keySize,
                            privateKeyFileName));

            System.out.println("\nДешифрация параметра из Properties (getByteToArray)\n" +
                    rsaCryptoService.decryptToString(
                            propertiesService.getByteArray("EncryptString"),
                            keySize,
                            privateKeyFileName));
        }



/*
        byte[] data = new byte[keySize / 8];

        // читаем не зашифрованные данные
        try (
                DataInputStream dataInputStream = new DataInputStream(
                        new FileInputStream("FileData.txt"));
        ) {
            dataInputStream.read(data);
            LOG.debug("{}", new String(data).trim());

        } catch (IOException e) {
            e.printStackTrace();
        }


        // шифруем, сохраняем
        byte[] cipher = rsaCryptoService.encrypt(data);
        LOG.debug("{}", new String(cipher).trim());

        try(
                DataOutputStream dataOutputStream = new DataOutputStream(
                        new FileOutputStream("FileCipher.txt"));
        ) {
            dataOutputStream.write(cipher);
            dataOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(new String(rsaCryptoService.decrypt(cipher)).trim());
*/
    }
}
