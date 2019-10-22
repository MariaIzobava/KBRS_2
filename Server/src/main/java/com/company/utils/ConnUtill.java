package main.java.com.company.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ConnUtill {

    public static void sendMsg(ObjectOutputStream out, Command.CommandType type, String msg) throws IOException {
        out.writeObject(new Command(type, msg));
        out.flush();
    }

    public static void sendMsg(ObjectOutputStream out, Command.CommandType type, byte[] msg) throws IOException {
        out.writeObject(new Command(type, msg));
        out.flush();
    }

}
