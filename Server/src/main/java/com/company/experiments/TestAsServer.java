package main.java.com.company.experiments;

import main.java.com.company.gm.GM;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.rsa.RSA;

public class TestAsServer {

    public static final String RSA_PUBLIC_KEY = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/rsa_public.txt";
    public static final String RSA_PRIVATE_KEY = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/rsa_private.txt";
    public static final String IDEA_INITIAL_TEXT = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/in1.txt";
    public static final String IDEA_ENCODED_TEXT = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/out.txt";
    public static final String IDEA_DECODED_TEXT = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/out2.txt";

    public static void main(String[] args) {

        try {
            GM gm = new GM();
            String encr = gm.encrypt("Mark does not like Masha:(");

            String decr = gm.decrypt(encr);
            System.out.println(decr);

            RSA rsa = new RSA();
            String en = rsa.encrypt("Masha still likes Mark though:)");

            String dec = rsa.decrypt(en);
            System.out.println(dec);
//            RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
//            keyPairGenerator.writeToFile(RSA_PUBLIC_KEY, keyPairGenerator.getPublicKey().getEncoded());
//            keyPairGenerator.writeToFile(RSA_PRIVATE_KEY, keyPairGenerator.getPrivateKey().getEncoded());
//            System.out.println("RSA/publicKey: " + Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()));
//            System.out.println("RSA/privateKey: " + Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded()));
//
//            RSAUtil.init(Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()),
//                    Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded()));
//
//
//            String encryptedString = Base64.getEncoder().encodeToString(RSAUtil.encrypt("Random text for testing"));
//            System.out.println("Encrypted String: " + encryptedString);
//            String decryptedString = RSAUtil.decrypt(encryptedString);
//            System.out.println("Eecrypted String: " + decryptedString);
//
//
//            FileCipher task = new FileCipher(IDEA_INITIAL_TEXT,
//                    IDEA_ENCODED_TEXT, KEY, true, OperationMode.Mode.CFB);
//            FileCipher task2 = new FileCipher(IDEA_ENCODED_TEXT,
//                    IDEA_DECODED_TEXT, KEY, false, OperationMode.Mode.CFB);
//
//
//            task.cryptFile();
//            task2.cryptFile();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static final String KEY = "KEY";
}
