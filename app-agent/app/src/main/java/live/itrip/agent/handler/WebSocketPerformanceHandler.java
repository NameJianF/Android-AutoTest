package live.itrip.agent.handler;

import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.DataSink;

import org.json.JSONObject;

import live.itrip.agent.Main;
import live.itrip.agent.common.AppType;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;

/**
 * Created by Feng on 2017/9/14.
 *
 * @author JianF
 */

public class WebSocketPerformanceHandler {
    private static final String KEY_ALL = "all";
    private static final String KEY_MEMORY = "memory";
    private static final String KEY_DEVICE_MEMORY = "DeviceMemoryInfo";
    private static final String KEY_APP_MEMORY = "AppMemoryInfo";
    private static final String KEY_CPU = "cpu";
    private static final String KEY_FPS = "fps";
    private static final String KEY_NETWORK = "network";
    private static final String KEY_TYPE = "type";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_APP_TYPE = "appType";

    public static void handleMessage(DataSink webSocket, String message) {
        try {
            LogUtils.d(message);
            JSONObject jSONObject = new JSONObject(message);
            String type = jSONObject.getString(KEY_TYPE);
            int appType = AppType.APPLICATION;
            String packageName = null;
            if (jSONObject.has(KEY_PACKAGE_NAME)) {
                packageName = jSONObject.getString(KEY_PACKAGE_NAME);
            }
            if (jSONObject.has(KEY_APP_TYPE)) {
                appType = jSONObject.getInt(KEY_APP_TYPE);
            }

            Object activityManager = InternalApi.getActivityManager();
            JSONObject response = new JSONObject();

            if (KEY_ALL.equals(type)) {
                response.put(KEY_DEVICE_MEMORY, PerformanceHandler.getDeviceMemoryInfo(activityManager));
                if (!TextUtils.isEmpty(packageName)) {
                    response.put(KEY_APP_MEMORY, PerformanceHandler.getAppMemoryInfo(activityManager, packageName));
                }
                JSONObject cpu = PerformanceHandler.getAppCpuInfo(activityManager, packageName);
                response.put(KEY_CPU, cpu);
                response.put(KEY_FPS, PerformanceHandler.getFPSInfos(packageName, appType));
                response.put(KEY_NETWORK, PerformanceHandler.getAppNetFlow(activityManager, packageName));
            } else {
                if (KEY_MEMORY.equals(type)) {
                    response.put(KEY_DEVICE_MEMORY, PerformanceHandler.getDeviceMemoryInfo(activityManager));
                    if (!TextUtils.isEmpty(packageName)) {
                        response.put(KEY_APP_MEMORY, PerformanceHandler.getAppMemoryInfo(activityManager, packageName));
                    }
                } else if (KEY_CPU.equals(type)) {
                    JSONObject cpu = PerformanceHandler.getAppCpuInfo(activityManager, packageName);
                    LogUtils.d("cpu datas :" + cpu.toString());
                    response.put(KEY_CPU, cpu);
                } else if (KEY_FPS.equals(type)) {
                    response.put(KEY_FPS, PerformanceHandler.getFPSInfos(packageName, appType));
                } else if (KEY_NETWORK.equals(type)) {
                    response.put(KEY_NETWORK, PerformanceHandler.getAppNetFlow(activityManager, packageName));
                }
            }
            LogUtils.d("Performance data >>> " + response.toString());

            WebSocketInputHandler.sendEvent(webSocket, response);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
}
