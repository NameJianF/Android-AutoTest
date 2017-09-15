package live.itrip.agent;

import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.MotionEvent;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.BufferedDataSink;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import live.itrip.agent.callback.DeviceInfoRequestCallback;
import live.itrip.agent.callback.PerformanceRequestCallback;
import live.itrip.agent.callback.ScreenshotRequestCallback;
import live.itrip.agent.handler.WebSocketInputHandler;
import live.itrip.agent.handler.WebSocketPerformanceHandler;
import live.itrip.agent.handler.fps.Audience;
import live.itrip.agent.handler.fps.Metronome;
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
    private static Method injectInputEventMethod;
    private static DataSink webSocketDataSink;
    private static Metronome metronome; // get fps

    public static Object activityManager;
    public static StdOutDevice currentDevice;
    public static Method broadcastIntent;
    public static boolean isImeRunning;
    public static double lastFPSValue = 0d;

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


        // device information
        httpServer.get("/device.json", new DeviceInfoRequestCallback(windowManager));

        // screenshot
        httpServer.get("/screenshot.jpg", new ScreenshotRequestCallback(windowManager));

        // screen live /h264
        httpServer.get("/h264", new HttpServerRequestCallback() {
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Log.i(Main.LOGTAG, "h264 authentication success");
                try {
                    WebSocketInputHandler.turnScreenOn(inputManager, injectInputEventMethod, powerManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.getHeaders().set("Access-Control-Allow-Origin", "*");
                response.getHeaders().set("Connection", "close");

                Main.writer(new BufferedDataSink(response), windowManager);
                response.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        Log.i(Main.LOGTAG, "Connection terminated.");
                        if (ex != null) {
                            Log.e(Main.LOGTAG, "Error", ex);
                        }
                        if (currentDevice != null) {
                            currentDevice.stop();
                        }
                    }
                });
            }
        });

        // http get  性能数据
        httpServer.get("/performance.json", new PerformanceRequestCallback(windowManager));

        // web socket

        // socket 长连接获取性能数据
        httpServer.websocket("/infos", "infos-protocol", new AsyncHttpServer.WebSocketRequestCallback() {
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String message) {
                        WebSocketPerformanceHandler.handleMessage(webSocket, message);
                    }
                });
                webSocket.setClosedCallback(new CompletedCallback() {
                    public void onCompleted(Exception ex) {
                        Log.i(Main.LOGTAG, "infos Websocket closed...");
                    }
                });
            }
        });


        // input
        final CompletedCallback webSocketClosedCallback = new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                Log.i(Main.LOGTAG, "Websocket closed...");
                Main.looper.quit();
            }
        };
        final KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(-1);
        final IRotationWatcher watcher = new IRotationWatcher.Stub() {
            public void onRotationChanged(int rotation) throws RemoteException {
                if (Main.webSocketDataSink != null) {
                    Point displaySize = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
                    JSONObject json = new JSONObject();
                    try {
                        json.put("type", "displaySize");
                        json.put("screenWidth", displaySize.x);
                        json.put("screenHeight", displaySize.y);
                        json.put("nav", Main.hasNavBar());
                        WebSocketInputHandler.sendEvent(Main.webSocketDataSink, json);
                    } catch (JSONException ignored) {
                    }
                    WebSocketInputHandler.sendEncodeSize(Main.webSocketDataSink);
                }
            }
        };

        httpServer.websocket("/input", "mirror-protocol", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket ws, AsyncHttpServerRequest request) {

                if (Main.webSocketDataSink != null) {
                    Main.webSocketDataSink.setClosedCallback(null);
                }
                Log.i(LOGTAG, "input websocket onConnected...");

                Main.webSocketDataSink = ws;
                Main.webSocketDataSink.setClosedCallback(webSocketClosedCallback);
                ws.setStringCallback(WebSocketInputHandler.createWebSocketHandler(injectInputEventMethod, windowManager, inputManager, keyCharacterMap, powerManager));
                try {
                    watcher.onRotationChanged(0);
                } catch (RemoteException e) {
                    throw new AssertionError(e);
                }
            }
        });

        httpServer.websocket("/ime", "ime-protocol", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                if (Main.broadcastIntent == null) {
                    webSocket.close();
                    return;
                }
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        if ("bind".equals(s)) {
                            Main.isImeRunning = true;
                        } else if ("unbind".equals(s)) {
                            Main.isImeRunning = false;
                        }
                    }
                });
                webSocket.setClosedCallback(new CompletedCallback() {
                    public void onCompleted(Exception ex) {
                        Main.isImeRunning = false;
                    }
                });
            }
        });


        httpServer.listen(server, 53516);

        // for fps
        initForFPS();

        Log.e(LOGTAG, "main entry started...");
        Looper.loop();
    }

    private static void init() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
        windowManager = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
        inputManager = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        powerManager = IPowerManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"power"}));
        activityManager = Class.forName("android.app.ActivityManagerNative").getDeclaredMethod("getDefault", new Class[0]).invoke(null, new Object[0]);

        MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
        injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});

    }

    private static void initForFPS() {
        metronome = new Metronome();
        metronome.setInterval(1000); // 1s
        metronome.addListener(new Audience() {
            @Override
            public void heartbeat(double fps) {
//                Log.d(Main.LOGTAG, "FPS:" + fps);
                lastFPSValue = fps;
            }
        });
        metronome.start();

    }

//    private static void getFPS() {
//        if (metronome != null) {
//            metronome.start();
//        }
//    }
//
//    private static void stopFPS() {
//        if (metronome != null) {
//            metronome.stop();
//        }
//    }


    private static void writer(BufferedDataSink sink, IWindowManager wm) {
        if (currentDevice != null) {
            currentDevice.stop();
            currentDevice = null;
        }
        Point encodeSize = WebSocketInputHandler.getEncodeSize();
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
            } catch (RemoteException ignored) {
            }
            feeder.feed();
        }
        Log.i(LOGTAG, "virtual display registered");
    }

    private static boolean hasNavBar() {
        return KeyCharacterMap.deviceHasKey(4) && KeyCharacterMap.deviceHasKey(3);
    }
}
