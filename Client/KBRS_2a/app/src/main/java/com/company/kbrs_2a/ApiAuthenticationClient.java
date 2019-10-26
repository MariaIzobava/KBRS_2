package com.company.kbrs_2a;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    public String execute()
    {

        // login and password should be encrypted
        String str = login + " " + password;
        try {

            ConnUtill.sendMsg(out, Command.CommandType.VERIFY_CREDENTIALS, str);
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





}
