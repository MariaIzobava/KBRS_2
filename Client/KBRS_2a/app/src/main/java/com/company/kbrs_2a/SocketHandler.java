package com.company.kbrs_2a;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketHandler {
    private static Socket socket;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;
    private static String sessionKey;

    public static synchronized Socket getSocket() {
        return socket;
    }
    public static synchronized ObjectOutputStream getOutputStream() {
        return out;
    }
    public static synchronized ObjectInputStream getInputStream() {
        return in;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }

    public static void setIn(ObjectInputStream in) {
        SocketHandler.in = in;
    }

    public static void setOut(ObjectOutputStream out) {
        SocketHandler.out = out;
    }


    public static String getSessionKey() {
        return sessionKey;
    }

    public static void setSessionKey(String sessionKey) {
        SocketHandler.sessionKey = sessionKey;
    }
}
