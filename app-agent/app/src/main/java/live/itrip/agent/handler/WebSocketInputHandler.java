package live.itrip.agent.handler;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.BuildConfig;
import android.support.v4.view.InputDeviceCompat;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import live.itrip.agent.AdbRotationHelper;
import live.itrip.agent.Main;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;

/**
 * Created by Feng on 2017/9/14.
 */

public class WebSocketInputHandler {


    public static WebSocket.StringCallback createWebSocketHandler(
            Method injectInputEventMethod,
            IWindowManager wm,
            InputManager im,
            KeyCharacterMap kcm,
            IPowerManager pm) {

        final InputManager inputManager = im;
        final Method method = injectInputEventMethod;
        final IPowerManager iPowerManager = pm;
        final IWindowManager iWindowManager = wm;
        final KeyCharacterMap keyCharacterMap = kcm;

        return new WebSocket.StringCallback() {
            boolean authenticated = true;
            long downTime;
            boolean isDown;

            public void onStringAvailable(String s) {
                try {

                    Log.i(Main.LOGTAG, "get message: " + s);

                    JSONObject jSONObject = new JSONObject(s);
                    String type = jSONObject.getString("type");
                    float clientX = (float) jSONObject.optDouble("clientX");
                    float clientY = (float) jSONObject.optDouble("clientY");

                    if ("wakeup".equals(type)) {
                        WebSocketInputHandler.turnScreenOn(inputManager, method, iPowerManager);
                    } else if (!this.authenticated) {
                        Log.e(Main.LOGTAG, "Command not allowed, not authenticated.");
                    } else if ("mousemove".equals(type)) {
                        if (this.isDown) {
                            WebSocketInputHandler.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 2, this.downTime, this.downTime + jSONObject.optLong("downDelta", SystemClock.uptimeMillis() - this.downTime), clientX, clientY, 1.0f);
                        }
                    } else if ("mouseup".equals(type)) {
                        if (this.isDown) {
                            this.isDown = false;
                            WebSocketInputHandler.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 1, this.downTime, this.downTime + jSONObject.optLong("downDelta", SystemClock.uptimeMillis() - this.downTime), clientX, clientY, 1.0f);
                        }
                    } else if ("mousedown".equals(type)) {
                        if (!this.isDown) {
                            this.isDown = true;
                            this.downTime = SystemClock.uptimeMillis();
                            WebSocketInputHandler.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 0, this.downTime, this.downTime, clientX, clientY, 1.0f);
                        }
                    } else if ("rotate".equals(type)) {
                        if (iWindowManager.getRotation() == Surface.ROTATION_0) {
                            AdbRotationHelper.forceRotation(Surface.ROTATION_90);
                        } else {
                            AdbRotationHelper.forceRotation(Surface.ROTATION_0);
                        }
                    } else if ("scroll".equals(type)) {
                        long when = SystemClock.uptimeMillis();
                        float x = clientX;
                        float y = clientY;
                        MotionEvent.PointerProperties[] pp = new MotionEvent.PointerProperties[]{new MotionEvent.PointerProperties()};
                        pp[0].clear();
                        pp[0].id = 0;
                        MotionEvent.PointerCoords[] pc = new MotionEvent.PointerCoords[]{new MotionEvent.PointerCoords()};
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
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_HOME, false);
                    } else if ("delete".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_FORWARD_DEL, false);
                    } else if ("backspace".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DEL, false);
                    } else if ("up".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_UP, false);
                    } else if ("down".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_DOWN, false);
                    } else if ("left".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_LEFT, false);
                    } else if ("right".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_RIGHT, false);
                    } else if ("back".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_BACK, false);
                    } else if ("menu".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_MENU, false);
                    } else if ("keycode".equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, jSONObject.getInt("keycode"), jSONObject.optBoolean("shift", false));
                    } else if ("keychar".equals(type)) {
                        Log.i(Main.LOGTAG, s);
                        if (Main.isImeRunning && Main.broadcastIntent != null) {
                            Intent intent = new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, "com.koushikdutta.vysor.CharCodeReceiver"));
                            intent.putExtra("keychar", jSONObject.getString("keychar"));
                            try {
                                BroadcastHandler.sendBroadcast(intent);
                                return;
                            } catch (Throwable e) {
                                Log.e(Main.LOGTAG, "Error invoking broadcast", e);
                                Main.broadcastIntent = null;
                            }
                        }
                        switch (jSONObject.getString("keychar").charAt(0)) {
                            case '\r':
                                WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_ENTER, false);
                                return;
                            default:
                                for (KeyEvent e2 : keyCharacterMap.getEvents(jSONObject.getString("keychar").toCharArray())) {
                                    WebSocketInputHandler.injectKeyEvent(inputManager, method, e2);
                                }
                                return;
                        }
                    } else if ("bitrate".equals(type)) {
                        int bitrate = jSONObject.optInt("bitrate", Main.currentDevice.getBitrate());
                        if (Main.currentDevice != null && Build.VERSION.SDK_INT >= 19) {
                            Main.currentDevice.setBitrate(bitrate);
                        }
                    } else if (!"sync-frame".equals(type)) {
                        Log.e(Main.LOGTAG, "Unknown: " + s);
                    } else if (Main.currentDevice != null && Build.VERSION.SDK_INT >= 19) {
                        Main.currentDevice.requestSyncFrame();
                    }
                } catch (Throwable e3) {
                    Log.e(Main.LOGTAG, "input websocket", e3);
                }
            }
        };
    }

    public static void turnScreenOn(InputManager im, Method injectInputEventMethod, IPowerManager pm) throws RemoteException, InvocationTargetException, IllegalAccessException {
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

    public static void sendEvent(DataSink webSocket, JSONObject event) {
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
        webSocket.write(list);
    }

    public static void sendEncodeSize(DataSink webSocket) {
        Point encodeSize = getEncodeSize();
        try {
            JSONObject json = new JSONObject();
            json.put("type", "encodeSize");
            json.put("encodeWidth", encodeSize.x);
            json.put("encodeHeight", encodeSize.y);
            sendEvent(webSocket, json);
        } catch (Exception e2) {
            throw new AssertionError(e2);
        }
    }

    public static Point getEncodeSize() {
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

    private static void sendKeyEvent(InputManager im, Method injectInputEventMethod, int inputSource, int keyCode, boolean shift) throws InvocationTargetException, IllegalAccessException {
        long now = SystemClock.uptimeMillis();
        int meta = shift ? 1 : 0;
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 0, keyCode, 0, meta, -1, 0, 0, inputSource));
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 1, keyCode, 0, meta, -1, 0, 0, inputSource));
    }

    private static void injectKeyEvent(InputManager im, Method injectInputEventMethod, KeyEvent event) throws InvocationTargetException, IllegalAccessException {
        injectInputEventMethod.invoke(im, new Object[]{event, 0});
    }

    private static void injectMotionEvent(InputManager im, Method injectInputEventMethod, int inputSource, int action, long downTime, long eventTime, float x, float y, float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, new Object[]{event, 0});
    }
}
