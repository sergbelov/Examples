package ru.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSACryptoService {

    private static final Logger LOG = LogManager.getLogger();
    private final String DEFAULT_ALGORITHM = "RSA";


    // инициализация приватного и публичного ключей
    public void initKeyPair(
            int keySize,
            String fileName) {

        initKeyPair(keySize, fileName, DEFAULT_ALGORITHM);
    }

    // инициализация приватного и публичного ключей
    public void initKeyPair(
            int keySize,
            String fileName,
            String algorithm) {

        File privateKeyFile = new File(fileName + ".prv");
        File publicKeyFile = new File(fileName + ".pub");

        // не найден файл с приватным или публичным ключем, создадим новую пару
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            createKeyPair(keySize, fileName, algorithm);
        }
    }


    // генерация новых публичного и приватного ключей
    public void createKeyPair(
            int keySize,
            String fileName) {

        createKeyPair(keySize, fileName, DEFAULT_ALGORITHM);
    }


    // генерация новых публичного и приватного ключей
    public void createKeyPair(
            int keySize,
            String fileName,
            String algorithm) {

        File privateKeyFile = new File(fileName + ".prv");
        File publicKeyFile = new File(fileName + ".pub");

        KeyPairGenerator kpg = null;
        KeyPair keyPair = null;
        try {
            kpg = KeyPairGenerator.getInstance(algorithm);
            kpg.initialize(keySize);
            keyPair = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        }

        LOG.debug("Создание приватного ключа {}", privateKeyFile);
        try (
                DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(privateKeyFile));
        ) {
            PrivateKey priKey = keyPair.getPrivate();
            byte[] ky = priKey.getEncoded();
            dataOutputStream.write(ky);
            dataOutputStream.flush();

            LOG.info("Приватный ключ {} создан", privateKeyFile);

        } catch (IOException e) {
            LOG.error(e);
        }

        LOG.debug("Создание публичного ключа {}", publicKeyFile);
        try (
                DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(publicKeyFile));
        ) {
            PublicKey pubKey = keyPair.getPublic();
            byte[] ky = pubKey.getEncoded();
            dataOutputStream.write(ky);
            dataOutputStream.flush();

            LOG.info("Публичный ключ {} создан", publicKeyFile);

        } catch (IOException e) {
            LOG.error(e);
        }
    }


    /*
    шифруем
     */
    public byte[] encrypt(
            String data,
            int keySize,
            String publicKeyFileName) {

        return encrypt(
                data.getBytes(),
                keySize,
                publicKeyFileName);
    }

    public byte[] encrypt(
            byte[] data,
            int keySize,
            String publicKeyFileName) {

        File publicKeyFile = new File(publicKeyFileName);

        byte[] r = null;
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        }

        try (
                DataInputStream publicKeyStream = new DataInputStream(new FileInputStream(publicKeyFile));
        ) {
            byte[] publicKeyBytes = new byte[keySize]; // 1024 bits
            publicKeyStream.read(publicKeyBytes);
            publicKeyStream.close();
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Cipher cf = Cipher.getInstance("RSA/ECB/NoPadding");
            cf.init(Cipher.ENCRYPT_MODE, publicKey);
            r = cf.doFinal(data);

//            LOG.debug("До шифрования: \n{}", new String(data));
//            LOG.debug("После шифрования: \n{}", new String(r));

        } catch (IOException |
                NoSuchPaddingException |
                NoSuchAlgorithmException |
                InvalidKeyException |
                InvalidKeySpecException |
                BadPaddingException |
                IllegalBlockSizeException e) {

            LOG.error(e);
        }

        return r;
    }

    public String encryptToString(
            byte[] data,
            int keySize,
            String publicKeyFileName) {

        return byteArrayToString(encrypt(
                data,
                keySize,
                publicKeyFileName));
    }

    public String encryptToString(
            String data,
            int keySize,
            String publicKeyFileName) {

        return byteArrayToString(encrypt(
                data,
                keySize,
                publicKeyFileName));
    }


    /*
    дешифруем
     */
    public byte[] decrypt(
            String data,
            int keySize,
            String privateKeyFileName) {

        return decrypt(
                stringToByteArray(data),
                keySize,
                privateKeyFileName);
    }

    public byte[] decrypt(
            byte[] data,
            int keySize,
            String privateKeyFileName) {

        File privateKeyFile = new File(privateKeyFileName);

        byte[] r = null;
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        }
        try (
                DataInputStream privateKeyStream = new DataInputStream(new FileInputStream(privateKeyFile));

        ) {
            byte[] privateKeyBytes = new byte[keySize];
            privateKeyStream.read(privateKeyBytes);
            privateKeyStream.close();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            Cipher cf = Cipher.getInstance("RSA/ECB/NoPadding");
            cf.init(Cipher.DECRYPT_MODE, privateKey);
            r = cf.doFinal(data);

//            LOG.debug("До дешифрации: \n{}", new String(data));
//            LOG.debug("После дешифрации: \n{}", new String(r).trim());

        } catch (IOException |
                NoSuchPaddingException |
                NoSuchAlgorithmException |
                InvalidKeyException |
                InvalidKeySpecException |
                BadPaddingException |
                IllegalBlockSizeException e) {

            LOG.error(e);
        }

        return r;
    }

    public String decryptToString(
            byte[] data,
            int keySize,
            String privateKeyFileName) {

        return new String(decrypt(
                data,
                keySize,
                privateKeyFileName)).trim();
    }

    public String decryptToString(
            String data,
            int keySize,
            String privateKeyFileName) {

        return new String(decrypt(
                data,
                keySize,
                privateKeyFileName)).trim();
    }

    public String byteArrayToString(byte[] data) {
        return new BigInteger(1, data).toString(16);
    }

    public byte[] stringToByteArray(String data) {
        return new BigInteger(data, 16).toByteArray();
    }
}
