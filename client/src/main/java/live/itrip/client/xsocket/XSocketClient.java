package live.itrip.client.xsocket;

import live.itrip.client.bean.Message;
import live.itrip.client.util.Logger;
import org.json.JSONObject;
import org.xsocket.connection.*;

import java.io.IOException;
import java.nio.BufferOverflowException;

/**
 * Created by Feng on 2017/6/14.
 */
public class XSocketClient {
    private INonBlockingConnection nbc;
    private ClientHandler clientHandler;
    private String ip = "127.0.0.1";
    private int port = 7878;

    public XSocketClient(String ip, int port, ICommandCallback commandCallback) {

        this.clientHandler = new ClientHandler(commandCallback);
        this.ip = ip;
        this.port = port;
        init();
    }

    private boolean init() {
        if (this.nbc == null || !this.nbc.isOpen()) {
            try {
                //采用非阻塞式的连接
                nbc = new NonBlockingConnection(this.ip, this.port, this.clientHandler);
                nbc.setFlushmode(IConnection.FlushMode.ASYNC);
                // 设置编码格式
                nbc.setEncoding("UTF-8");
                // 设置是否自动清空缓存
                nbc.setAutoflush(true);
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isConnected() {
        return nbc != null && nbc.isOpen();
    }

    /**
     * 发送执行命令 NonBlockingConnection
     *
     * @param command
     * @throws IOException
     * @throws BufferOverflowException
     */
    public void sendCommandByNBC(CommandInfo command) throws BufferOverflowException, IOException {

        if (this.init()) {
            this.nbc.write(command.toJsonString());
            this.nbc.flush();
        }
    }

    /**
     * 发送执行命令 BlockingConnection
     *
     * @throws IOException
     */
    public CommandExecResult sendCommandByBC(CommandInfo command) throws IOException {
        Logger.debug("send command bc:" + command.toJsonString());
        Message msg = new Message();
        IBlockingConnection bc;
        init();
        bc = new BlockingConnection(this.nbc);
        // 设置编码格式
        bc.setEncoding("UTF-8");
        // 设置是否自动清空缓存
        bc.setAutoflush(true);

        // 向服务端写数据信息
        bc.write(command.toJsonString());
        // System.err.println(command.toJsonString());

        // 向客户端读取数据的信息
        byte[] byteBuffers = bc.readBytesByDelimiter(CommandInfo.DelimiterString, "UTF-8");
        // 打印服务器端信息
        String json = new String(byteBuffers);

        Logger.debug("get command back BC:" + json);

        JSONObject object = new JSONObject(json);
        CommandExecResult result = new CommandExecResult();
        result.setCode(object.getInt("code"));
        result.setData(object.getString("data"));

        // 将信息清除缓存，写入服务器端
        bc.flush();
        bc.close();

        return result;
    }
}
