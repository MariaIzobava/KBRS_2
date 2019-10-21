package main.java.com.company.async_encryptions;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA implements AsyncEncription<String, String> {

    private static String privateKey = null;
    private static String publicKey = null;

    public RSA() {
        init();
    }

    public RSA(String key) {
        publicKey = key; // Hope it will work
    }

    public void init() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
            publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            System.out.println("RSA: Public and Private Keys are updated!");
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    private static PublicKey getPublicKeyNew(){
        PublicKey publicKeyNew = null;
        try{
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKeyNew = keyFactory.generatePublic(keySpec);
            return publicKeyNew;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKeyNew;
    }

    private static PrivateKey getPrivateKeyNew(){
        PrivateKey privateKeyNew = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            privateKeyNew = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKeyNew;
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyNew());
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            System.out.println("encrypt " + e);
            return "";
        }
    }

    public String decrypt(String data)  {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyNew());
            return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes())));
        } catch (Exception e) {
            System.out.println("decrypt " + e);
            return "";
        }
    }
}
