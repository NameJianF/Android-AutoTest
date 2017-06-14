package xsocket;

/**
 * Created by Feng on 2017/6/14.
 */

public class CommandInfo {
    public static final String DelimiterString = "|";
    private String type = CommandType.normal;
    private String command = "";

    public static class CommandType {
        public static final String normal = "1";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
