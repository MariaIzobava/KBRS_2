package main.java.com.company.utils;

import java.io.Serializable;

public class Command  implements Serializable {

    CommandType _commandType;
    String _param;
    byte[] _byteParam;

    public Command(CommandType commandType, String param)
    {
        setCommandType(commandType);
        setParam(param);
        _byteParam = null;
    }

    public Command(CommandType _commandType, byte[] _byteParam) {
        this._commandType = _commandType;
        this._byteParam = _byteParam;
        this._param = null;
    }

    public CommandType getCommandType() { return _commandType; }
    public void setCommandType(CommandType commandType) { _commandType = commandType; }

    public String getParam() { return _param; }
    public void setParam(String param) { _param = param; }

    public byte[] get_byteParam() {
        return _byteParam;
    }

    public void set_byteParam(byte[] _byteParam) {
        this._byteParam = _byteParam;
    }

    public enum CommandType {
        UPDATE_RSA,
        REQUEST_TEXT,
        ERROR
    }
}

