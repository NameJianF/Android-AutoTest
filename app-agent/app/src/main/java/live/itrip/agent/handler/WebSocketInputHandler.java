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

import live.itrip.agent.Main;
import live.itrip.agent.StdOutDevice;
import live.itrip.agent.adb.AdbRotationHelper;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;

/**
 * Created on 2017/9/14.
 *
 * @author JianF
 */

public class WebSocketInputHandler {
    private static final String KEY_WAKEUP = "wakeup";
    private static final String KEY_MOUSE_MOVE = "mousemove";
    private static final String KEY_MOUSE_UP = "mouseup";
    private static final String KEY_MOUSE_DOWN = "mousedown";
    private static final String KEY_ROTATE = "rotate";
    private static final String KEY_SCROLL = "scroll";
    private static final String KEY_HOME = "home";
    private static final String KEY_DELETE = "delete";
    private static final String KEY_BACKSPACE = "backspace";
    private static final String KEY_UP = "up";
    private static final String KEY_DOWN = "down";
    private static final String KEY_LEFT = "left";
    private static final String KEY_RIGHT = "right";
    private static final String KEY_BACK = "back";
    private static final String KEY_MENU = "menu";
    private static final String KEY_KEYCODE = "keycode";
    private static final String KEY_KEY_CHAR = "keychar";
    private static final String KEY_BITRATE = "bitrate";
    private static final String KEY_SYNC_FRAME = "sync-frame";

    public static WebSocket.StringCallback createWebSocketHandler(
            Method injectInputEventMethod,
            IWindowManager wm,
            InputManager im,
            KeyCharacterMap kcm,
            IPowerManager pm,
            StdOutDevice device) {

        final InputManager inputManager = im;
        final Method method = injectInputEventMethod;
        final IPowerManager iPowerManager = pm;
        final IWindowManager iWindowManager = wm;
        final KeyCharacterMap keyCharacterMap = kcm;
        final StdOutDevice stdOutDevice = device;

        return new WebSocket.StringCallback() {
            boolean authenticated = true;
            long downTime;
            boolean isDown;

            @Override
            public void onStringAvailable(String s) {
                try {

                    JSONObject jSONObject = new JSONObject(s);
                    String type = jSONObject.getString("type");
                    float clientX = (float) jSONObject.optDouble("clientX");
                    float clientY = (float) jSONObject.optDouble("clientY");

                    if (KEY_WAKEUP.equals(type)) {
                        WebSocketInputHandler.turnScreenOn(inputManager, method, iPowerManager);
                    } else if (!this.authenticated) {
                        LogUtils.e("Command not allowed, not authenticated.");
                    } else if (KEY_MOUSE_MOVE.equals(type)) {
                        if (this.isDown) {
                            WebSocketInputHandler.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 2, this.downTime, this.downTime + jSONObject.optLong("downDelta", SystemClock.uptimeMillis() - this.downTime), clientX, clientY, 1.0f);
                        }
                    } else if (KEY_MOUSE_UP.equals(type)) {
                        if (this.isDown) {
                            this.isDown = false;
                            WebSocketInputHandler.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 1, this.downTime, this.downTime + jSONObject.optLong("downDelta", SystemClock.uptimeMillis() - this.downTime), clientX, clientY, 1.0f);
                        }
                    } else if (KEY_MOUSE_DOWN.equals(type)) {
                        if (!this.isDown) {
                            this.isDown = true;
                            this.downTime = SystemClock.uptimeMillis();
                            WebSocketInputHandler.injectMotionEvent(inputManager, method, InputDeviceCompat.SOURCE_TOUCHSCREEN, 0, this.downTime, this.downTime, clientX, clientY, 1.0f);
                        }
                    } else if (KEY_ROTATE.equals(type)) {
                        if (iWindowManager.getRotation() == Surface.ROTATION_0) {
                            AdbRotationHelper.forceRotation(Surface.ROTATION_90);
                        } else {
                            AdbRotationHelper.forceRotation(Surface.ROTATION_0);
                        }
                    } else if (KEY_SCROLL.equals(type)) {
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
                    } else if (KEY_HOME.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_HOME, false);
                    } else if (KEY_DELETE.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_FORWARD_DEL, false);
                    } else if (KEY_BACKSPACE.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DEL, false);
                    } else if (KEY_UP.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_UP, false);
                    } else if (KEY_DOWN.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_DOWN, false);
                    } else if (KEY_LEFT.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_LEFT, false);
                    } else if (KEY_RIGHT.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_RIGHT, false);
                    } else if (KEY_BACK.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_BACK, false);
                    } else if (KEY_MENU.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_MENU, false);
                    } else if (KEY_KEYCODE.equals(type)) {
                        WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, jSONObject.getInt("keycode"), jSONObject.optBoolean("shift", false));
                    } else if (KEY_KEY_CHAR.equals(type)) {
                        LogUtils.i(s);
                        if (Main.isImeRunning && InternalApi.getBroadcastIntent() != null) {
                            Intent intent = new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, "live.itrip.agent.receiver.CharCodeReceiver"));
                            intent.putExtra(KEY_KEY_CHAR, jSONObject.getString(KEY_KEY_CHAR));
                            try {
                                BroadcastHandler.sendBroadcast(intent);
                                return;
                            } catch (Throwable e) {
                                LogUtils.e("Error invoking broadcast", e);
                                InternalApi.setBroadcastIntent(null);
                            }
                        }
                        switch (jSONObject.getString(KEY_KEY_CHAR).charAt(0)) {
                            case '\r':
                                WebSocketInputHandler.sendKeyEvent(inputManager, method, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_ENTER, false);
                                break;
                            default:
                                for (KeyEvent e2 : keyCharacterMap.getEvents(jSONObject.getString(KEY_KEY_CHAR).toCharArray())) {
                                    WebSocketInputHandler.injectKeyEvent(inputManager, method, e2);
                                }
                                break;
                        }
                    } else if (KEY_BITRATE.equals(type)) {
                        int bitrate = jSONObject.optInt("bitrate", stdOutDevice.getBitrate());
                        if (stdOutDevice != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            stdOutDevice.setBitrate(bitrate);
                        }
                    } else if (!KEY_SYNC_FRAME.equals(type)) {
                        LogUtils.e("Unknown: " + s);
                    } else if (stdOutDevice != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        stdOutDevice.requestSyncFrame();
                    }
                } catch (Throwable e3) {
                    LogUtils.e("input websocket", e3);
                }
            }
        };
    }

    /**
     * 点亮设备屏幕
     *
     * @param im InputManager
     * @param injectInputEventMethod injectInputEventMethod
     * @param pm IPowerManager
     * @throws RemoteException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
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
