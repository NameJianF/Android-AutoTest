package xsocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Map;

import org.xsocket.connection.IConnection.FlushMode;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;


/**
 * Created by Feng on 2017/6/14.
 */
public class XSocketServer {
    private IServer mServer;
    private ServerHandler serverHandler;

    public XSocketServer() {
    }

    public void start(int port) {

        try {
            System.out.println("Server: on start...");
//            InetAddress address = InetAddress.getLocalHost();

            serverHandler = new ServerHandler();

            //创建一个服务端的对象
            mServer = new Server(port, serverHandler);

            //设置当前的采用的异步模式
            mServer.setFlushmode(FlushMode.ASYNC);

            try {
                mServer.start();
                System.out.println("Server:" + mServer.getLocalAddress() + ":" + port);

//                Map<String, Class> maps = mServer.getOptions();
//                if (maps != null) {
//                    for (Map.Entry<String, Class> entry : maps.entrySet()) {
//                        System.out.println("key= " + entry.getKey() + " value =" + entry.getValue().getName());
//                    }
//                }
//                System.out.println("Log:" + mServer.getStartUpLogMessage());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isOpen() {
        return mServer != null && mServer.isOpen();
    }

    public void stop() {
        if (mServer != null && mServer.isOpen()) {
            try {
                mServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendImage(byte[] imgData) {
        if (this.serverHandler != null && this.isOpen()) {
            this.serverHandler.sendImage(imgData);
        }
    }

    public void sendMessage(String message) throws UnsupportedEncodingException {
        if (this.serverHandler != null && this.isOpen()) {
            this.serverHandler.sendMessage(message);
        }
    }
}
