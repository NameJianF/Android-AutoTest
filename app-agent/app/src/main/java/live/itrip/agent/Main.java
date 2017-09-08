package live.itrip.agent;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.BufferedDataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import live.itrip.agent.callback.PerformanceRequestCallback;
import live.itrip.agent.callback.ScreenshotRequestCallback;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;

/**
 * Created by Feng on 2017/6/12.
 */
public class Main {

    public static final String LOGTAG = "itrip-agent";
    private static Looper looper;
    private static IWindowManager windowManager;
    private static InputManager inputManager;
    private static IPowerManager powerManager;
    private static Object activityManager;
    private static Method injectInputEventMethod;
    private static StdOutDevice currentDevice;

    public static void main(String[] args) throws Exception {
        Log.e(LOGTAG, "main started...");
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

        // screenshot
        httpServer.get("/screenshot.jpg", new ScreenshotRequestCallback(windowManager));

        // screen live
        httpServer.get("/h264", new HttpServerRequestCallback() {
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Log.i(Main.LOGTAG, "h264 authentication success");
                try {
                    Main.turnScreenOn(inputManager, injectInputEventMethod, powerManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.getHeaders().set("Access-Control-Allow-Origin", "*");
                response.getHeaders().set("Connection", "close");

                Main.writer(new BufferedDataSink(response), windowManager);
                response.setClosedCallback(new CompletedCallback() {
                    public void onCompleted(Exception ex) {
                        Log.i(Main.LOGTAG, "Connection terminated.");
                        if (ex != null) {
                            Log.e(Main.LOGTAG, "Error", ex);
                        }
                        currentDevice.stop();
                    }
                });
            }
        });

        // 性能数据
        httpServer.get("/performance.json", new PerformanceRequestCallback(windowManager));

        httpServer.listen(server, 53516);

        Log.e(LOGTAG, "main entry started...");
        Looper.loop();
    }

    private static void init() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
        windowManager = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
        inputManager = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        powerManager = IPowerManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"power"}));

        MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
        injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});
    }

    private static void turnScreenOn(InputManager im, Method injectInputEventMethod, IPowerManager pm) throws RemoteException, InvocationTargetException, IllegalAccessException {
        try {
            if (!pm.isScreenOn()) {
                sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_POWER, false);
            }
        } catch (NoSuchMethodError e) {
            try {
                if (!pm.isInteractive()) {
                    sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_POWER, false);
                }
            } catch (NoSuchMethodError e2) {
            }
        }
    }

    private static void sendKeyEvent(InputManager im, Method injectInputEventMethod, int inputSource, int keyCode, boolean shift) throws InvocationTargetException, IllegalAccessException {
        long now = SystemClock.uptimeMillis();
        int meta = shift ? 1 : 0;
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 0, keyCode, 0, meta, -1, 0, 0, inputSource));
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 1, keyCode, 0, meta, -1, 0, 0, inputSource));
    }

    private static void injectKeyEvent(InputManager im, Method injectInputEventMethod, KeyEvent event) throws InvocationTargetException, IllegalAccessException {
        injectInputEventMethod.invoke(im, new Object[]{event, 0});
    }

    private static void writer(BufferedDataSink sink, IWindowManager wm) {
        if (currentDevice != null) {
            currentDevice.stop();
            currentDevice = null;
        }
        Point encodeSize = getEncodeSize();
        SurfaceControlVirtualDisplayFactory factory = new SurfaceControlVirtualDisplayFactory();
        StdOutDevice currentDevice = new StdOutDevice(encodeSize.x, encodeSize.y, sink);
        if (Build.VERSION.SDK_INT < 19) {
            currentDevice.useSurface(false);
        }
        Log.i(LOGTAG, "registering virtual display");
        if (currentDevice.supportsSurface()) {
            currentDevice.registerVirtualDisplay(factory, 0);
        } else {
            Log.i(LOGTAG, "Using legacy path");
            currentDevice.createDisplaySurface();
            final EncoderFeeder feeder = new EncoderFeeder(currentDevice.getMediaCodec(), currentDevice.getEncodingDimensions().x, currentDevice.getEncodingDimensions().y, currentDevice.getColorFormat());
            try {
                IRotationWatcher watcher = new IRotationWatcher.Stub() {
                    public void onRotationChanged(int rotation) throws RemoteException {
                        feeder.setRotation(rotation);
                    }
                };
                wm.watchRotation(watcher, 0);
            } catch (RemoteException e) {
            }
            feeder.feed();
        }
        Log.i(LOGTAG, "virtual display registered");
    }

    static Point getEncodeSize() {
        Point encodeSize = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
        if (encodeSize.x >= 1280 || encodeSize.y >= 1280) {
            encodeSize.x /= 2;
            encodeSize.y /= 2;
        }
        while (true) {
            if (encodeSize.x <= 1280 && encodeSize.y <= 1280) {
                return encodeSize;
            }
            encodeSize.x /= 2;
            encodeSize.y /= 2;
        }
    }
}
