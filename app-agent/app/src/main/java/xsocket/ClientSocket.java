package xsocket;

import org.xsocket.connection.INonBlockingConnection;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Created by Feng on 2017/6/14.
 */
public class ClientSocket extends Vector {

    public ClientSocket() {

    }

    void add(INonBlockingConnection sock) {
        super.add(sock);
    }

    void remove(INonBlockingConnection sock) {
        super.remove(sock);
    }


    public synchronized void sendImageToAll(byte[] imgDatas) {

        INonBlockingConnection sock;

        for (int i = 0; i < size(); i++) {
            sock = (INonBlockingConnection) elementAt(i);
            try {
                sock.write(imgDatas, 0, imgDatas.length);
                sock.write(CommandInfo.DelimiterString);
                sock.flush();
                System.out.println("send image bytes " + sock.getRemoteAddress() + ":" + sock.getRemotePort() + "|" + imgDatas.length);
                //  writer=new PrintWriter(sock.getOutputStream(),true);
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public synchronized void sendStringToAll(String message) {

        INonBlockingConnection sock;

        for (int i = 0; i < size(); i++) {
            sock = (INonBlockingConnection) elementAt(i);
            try {
                sock.setEncoding("UTF-8");
                sock.write(message);
                sock.flush();
                System.out.println("send message " + sock.getRemoteAddress() + ":" + sock.getRemotePort() + "|" + message);
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }


//    public synchronized void sendToOne(INonBlockingConnection socket, String msg) {
//        INonBlockingConnection sock;
//        for (int i = 0; i < size(); i++) {
//            sock = (INonBlockingConnection) elementAt(i);
//            if (socket == sock) {
//
//                try {
//                    sock.write(msg);
//                    sock.flush();
//
//                } catch (Exception ie) {
//                }
//            }
//        }
//    }
}
