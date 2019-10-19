package main.java.com.company.experiments;

import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.rsa.RSAKeyPairGenerator;
import main.java.com.company.rsa.RSAUtil;
import main.java.com.company.idea_cipher.modes.OperationMode;

import java.util.Base64;

public class TestAsServer {

    public static final String RSA_PUBLIC_KEY = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/rsa_public.txt";
    public static final String RSA_PRIVATE_KEY = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/rsa_private.txt";
    public static final String IDEA_INITIAL_TEXT = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/in1.txt";
    public static final String IDEA_ENCODED_TEXT = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/out.txt";
    public static final String IDEA_DECODED_TEXT = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/out2.txt";

    public static void main(String[] args) {

        try {
            RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
            keyPairGenerator.writeToFile(RSA_PUBLIC_KEY, keyPairGenerator.getPublicKey().getEncoded());
            keyPairGenerator.writeToFile(RSA_PRIVATE_KEY, keyPairGenerator.getPrivateKey().getEncoded());
            System.out.println("RSA/publicKey: " + Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()));
            System.out.println("RSA/privateKey: " + Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded()));

            RSAUtil.init(Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()),
                    Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded()));


            String encryptedString = Base64.getEncoder().encodeToString(RSAUtil.encrypt("Random text for testing"));
            System.out.println("Encrypted String: " + encryptedString);
            String decryptedString = RSAUtil.decrypt(encryptedString);
            System.out.println("Eecrypted String: " + decryptedString);


            FileCipher task = new FileCipher(IDEA_INITIAL_TEXT,
                    IDEA_ENCODED_TEXT, KEY, true, OperationMode.Mode.CFB);
            FileCipher task2 = new FileCipher(IDEA_ENCODED_TEXT,
                    IDEA_DECODED_TEXT, KEY, false, OperationMode.Mode.CFB);


            task.cryptFile();
            task2.cryptFile();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static final String KEY = "KEY";
}
