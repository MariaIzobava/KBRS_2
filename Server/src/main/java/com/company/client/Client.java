package main.java.com.company.client;

import main.java.com.company.async_encryptions.GM;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.async_encryptions.RSA;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {

    private static final String PATH = "/";
    private static RSA rsa = null;
    private static GM gm = null;

    public static void main(String[] args) throws IOException {

        String home = new java.io.File( "." ).getCanonicalPath();

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

                    byte[] encodedText = cmd_file.get_byteParam();
                    String encodedTextPath = home + PATH + line + "_encoded" + ".txt";
                    Files.write( Paths.get(encodedTextPath), encodedText);
                    String decodedTextPath = new String(home + PATH + line + "_decoded" + ".txt");

                    System.out.println("Encoded text is in " + encodedTextPath);
                    System.out.println("Decoded text will be in " + decodedTextPath);

                    System.out.println("encryptedSessionKey"+cmd_key.getParam());
                    String decryptedSessionKey = gm.decrypt(cmd_key.getParam());
                    System.out.println("decryptedSessionKey"+decryptedSessionKey);
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
        //rsa = new RSA();
        gm = new GM();

        // Send RSA public key to the server
        ConnUtill.sendMsg(out, Command.CommandType.UPDATE_RSA,
                gm.getPublicKey());
    }
}