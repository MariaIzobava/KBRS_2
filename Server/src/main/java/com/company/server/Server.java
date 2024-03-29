package main.java.com.company.server;

import main.java.com.company.async_encryptions.GM;
import main.java.com.company.async_encryptions.RSA;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Random;

public class Server {

    private static HashMap<String, String> credentialsList = new HashMap<>();


    public static void main(String[] args) {
        credentialsList.put("admin","admin");
        ServerSocket server = null;
        try {
            server = new ServerSocket(32000);
            server.setReuseAddress(true);
            // The main thread is just accepting new connections
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected " + client.getInetAddress().getHostAddress());
                ClientHandler clientSock = new ClientHandler(client);

                // The background thread will handle each client separately
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {

        private static final String PATH = "/main/resources/";

        private final Socket clientSocket;

        private String publicKey;
        private String sessionKey;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;

        private enum AsyncMode {
            NONE,
            RSA,
            GM
        }

        private AsyncMode mode = AsyncMode.NONE;

        private String getNewSessionKey() {
            Random rand = new Random();
            StringBuilder str = new StringBuilder();
            while (str.length() < 16) {
                str.append((char)('a' + rand.nextInt() % 26));
            }
            return str.toString();
        }

        private void resolveRequest(String textName) throws Exception {
            String InitialTextPath, encodedTextPath;

            String home = new java.io.File( "." ).getCanonicalPath();

            System.out.printf("Sent from the client: %s\n", textName);
            InitialTextPath = new String(home + PATH + textName + ".txt");

            if (!(new File(InitialTextPath).exists())) {
                System.out.println("path:"+InitialTextPath);
                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Text with name " + textName + " does not exist");
                return;
            }

            encodedTextPath = new String(home + PATH + textName + "_encoded" + ".txt");

            FileCipher task = new FileCipher(InitialTextPath,
                    encodedTextPath, sessionKey, true, OperationMode.Mode.CFB);

            task.cryptFile();

            byte[] contents = Files.readAllBytes(Paths.get(encodedTextPath));
            ConnUtill.sendMsg(out, Command.CommandType.REQUEST_TEXT, contents);

        }

        private void saveText(String textName, byte[] text) throws Exception {

            String home = new java.io.File( "." ).getCanonicalPath();

            System.out.println("Client wants to save modified text");

            File outputDir = new File(home + PATH);
            File encryptedFile = File.createTempFile("encrypted", "txt", outputDir);
            File decryptedFile = new File(home + PATH + textName + ".txt");

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile));
            bos.write(text);
            bos.flush();
            bos.close();

            FileCipher task = new FileCipher(encryptedFile.getPath(),
                    decryptedFile.getPath(), sessionKey, false, OperationMode.Mode.CFB);

            task.cryptFile();
            System.out.println("File was decrypted and saved as " + decryptedFile.getCanonicalPath());
            ConnUtill.sendMsg(out, Command.CommandType.SAVE_TEXT_PROCEED, "Text was saved successfully");

            encryptedFile.deleteOnExit();
        }


        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                Command prevCmd = null;

                while (true) {
                    Command cmd = (Command)in.readObject();

                    switch (cmd.getCommandType()) {

                        case REQUEST_ACCESS_WITH_GM:
                            mode = AsyncMode.GM;
                            publicKey = cmd.getParam();
                            System.out.println("GM Public Key successfully updated");
                            sessionKey = getNewSessionKey();

                            GM gm = new GM(publicKey);

                            System.out.println("sessionKey=" + sessionKey);
                            String encryptedSessionKey = gm.encrypt(sessionKey);

                            ConnUtill.sendMsg(out, Command.CommandType.REQUEST_ACCESS_WITH_GM, encryptedSessionKey);
                            break;


                        case REQUEST_ACCESS_WITH_RSA:
                            mode = AsyncMode.RSA;
                            publicKey = cmd.getParam();
                            System.out.println("RSA Public Key successfully updated");
                            sessionKey = getNewSessionKey();

                            RSA rsa  = new RSA(publicKey);

                            System.out.println("sessionKey=" + sessionKey);
                            encryptedSessionKey = rsa.encrypt(sessionKey);

                            ConnUtill.sendMsg(out, Command.CommandType.REQUEST_ACCESS_WITH_GM, encryptedSessionKey);
                            break;

                        case REQUEST_TEXT:
                            if (publicKey.equals("")) {
                                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Set up Public Key first");
                                break;
                            }
                            resolveRequest(cmd.getParam());
                            break;

                        case SAVE_TEXT_INIT:
                            if (publicKey.equals("")) {
                                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Set up Public Key first");
                                break;
                            }
                            ConnUtill.sendMsg(out, Command.CommandType.SAVE_TEXT_INIT, "Ready for accepting text");
                            break;

                        case SAVE_TEXT_PROCEED:
                            if (publicKey.equals("")) {
                                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Set up Public Key first");
                                break;
                            }
                            if (prevCmd == null || !prevCmd.getCommandType().equals(Command.CommandType.SAVE_TEXT_INIT)) {
                                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "SAVE TEXT command was not initialized");
                                break;
                            }
                            System.out.println("Received text");
                            saveText(prevCmd.getParam(), cmd.get_byteParam());
                            break;

                        case VERIFY_CREDENTIALS:
                            // login and password should be decrypted
                            byte[] str = cmd.get_byteParam();

                            String[] credentials = decryptCredentials(sessionKey, str).split(" ");

                            String msg = "false";
                            if (credentials.length == 2 &&
                                credentialsList.containsKey(credentials[0]) &&
                                credentialsList.get(credentials[0]).equals(credentials[1])) {
                                msg = "true";
                            }
                            ConnUtill.sendMsg(out, Command.CommandType.VERIFY_CREDENTIALS, msg);
                            break;


                        case ERROR:
                            System.out.println("Server received ERROR message from Client: " + cmd.getParam());
                            break;

                            default: break;

                    }

                    prevCmd = cmd;

                }

            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Connection was teared down.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
                    if (in != null)
                        in.close();
                    clientSocket.close();

                    System.out.println("Client with sessionKey = " + sessionKey + " is disconnected.\n Deleting session.");
                    sessionKey = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static String decryptCredentials(String key, byte[] encrypted)
                throws GeneralSecurityException {

            byte[] raw = key.getBytes(Charset.forName("UTF-8"));
            if (raw.length != 16) {
                throw new IllegalArgumentException("Invalid key size.");
            }
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec,
                    new IvParameterSpec(new byte[16]));
            byte[] original = cipher.doFinal(encrypted);

            return new String(original, Charset.forName("UTF-8"));
        }

    }
}