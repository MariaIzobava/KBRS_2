package main.java.com.company.server;

import main.java.com.company.async_encryptions.GM;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Server {
    public static void main(String[] args) {

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
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;

        private String getNewSessionKey() {
            Random rand = new Random();
            StringBuilder str = new StringBuilder();
            while (str.length()<5) {
                str.append((char)('a' + rand.nextInt() % 26));
            }
            return str.toString();
        }

        private void resolveRequest(String textName) throws Exception {
            String InitialTextPath, encodedTextPath;

            String home = new java.io.File( "." ).getCanonicalPath();

            System.out.printf("Sent from the client: %s\n", textName);
            InitialTextPath = new String(home+PATH + textName + ".txt");

            if (!(new File(InitialTextPath).exists())) {
                System.out.println("path:"+InitialTextPath);
                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Text with name " + textName + " does not exist");
                return;
            }

            encodedTextPath = new String(home + PATH + textName + "_encoded" + ".txt");

            String sessionKey = getNewSessionKey();

            FileCipher task = new FileCipher(InitialTextPath,
                    encodedTextPath, sessionKey, true, OperationMode.Mode.CFB);

            task.cryptFile();

            byte[] contents = Files.readAllBytes(Paths.get(encodedTextPath));
            ConnUtill.sendMsg(out, Command.CommandType.REQUEST_TEXT, contents);

            //RSA rsa = new RSA(publicKey);
            GM gm  = new GM(publicKey);

            System.out.println("sessionKey="+sessionKey);
            String encryptedSessionKey = gm.encrypt(sessionKey);
            System.out.println("encryptedSessionKey="+encryptedSessionKey);

            ConnUtill.sendMsg(out, Command.CommandType.REQUEST_TEXT, encryptedSessionKey);

        }


        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    Command cmd = (Command)in.readObject();

                    switch (cmd.getCommandType()) {

                        case UPDATE_RSA:
                            publicKey = cmd.getParam();
                            System.out.println("Public Key successfully updated");
                            break;

                        case REQUEST_TEXT:
                            if (publicKey.equals(""))
                                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Set up Public Key first");
                            resolveRequest(cmd.getParam());
                            break;

                        case ERROR:
                            System.out.println("Server received ERROR message from Client: " + cmd.getParam());
                            break;

                            default: break;

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null)
                        in.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}