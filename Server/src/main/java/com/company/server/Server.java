package main.java.com.company.server;

import main.java.com.company.gm.GM;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.rsa.RSA;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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

        private static final String PATH = "/Users/mariaizobova/Desktop/4_курс/КБРС/KBRS_2/Server/src/main/resources/";

        private final Socket clientSocket;

        private String publicKey;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;

        private String getNewSessionKey() {
            Random rand = new Random();
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < rand.nextInt() % 10 + 5; i++)
                str.append((char)('a' + rand.nextInt() % 26));
            return str.toString();
        }

        private void resolveRequest(String textName) throws Exception {
            String InitialTextPath, encodedTextPath;

            System.out.printf("Sent from the client: %s\n", textName);
            InitialTextPath = new String(PATH + textName + ".txt");

            if (!(new File(InitialTextPath).exists())) {
                ConnUtill.sendMsg(out, Command.CommandType.ERROR, "Text with name " + textName + " does not exist");
                return;
            }

            encodedTextPath = new String(PATH + textName + "_encoded" + ".txt");

            String sessionKey = getNewSessionKey();

            FileCipher task = new FileCipher(InitialTextPath,
                    encodedTextPath, sessionKey, true, OperationMode.Mode.CFB);

            task.cryptFile();

            ConnUtill.sendMsg(out, Command.CommandType.REQUEST_TEXT, encodedTextPath);

            //RSA rsa = new RSA(publicKey);
            GM gm  = new GM(publicKey);

            String encryptedSessionKey = gm.encrypt(sessionKey);

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