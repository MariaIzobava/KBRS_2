package main.java.com.company.utils;

import java.io.Serializable;

public class Command  implements Serializable {

    CommandType _commandType;
    String _param;

    public Command(CommandType commandType, String param)
    {
        setCommandType(commandType);
        setParam(param);
    }

    public CommandType getCommandType() { return _commandType; }
    public void setCommandType(CommandType commandType) { _commandType = commandType; }

    public String getParam() { return _param; }
    public void setParam(String param) { _param = param; }

    public enum CommandType {
        UPDATE_RSA,
        REQUEST_TEXT,
        ERROR
    }
}

