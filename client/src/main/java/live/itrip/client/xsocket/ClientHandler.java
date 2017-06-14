package live.itrip.client.xsocket;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;

import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.INonBlockingConnection;

/**
 * Created by Feng on 2017/6/14.
 */
public class ClientHandler implements IDataHandler, IConnectHandler, IDisconnectHandler {
    private ICommandCallback commandCallback;

    public ClientHandler(ICommandCallback commandCallback) {
        this.commandCallback = commandCallback;
    }

    /**
     * 连接的成功时的操作
     */
    @Override
    public boolean onConnect(INonBlockingConnection nbc)
            throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
//		String remoteName = nbc.getRemoteAddress().getHostName();
//		System.out.println("remoteName " + remoteName + " has connected ！");
        return true;
    }

    /**
     * 连接断开时的操作
     */
    @Override
    public boolean onDisconnect(INonBlockingConnection nbc) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 接收到数据库时候的处理
     */
    @Override
    public boolean onData(INonBlockingConnection nbc)
            throws IOException, BufferUnderflowException, ClosedChannelException, MaxReadSizeExceededException {
//        String message = nbc.readStringByDelimiter(CommandInfo.DelimiterString, "UTF-8");

        byte[] bytes = nbc.readBytesByDelimiter(CommandInfo.DelimiterString);
        nbc.flush();
        // System.out.println("handler ondata:" + data);
        if (this.commandCallback != null) {
//            this.commandCallback.handleStringMessage(message);
            this.commandCallback.handleScreenshot(bytes);
        }
        return true;
    }
}
