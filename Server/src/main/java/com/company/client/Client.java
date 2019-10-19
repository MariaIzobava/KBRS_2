package main.java.com.company.client;

import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.rsa.RSAKeyPairGenerator;
import main.java.com.company.rsa.RSAUtil;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class Client {

    private static final String PATH = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/";

    public static void main(String[] args) {

        String host = "127.0.0.1";
        int port = 32000;
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Generate RSA and send publicKey to the Server
            generateRSA(out);

            Scanner scanner = new Scanner(System.in);
            String line;
            while (true) {
                line = scanner.nextLine();
                if (line.equals("new RSA")) {
                    generateRSA(out);
                }
                else if (line.equals("get text")) {
                    System.out.println("Please, write down text name");
                    line = scanner.nextLine();
                    ConnUtill.sendMsg(out, Command.CommandType.REQUEST_TEXT, line);

                    // Receive encoded text from server
                    Command cmd_file = (Command) in.readObject();
                    if (cmd_file.getCommandType().equals(Command.CommandType.ERROR)) {
                        System.out.println("Server responded with ERROR message: " + cmd_file.getParam());
                        continue;
                    }

                    // Receive encoded session key from server
                    Command cmd_key = (Command) in.readObject();
                    if (cmd_key.getCommandType().equals(Command.CommandType.ERROR)) {
                        System.out.println("Server responded with ERROR message: " + cmd_key.getParam());
                        continue;
                    }

                    System.out.println("File and session key were received successfully");

                    String encodedTextPath = cmd_file.getParam();
                    String decodedTextPath = new String(PATH + line + "_decoded" + ".txt");

                    System.out.println("Encoded text is in " + encodedTextPath);
                    System.out.println("Decoded text will be in " + decodedTextPath);

                    String decryptedSessionKey = RSAUtil.decrypt(cmd_key.getParam());
                    FileCipher task = new FileCipher(encodedTextPath,
                            decodedTextPath, decryptedSessionKey, false, OperationMode.Mode.CFB);

                    task.cryptFile();
                }
                else {
                    System.out.println("Command is not defined, available commands are: \n " +
                                        "\"new RSA\" - to regenerate new RSA keys; \n " +
                                        "\"get text\" - to get text from server.");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateRSA(ObjectOutputStream out) throws Exception {

        // Generate new RSA
        RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
        RSAUtil.init(Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()),
                Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded()));

        // Send RSA public key to the server
        ConnUtill.sendMsg(out, Command.CommandType.UPDATE_RSA,
                Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()));
    }
}