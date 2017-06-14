package xsocket;

/**
 * Created by Feng on 2017/6/14.
 */

public interface ICommandCallback {
    CommandExecResult execCommand(CommandInfo command);
}
