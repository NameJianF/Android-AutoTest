package xsocket;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;

import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IConnectionTimeoutHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.IIdleTimeoutHandler;
import org.xsocket.connection.INonBlockingConnection;

/**
 * Created by Feng on 2017/6/14.
 */

public class ServerHandler implements IDataHandler, IConnectHandler,
        IIdleTimeoutHandler, IConnectionTimeoutHandler, IDisconnectHandler {

    private final String TAG = "SOCKET_SERVER";
    private ICommandCallback commandCallback;
    private static ClientSocket clientList = new ClientSocket();


    ServerHandler() {
    }

    /**
     * 即如果失去连接应当如何处理？
     * 需要实现 IDisconnectHandler  这个接口
     * 连接断开时的操作
     */
    @Override
    public boolean onDisconnect(INonBlockingConnection nbc) throws IOException {
        Log.d(TAG, "------ socket onDisconnect -----------");
        clientList.remove(nbc);
        return false;
    }

    /**
     * 即当建立完连接之后可以进行的一些相关操作处理。包括修改连接属性、准备资源、等！
     * 连接的成功时的操作
     */
    @Override
    public boolean onConnect(INonBlockingConnection nbc) throws IOException,
            BufferUnderflowException {
        String remoteName = nbc.getRemoteAddress().getHostName();
        Log.d(TAG, "remoteName " + remoteName + " has connected ！");

        if (!clientList.contains(nbc)) {
            clientList.add(nbc);
        }

        return true;
    }

    /**
     * 即这个方法不光是说当接收到一个新的网络包的时候会调用而且如果有新的缓存存在的时候也会被调用。而且 The onData will also be
     * called, if the connection is closed当连接被关闭的时候也会被调用的!
     */
    @Override
    public boolean onData(INonBlockingConnection nbc) throws IOException,
            BufferUnderflowException {
        String data = nbc.readStringByDelimiter(CommandInfo.DelimiterString);
        System.err.println("socket server get data:" + data);


//        CommandExecResult result = new CommandExecResult();
//        if (data != null && !"".equals(data)) {
//            // 有数据
//            CommandInfo info = null;
//            try {
//                info = new CommandInfo();
//                JSONObject jsonObject = new JSONObject(data);
//                if (!jsonObject.isNull("type")) {
//                    info.setType(jsonObject.getString("type"));
//                }
//                if (!jsonObject.isNull("command")) {
//                    info.setCommand(jsonObject.getString("command"));
//                }
//                result = this.commandCallback.execCommand(info);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        String str = "";
//        try {
//            str = result.toJsonString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        nbc.write(str);
//        nbc.flush();
        return true;
    }

    /**
     * 请求处理超时的处理事件
     */
    @Override
    public boolean onIdleTimeout(INonBlockingConnection connection)
            throws IOException {
        return false;
    }

    /**
     * 连接超时处理事件
     */
    @Override
    public boolean onConnectionTimeout(INonBlockingConnection connection)
            throws IOException {
        return false;
    }

    /**
     *
     */
    public void sendImage(byte[] imgDatas) {
        clientList.sendImageToAll(imgDatas);
    }

    /**
     * @param message
     */
    public void sendMessage(String message) throws UnsupportedEncodingException {
        clientList.sendStringToAll(message);
    }

}