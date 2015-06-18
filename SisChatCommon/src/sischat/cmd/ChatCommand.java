package sischat.cmd;

import java.io.Serializable;

public class ChatCommand <T extends ChatCommandType> implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    private T type;
    private Object message;

    public ChatCommand(T type, Object message) {
        this.type = type;
        this.message = message;
    }

    public ChatCommandType getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

}

