package live.itrip.agent;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import java.lang.reflect.InvocationTargetException;

import live.itrip.agent.callback.DeviceInfoRequestCallback;
import live.itrip.agent.callback.ImeSocketRequestCallback;
import live.itrip.agent.callback.InputSocketRequestCallback;
import live.itrip.agent.callback.ScreenLiveRequestCallback;
import live.itrip.agent.callback.ScreenshotRequestCallback;
import live.itrip.agent.callback.SettingsRequestCallback;
import live.itrip.agent.handler.BroadcastHandler;
import live.itrip.agent.handler.WebSocketPerformanceHandler;
import live.itrip.agent.util.LogUtils;
import live.itrip.agent.util.ProcessUtils;

/**
 * Created by Feng on 2017/6/12.
 *
 * @author JianF
 *         <p>
 *         1. Main class 主要提供实时视频流及截图
 *         2. 启动相关服务
 */
public class Main {
    private static final String PROCESS_NAME = "itrip-agent";
    private static final int PORT_53516 = 53516;
    public static Looper looper;
    public static boolean isImeRunning;
    private static StdOutDevice currentDevice;
    public static double resolution = 0.0d;


    public static void main(String[] args) throws Exception {
        LogUtils.e("main started...");
        ProcessUtils.setArgV0(PROCESS_NAME);

        AsyncHttpServer httpServer = new AsyncHttpServer() {
            @Override
            protected boolean onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                return super.onRequest(request, response);
            }
        };

        Looper.prepare();
        looper = Looper.myLooper();

        init();

        AsyncServer server = new AsyncServer();

        // device information
        httpServer.get("/device.json", new DeviceInfoRequestCallback());

        // screenshot
        httpServer.get("/screenshot.jpg", new ScreenshotRequestCallback());

        // screen live /h264
        httpServer.get("/h264", new ScreenLiveRequestCallback(currentDevice));

        /**

         // http get : open/close  device setting
         httpServer.get("/setAccessibilityOpen", new SettingsRequestCallback(SettingsRequestCallback.SettingsType.OPEN_ACCESSIBILITY_SERVICE));
         httpServer.get("/setAccessibilityClose", new SettingsRequestCallback(SettingsRequestCallback.SettingsType.CLOSE_ACCESSIBILITY_SERVICE));
         httpServer.get("/setGfxOpen", new SettingsRequestCallback(SettingsRequestCallback.SettingsType.PROPERTY_PROFILE_TRUE));
         httpServer.get("/setGfxClose", new SettingsRequestCallback(SettingsRequestCallback.SettingsType.PROPERTY_PROFILE_FALSE));


         // web socket
         // socket 长连接获取性能数据
         httpServer.websocket("/infos", "infos-protocol", new AsyncHttpServer.WebSocketRequestCallback() {
        @Override public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
        webSocket.setStringCallback(new WebSocket.StringCallback() {
        @Override public void onStringAvailable(String message) {
        WebSocketPerformanceHandler.handleMessage(webSocket, message);
        }
        });
        webSocket.setClosedCallback(new CompletedCallback() {
        @Override public void onCompleted(Exception ex) {
        LogUtils.i("infos Websocket closed...");
        }
        });
        }
        });
         **/

        // input
        httpServer.websocket("/input", "mirror-protocol", new InputSocketRequestCallback(currentDevice));
        //httpServer.websocket("/ime", "ime-protocol", new ImeSocketRequestCallback());


        httpServer.listen(server, PORT_53516);

        LogUtils.e("main entry started...");
        Looper.loop();
    }

    private static void init() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
    }

    /**
     * 启动 Activity
     * 例如：startActivity("net.oschina.app", "net.oschina.app.LaunchActivity");
     *
     * @param pkg      package name
     * @param activity activity
     */
    public static void startActivity(String pkg, String activity) {
        Intent intent = new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, "live.itrip.agent.receiver.StartActivityReceiver"));
        intent.putExtra("pkg", pkg);
        intent.putExtra("activity", activity);

        try {
//            Log.d(Main.LOGTAG, "broadcastIntent >>>>>>>>>>>>>> " + broadcastIntent.getParameterTypes().length);
            BroadcastHandler.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasNavBar() {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) && KeyCharacterMap.deviceHasKey((KeyEvent.KEYCODE_HOME));
    }

    public static void setCurrentDevice(StdOutDevice currentDevice) {
        Main.currentDevice = currentDevice;
    }
}
