package com.company.kbrs_2a;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

public class ApiAuthenticationClient {
    private String login;
    private String password;
    private ObjectOutputStream out;
    private ObjectInputStream in;




    public ApiAuthenticationClient(ObjectOutputStream out, ObjectInputStream in, String login, String password)
    {
        this.login=login;
        this.out = out;
        this.in = in;
        this.password = password;

    }

    public String execute(String sk)
    {

        // login and password should be encrypted

        try {

            String str = login + " " + password;

            byte[] encrypted_str = encryptCredentials(sk, str);

            ConnUtill.sendMsg(out, Command.CommandType.VERIFY_CREDENTIALS, encrypted_str);
            Command cmd_key = (Command) in.readObject();
            if (cmd_key.getCommandType().equals(Command.CommandType.ERROR)) {
                throw new IOException("Server responded with ERROR message: " + cmd_key.getParam());
            }
            return cmd_key.getParam();
        }

        catch (Exception e) {
            e.printStackTrace();
            return "false";
        }

    }


    private static byte[] encryptCredentials(String key, String value) throws GeneralSecurityException {

        byte[] raw = key.getBytes(Charset.forName("UTF-8"));
        if (raw.length != 16) {
            throw new IllegalArgumentException("Invalid key size.");
        }

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec,
                new IvParameterSpec(new byte[16]));
        return cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));

    }

}
