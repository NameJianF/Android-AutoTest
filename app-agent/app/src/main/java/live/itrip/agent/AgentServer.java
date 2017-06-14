package live.itrip.agent;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import xsocket.CommandInfo;
import xsocket.XSocketServer;

/**
 * Created by Feng on 2017/6/13.
 */

public class AgentServer {
    private static final int PORT = 7878;


    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        XSocketServer socketServer = new XSocketServer();
        socketServer.start(PORT);
        int times = 1;
        while (times < 100) {
//            socketServer.sendMessage("aaaaaaaaaaaaaaaaaaaaa" + CommandInfo.DelimiterString);

            Bitmap bitmap = (Bitmap) Class.forName("android.view.SurfaceControl").getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(1080), Integer.valueOf(1920)});
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
            bout.flush();
            socketServer.sendImage(bout.toByteArray());
            Thread.sleep(10 * 1000);
            times++;
        }
    }
}
