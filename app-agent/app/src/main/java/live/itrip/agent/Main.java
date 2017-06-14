package live.itrip.agent;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.BuildConfig;
import android.support.v4.view.InputDeviceCompat;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IRotationWatcher.Stub;
import android.view.IWindowManager;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

//import com.koushikdutta.async.AsyncServer;
//import com.koushikdutta.async.AsyncServerSocket;
//import com.koushikdutta.async.AsyncSocket;
//import com.koushikdutta.async.BufferedDataSink;
//import com.koushikdutta.async.ByteBufferList;
//import com.koushikdutta.async.DataSink;
//import com.koushikdutta.async.LineEmitter;
//import com.koushikdutta.async.callback.CompletedCallback;
//import com.koushikdutta.async.callback.DataCallback.NullDataCallback;
//import com.koushikdutta.async.callback.ListenCallback;
//import com.koushikdutta.async.http.WebSocket;
//import com.koushikdutta.async.http.WebSocket.StringCallback;
//import com.koushikdutta.async.http.server.AsyncHttpServer;
//import com.koushikdutta.async.http.server.AsyncHttpServer.WebSocketRequestCallback;
//import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
//import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
//import com.koushikdutta.async.http.server.HttpServerRequestCallback;
//import com.koushikdutta.async.util.Charsets;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import vysor.AdbRotationHelper;
import vysor.EncoderFeeder;
import vysor.StdOutDevice;
import vysor.virtualdisplay.SurfaceControlVirtualDisplayFactory;

/**
 * Created by Feng on 2017/6/12.
 */
public class Main {
    /*
    private static final String LOGTAG = "ITRIP-AGENT-MAIN";
    static Object activityManager;
    static Method broadcastIntent;
    private static String commandLinePassword;
    static StdOutDevice current;
    static boolean isImeRunning;
    static Looper looper;
    static DataSink webSocket;

    private static void injectMotionEvent(InputManager im, Method injectInputEventMethod, int inputSource, int action, long downTime, long eventTime, float x, float y, float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, new Object[]{event, 0});
    }

    private static void injectKeyEvent(InputManager im, Method injectInputEventMethod, KeyEvent event) throws InvocationTargetException, IllegalAccessException {
        injectInputEventMethod.invoke(im, new Object[]{event, 0});
    }

    private static boolean hasNavBar() {
        return KeyCharacterMap.deviceHasKey(4) && KeyCharacterMap.deviceHasKey(3);
    }

    private static void sendKeyEvent(InputManager im, Method injectInputEventMethod, int inputSource, int keyCode, boolean shift) throws InvocationTargetException, IllegalAccessException {
        long now = SystemClock.uptimeMillis();
        int meta = shift ? 1 : 0;
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 0, keyCode, 0, meta, -1, 0, 0, inputSource));
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 1, keyCode, 0, meta, -1, 0, 0, inputSource));
    }

    static void sendEvent(JSONObject event) {
        if (webSocket instanceof WebSocket) {
            ((WebSocket) webSocket).send(event.toString());
            return;
        }
        ByteBufferList list = new ByteBufferList();
        byte[] bytes = (event.toString() + "\n").getBytes();
        ByteBuffer b = ByteBuffer.allocate(bytes.length);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(bytes);
        b.flip();
        list.add(b);
        ((BufferedDataSink) webSocket).write(list);
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

    static void sendEncodeSize() {
        Point encodeSize = getEncodeSize();
        try {
            JSONObject e = new JSONObject();
            e.put("type", "encodeSize");
            e.put("encodeWidth", encodeSize.x);
            e.put("encodeHeight", encodeSize.y);
            sendEvent(e);
        } catch (Exception e2) {
            throw new AssertionError(e2);
        }
    }

    static StdOutDevice writer(BufferedDataSink sink, IWindowManager wm) {
        if (current != null) {
            current.stop();
            current = null;
        }
        Point encodeSize = getEncodeSize();
        SurfaceControlVirtualDisplayFactory factory = new SurfaceControlVirtualDisplayFactory();
        StdOutDevice device = new StdOutDevice(encodeSize.x, encodeSize.y, sink);
        if (VERSION.SDK_INT < 19) {
            device.useSurface(false);
        }
        current = device;
        Log.i(LOGTAG, "registering virtual display");
        if (device.supportsSurface()) {
            device.registerVirtualDisplay(null, factory, 0);
        } else {
            Log.i(LOGTAG, "Using legacy path");
            device.createDisplaySurface();
            final EncoderFeeder feeder = new EncoderFeeder(device.getMediaCodec(), device.getEncodingDimensions().x, device.getEncodingDimensions().y, device.getColorFormat());
            try {
                wm.watchRotation(new Stub() {
                    public void onRotationChanged(int rotation) throws RemoteException {
                        feeder.setRotation(rotation);
                    }
                });
            } catch (RemoteException e) {
            }
            feeder.feed();
        }
        Log.i(LOGTAG, "virtual display registered");
        return device;
    }

    private static void turnScreenOn(InputManager im, Method injectInputEventMethod, IPowerManager pm) throws RemoteException, InvocationTargetException, IllegalAccessException {
        try {
            if (!pm.isScreenOn()) {
                sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, 26, false);
            }
        } catch (NoSuchMethodError e) {
            try {
                if (!pm.isInteractive()) {
                    sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, 26, false);
                }
            } catch (NoSuchMethodError e2) {
            }
        }
    }


    static StringCallback createWebSocketHandler(Method injectInputEventMethod, IWindowManager wm, InputManager im, KeyCharacterMap kcm, IPowerManager pm) {
        final InputManager inputManager = im;
        final Method method = injectInputEventMethod;
        final IPowerManager iPowerManager = pm;
        final IWindowManager iWindowManager = wm;
        final KeyCharacterMap keyCharacterMap = kcm;
        return new StringCallback() {
//            boolean authenticated;
            long downTime;
            boolean isDown;

            public void onStringAvailable(String s) {
                try {
                    JSONObject jSONObject = new JSONObject(s);
                    String type = jSONObject.getString("type");
                    float clientX = (float) jSONObject.optDouble("clientX");
                    float clientY = (float) jSONObject.optDouble("clientY");
//                    if ("password".equals(type)) {
//                        this.authenticated = Main.checkPassword(jSONObject.getString("password"));
//                        Log.i(Main.LOGTAG, "WebSocket authenticated: " + this.authenticated);
//                    }
                    if ("wakeup".equals(type)) {
                        Main.turnScreenOn(inputManager, method, iPowerManager);
                    }
//                    else if (!this.authenticated) {
//                        Log.e(Main.LOGTAG, "Command not allowed, not authenticated.");
//                    }
                    else if ("mousemove".equals(type)) {
                        if (this.isDown) {
                            Main.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 2, this.downTime, this.downTime + jSONObject.optLong("downDelta", SystemClock.uptimeMillis() - this.downTime), clientX, clientY, 1.0f);
                        }
                    } else if ("mouseup".equals(type)) {
                        if (this.isDown) {
                            this.isDown = false;
                            Main.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 1, this.downTime, this.downTime + jSONObject.optLong("downDelta", SystemClock.uptimeMillis() - this.downTime), clientX, clientY, 1.0f);
                        }
                    } else if ("mousedown".equals(type)) {
                        if (!this.isDown) {
                            this.isDown = true;
                            this.downTime = SystemClock.uptimeMillis();
                            Main.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 0, this.downTime, this.downTime, clientX, clientY, 1.0f);
                        }
                    } else if ("rotate".equals(type)) {
                        if (iWindowManager.getRotation() == 0) {
                            AdbRotationHelper.forceRotation(1);
                        } else {
                            AdbRotationHelper.forceRotation(0);
                        }
                    } else if ("scroll".equals(type)) {
                        long when = SystemClock.uptimeMillis();
                        float x = clientX;
                        float y = clientY;
                        PointerProperties[] pp = new PointerProperties[]{new PointerProperties()};
                        pp[0].clear();
                        pp[0].id = 0;
                        PointerCoords[] pc = new PointerCoords[]{new PointerCoords()};
                        pc[0].clear();
                        pc[0].x = x;
                        pc[0].y = y;
                        pc[0].pressure = 1.0f;
                        pc[0].size = 1.0f;
                        pc[0].setAxisValue(10, (float) jSONObject.optDouble("deltaX"));
                        pc[0].setAxisValue(9, (float) jSONObject.optDouble("deltaY"));
                        MotionEvent event = MotionEvent.obtain(when, when, 8, 1, pp, pc, 0, 0, 1.0f, 1.0f, 0, 0, InputDeviceCompat.SOURCE_TOUCHSCREEN, 0);
                        method.invoke(inputManager, new Object[]{event, Integer.valueOf(0)});
                    } else if ("home".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 3, false);
                    } else if ("delete".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 112, false);
                    } else if ("backspace".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 67, false);
                    } else if ("up".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 19, false);
                    } else if ("down".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 20, false);
                    } else if ("left".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 21, false);
                    } else if ("right".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 22, false);
                    } else if ("back".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 4, false);
                    } else if ("menu".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 82, false);
                    } else if ("keycode".equals(type)) {
                        Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, jSONObject.getInt("keycode"), jSONObject.optBoolean("shift", false));
                    } else if ("keychar".equals(type)) {
                        Log.i(Main.LOGTAG, s);
                        if (Main.isImeRunning && Main.broadcastIntent != null) {
                            Intent intent = new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, "com.koushikdutta.vysor.CharCodeReceiver"));
                            intent.putExtra("keychar", jSONObject.getString("keychar"));
                            try {
                                Main.sendBroadcast(intent);
                                return;
                            } catch (Throwable e) {
                                Log.e(Main.LOGTAG, "Error invoking broadcast", e);
                                Main.broadcastIntent = null;
                            }
                        }
                        switch (jSONObject.getString("keychar").charAt(0)) {
                            case '\r':
                                Main.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, 66, false);
                                return;
                            default:
                                for (KeyEvent e2 : keyCharacterMap.getEvents(jSONObject.getString("keychar").toCharArray())) {
                                    Main.injectKeyEvent(inputManager, method, e2);
                                }
                                return;
                        }
                    } else if ("bitrate".equals(type)) {
                        int bitrate = jSONObject.optInt("bitrate", Main.current.getBitrate());
                        if (Main.current != null && VERSION.SDK_INT >= 19) {
                            Main.current.setBitrate(bitrate);
                        }
                    } else if (!"sync-frame".equals(type)) {
                        Log.e(Main.LOGTAG, "Unknown: " + s);
                    } else if (Main.current != null && VERSION.SDK_INT >= 19) {
                        Main.current.requestSyncFrame();
                    }
                } catch (Throwable e3) {
                    Log.e(Main.LOGTAG, "input websocket", e3);
                }
            }
        };
    }

    static void sendBroadcast(Intent intent) throws Exception {
        if (broadcastIntent.getParameterTypes().length == 11) {
            broadcastIntent.invoke(activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        } else if (broadcastIntent.getParameterTypes().length == 12) {
            broadcastIntent.invoke(activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        } else if (broadcastIntent.getParameterTypes().length == 13) {
            broadcastIntent.invoke(activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        }
    }

    public static void main(String[] args) throws Exception {
        final IRotationWatcher watcher;
        final CompletedCallback webSocketClosedCallback;
        AsyncServerSocket rawSocket;
        final Method method;
        final IWindowManager iWindowManager;
        final InputManager inputManager;
        final KeyCharacterMap keyCharacterMap;
        final IPowerManager iPowerManager;
        final CompletedCallback completedCallback;
        final IRotationWatcher iRotationWatcher;
        Hashtable<String, String> argDict = new Hashtable();
        for (String split : args) {
            String[] splits = split.split("=", 2);
            String value = "";
            if (splits.length == 2) {
                value = splits[1];
            }
            argDict.put(splits[0], value);
        }
        commandLinePassword = (String) argDict.get("password");
        if (commandLinePassword != null) {
            Log.i(LOGTAG, "Received command line password: " + commandLinePassword);
        }
        boolean keyboard = "true".equals(argDict.get("keyboard"));
        Looper.prepare();
        looper = Looper.myLooper();
        AsyncServer server = new AsyncServer();
        AsyncHttpServer httpServer = new AsyncHttpServer() {
            protected boolean onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Log.i(Main.LOGTAG, request.getHeaders().toString());
                return super.onRequest(request, response);
            }
        };
        final InputManager im = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
        final Method injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});
        final KeyCharacterMap kcm = KeyCharacterMap.load(-1);
        Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
        IClipboard clipboard = IClipboard.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"clipboard"}));
        final IClipboard iClipboard = clipboard;
        IOnPrimaryClipChangedListener anonymousClass4 = new IOnPrimaryClipChangedListener.Stub() {
            public void dispatchPrimaryClipChanged() throws RemoteException {
                if (Main.webSocket != null) {
                    try {
                        ClipData data = iClipboard.getPrimaryClip("com.android.shell");
                        JSONObject json = new JSONObject();
                        json.put("type", "clip");
                        json.put("clip", data.getItemAt(0).getText());
                        Main.sendEvent(json);
                    } catch (Exception e) {
                        Log.e(Main.LOGTAG, "Clip error", e);
                    }
                }
            }
        };
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(anonymousClass4, null);
        }
        final IPowerManager pm = IPowerManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"power"}));
        final IWindowManager wm = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
        activityManager = Class.forName("android.app.ActivityManagerNative").getDeclaredMethod("getDefault", new Class[0]).invoke(null, new Object[0]);
        for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
            if (method2.getName().equals("broadcastIntent")) {
                broadcastIntent = method2;
                if (broadcastIntent.getParameterTypes().length == 13 || broadcastIntent.getParameterTypes().length == 11 || broadcastIntent.getParameterTypes().length == 12) {
                    Log.i(LOGTAG, "Found IME hooks.");
                } else {
                    Log.i(LOGTAG, "Found invalid IME hooks.");
                    broadcastIntent = null;
                }
                if (broadcastIntent == null) {
                    Log.i(LOGTAG, "No IME hooks.");
                }
                watcher = new Stub() {
                    public void onRotationChanged(int rotation) throws RemoteException {
                        if (Main.webSocket != null) {
                            Point displaySize = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
                            JSONObject json = new JSONObject();
                            try {
                                json.put("type", "displaySize");
                                json.put("screenWidth", displaySize.x);
                                json.put("screenHeight", displaySize.y);
                                json.put("nav", Main.hasNavBar());
                                Main.sendEvent(json);
                            } catch (JSONException e) {
                            }
                            Main.sendEncodeSize();
                        }
                    }
                };
                wm.watchRotation(watcher);
                httpServer.get("/screenshot.jpg", new HttpServerRequestCallback() {
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                        response.getHeaders().set("Cache-Control", "no-cache");
                            Log.i(Main.LOGTAG, "screenshot authentication success");
                            try {
                                Bitmap bitmap = EncoderFeeder.screenshot(wm);
                                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                bitmap.compress(CompressFormat.JPEG, 100, bout);
                                bout.flush();
                                response.send("image/jpeg", bout.toByteArray());
                                return;
                            } catch (Exception e) {
                                response.code(500);
                                response.send(e.toString());
                                return;
                            }
                    }
                });
                webSocketClosedCallback = new CompletedCallback() {
                    public void onCompleted(Exception ex) {
                        Log.i(Main.LOGTAG, "Websocket closed...");
                        Main.looper.quit();
                    }
                };
                httpServer.websocket("/input", "mirror-protocol", new WebSocketRequestCallback() {
                    public void onConnected(WebSocket ws, AsyncHttpServerRequest request) {
                        if (Main.webSocket != null) {
                            Main.webSocket.setClosedCallback(null);
                        }
                        Main.webSocket = ws;
                        Main.webSocket.setClosedCallback(webSocketClosedCallback);
                        ws.setStringCallback(Main.createWebSocketHandler(injectInputEventMethod, wm, im, kcm, pm));
                        try {
                            watcher.onRotationChanged(0);
                        } catch (RemoteException e) {
                            throw new AssertionError(e);
                        }
                    }
                });
                httpServer.websocket("/ime", "ime-protocol", new WebSocketRequestCallback() {
                    public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                        if (Main.broadcastIntent == null) {
                            webSocket.close();
                            return;
                        }
                        webSocket.setStringCallback(new StringCallback() {
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
                httpServer.get("/h264", new HttpServerRequestCallback() {
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                        Log.i(Main.LOGTAG, "h264 authentication success");
                        try {
                            Main.turnScreenOn(im, injectInputEventMethod, pm);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        response.getHeaders().set("Access-Control-Allow-Origin", "*");
                        response.getHeaders().set("Connection", "close");
                        final StdOutDevice device = Main.writer(new BufferedDataSink(response), wm);
                        response.setClosedCallback(new CompletedCallback() {
                            public void onCompleted(Exception ex) {
                                Log.i(Main.LOGTAG, "Connection terminated.");
                                if (ex != null) {
                                    Log.e(Main.LOGTAG, "Error", ex);
                                }
                                device.stop();
                            }
                        });
                    }
                });
                Log.i(LOGTAG, "Server starting");
                rawSocket = server.listen(null, 53517, new ListenCallback() {
                    StdOutDevice device;

                    void onAuthenticated(AsyncSocket socket) {
                        Log.i(Main.LOGTAG, "h264 authentication succeeded");
                        socket.setDataCallback(new NullDataCallback());
                        this.device = Main.writer(new BufferedDataSink(socket), wm);
                    }

                    public void onAccepted(final AsyncSocket socket) {
                        Log.i(Main.LOGTAG, "New raw socket accepted.");
                        socket.setClosedCallback(new CompletedCallback() {
                            public void onCompleted(Exception ex) {
                                Log.i(Main.LOGTAG, "Connection terminated.");
//                                if (AnonymousClass11.this.device != null) {
//                                    AnonymousClass11.this.device.stop();
//                                }
                            }
                        });
                        LineEmitter emitter = new LineEmitter(Charsets.UTF_8);
                        emitter.setLineCallback(new LineEmitter.StringCallback() {
                            public void onStringAvailable(String password) {
                                Log.i(Main.LOGTAG, "Got password " + password);
//                                if (Main.checkPassword(password)) {
//                                    AnonymousClass11.this.onAuthenticated(socket);
//                                } else {
//                                    Log.i(Main.LOGTAG, "h264 authentication failed");
//                                }
                            }
                        });
                        socket.setDataCallback(emitter);
                    }

                    public void onListening(AsyncServerSocket socket) {
                    }

                    public void onCompleted(Exception ex) {
                    }
                });
                method = injectInputEventMethod;
                iWindowManager = wm;
                inputManager = im;
                keyCharacterMap = kcm;
                iPowerManager = pm;
                completedCallback = webSocketClosedCallback;
                iRotationWatcher = watcher;
                server.listen(null, 53518, new ListenCallback() {
                    public void onAccepted(AsyncSocket socket) {
                        final StringCallback webSocketEventHandler = Main.createWebSocketHandler(method, iWindowManager, inputManager, keyCharacterMap, iPowerManager);
                        if (Main.webSocket != null) {
                            Main.webSocket.setClosedCallback(null);
                        }
                        Main.webSocket = new BufferedDataSink(socket);
                        socket.setClosedCallback(completedCallback);
                        LineEmitter emitter = new LineEmitter(Charsets.UTF_8);
                        emitter.setLineCallback(new LineEmitter.StringCallback() {
                            public void onStringAvailable(String s) {
                                webSocketEventHandler.onStringAvailable(s);
                            }
                        });
                        socket.setDataCallback(emitter);
                        try {
                            iRotationWatcher.onRotationChanged(0);
                        } catch (RemoteException e) {
                            throw new AssertionError(e);
                        }
                    }

                    public void onListening(AsyncServerSocket socket) {
                    }

                    public void onCompleted(Exception ex) {
                    }
                });

                if (httpServer.listen(server, 53516) == null || rawSocket == null) {
                    System.out.println("No server socket?");
                    Log.e(LOGTAG, "No server socket?");
                    throw new AssertionError("No server socket?");
                }

                System.out.println("Started");
                if (broadcastIntent != null) {
                    final boolean z = keyboard;
                    new Thread() {
                        public void run() {
                            try {
                                Runtime.getRuntime().exec(new String[]{"/system/bin/ime", "enable", "com.koushikdutta.vysor/.VysorIME"}).waitFor();
                                if (z) {
                                    Runtime.getRuntime().exec(new String[]{"/system/bin/ime", "set", "com.koushikdutta.vysor/.VysorIME"}).waitFor();
                                }
                                Intent intent = new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, "com.koushikdutta.vysor.CharCodeReceiver"));
                                intent.putExtra("connect", true);
                                Main.sendBroadcast(intent);
                            } catch (Exception e) {
                                Log.e(Main.LOGTAG, "Error setting IME", e);
                            }
                        }
                    }.start();
                }
                Log.i(LOGTAG, "Waiting for exit");
                Looper.loop();
                Log.i(LOGTAG, "Looper done");
                server.stop();
                if (current != null) {
                    current.stop();
                    current = null;
                }
                Log.i(LOGTAG, "Done!");
                System.exit(0);
                return;
            }
        }
        if (broadcastIntent == null) {
            Log.i(LOGTAG, "No IME hooks.");
        }

    }
    */
}
