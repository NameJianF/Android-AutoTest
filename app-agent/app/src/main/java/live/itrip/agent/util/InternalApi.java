package live.itrip.agent.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.IDisplayManager;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.IBinder;
import android.os.IPowerManager;
import android.util.Log;
import android.view.IWindowManager;
import android.view.InputEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import live.itrip.agent.Main;

/**
 * Created on 2017/12/4.
 * 获取系统服务、接口
 *
 * @author Feng
 *         Description : 获取系统Service、Method
 *         Update :
 */

public class InternalApi {
    private static final String ANDROID_OS_SERVICE_MANAGER = "android.os.ServiceManager";
    private static final String ANDROID_APP_ACTIVITY_MANAGER_NATIVE = "android.app.ActivityManagerNative";
    private static final String STRING_GET_SERVICE = "getService";
    private static final String STRING_GET_INSTANCE = "getInstance";
    private static final String STRING_GET_DEFAULT = "getDefault";
    private static final String STRING_INJECT_INPUT_EVENT = "injectInputEvent";
    private static final String BROADCAST_INTENT = "broadcastIntent";

    private static IWindowManager windowManager;
    private static InputManager inputManager;
    private static IPowerManager powerManager;
    private static IDisplayManager displayManager;
    private static Object activityManager;
    private static Method injectInputEventMethod;
    private static Method broadcastIntent;


    public static synchronized IWindowManager getWindowManager() throws ClassNotFoundException
            , NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (windowManager == null) {
            Method getServiceMethod = Class.forName(ANDROID_OS_SERVICE_MANAGER).getDeclaredMethod(STRING_GET_SERVICE, new Class[]{String.class});
            windowManager = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{Context.WINDOW_SERVICE}));
        }
        return windowManager;
    }

    public static synchronized InputManager getInputManager() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (inputManager == null) {
            inputManager = (InputManager) InputManager.class.getDeclaredMethod(STRING_GET_INSTANCE, new Class[0]).invoke(null, new Object[0]);
        }
        return inputManager;
    }

    public static synchronized IPowerManager getPowerManager() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (powerManager == null) {
            Method getServiceMethod = Class.forName(ANDROID_OS_SERVICE_MANAGER).getDeclaredMethod(STRING_GET_SERVICE, new Class[]{String.class});
            powerManager = IPowerManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{Context.POWER_SERVICE}));
        }
        return powerManager;
    }

    public static synchronized Object getActivityManager() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (activityManager == null) {
            activityManager = Class.forName(ANDROID_APP_ACTIVITY_MANAGER_NATIVE).getDeclaredMethod(STRING_GET_DEFAULT, new Class[0]).invoke(null, new Object[0]);
        }
        return activityManager;
    }


    public static synchronized Method getInjectInputEventMethod() throws NoSuchMethodException {
        if (injectInputEventMethod == null) {
            injectInputEventMethod = InputManager.class.getMethod(STRING_INJECT_INPUT_EVENT, new Class[]{InputEvent.class, Integer.TYPE});
        }
        return injectInputEventMethod;
    }

    public static synchronized Method getBroadcastIntent() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Method method2 : getActivityManager().getClass().getDeclaredMethods()) {
            if (BROADCAST_INTENT.equals(method2.getName())) {
                broadcastIntent = method2;
                if (broadcastIntent.getParameterTypes().length == 13 || broadcastIntent.getParameterTypes().length == 11 || broadcastIntent.getParameterTypes().length == 12) {
                    LogUtils.i("Found IME hooks.");
                } else {
                    LogUtils.i("Found invalid IME hooks.");
                    broadcastIntent = null;
                }
                if (broadcastIntent == null) {
                    LogUtils.i("No IME hooks.");
                }
                break;
            }
        }
        return broadcastIntent;
    }

    public static synchronized void setBroadcastIntent(Method intent) {
        broadcastIntent = intent;
    }

    public static synchronized IDisplayManager getDisplayManager() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Context.DISPLAY_SERVICE;
        if (displayManager == null) {
//            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Method getServiceMethod = Class.forName(ANDROID_OS_SERVICE_MANAGER).getDeclaredMethod(STRING_GET_SERVICE, new Class[]{String.class});
            displayManager = IDisplayManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{Context.DISPLAY_SERVICE}));
        }
        return displayManager;
    }


    public static synchronized Method getMethodScreenshot() throws ClassNotFoundException, NoSuchMethodException {
        String surfaceClassName;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Class<?> clazzSurface = Class.forName(surfaceClassName);
        return clazzSurface.getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE});
    }

    /**
     * print device services
     *
     * @param name
     * @return
     */
    public static boolean hasService(String name) {
        try {
            // The ServiceManager class is @hidden in newer SDKs
            Class<?> serviceManager = Class.forName(ANDROID_OS_SERVICE_MANAGER);
            Method getService = serviceManager.getMethod("getService", String.class);
            return getService.invoke(null, name) != null;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return false;
        }
    }
}
