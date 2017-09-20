package live.itrip.agent.handler;

import android.util.Log;

import com.koushikdutta.async.DataSink;

import org.json.JSONObject;

import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/14.
 */

public class WebSocketPerformanceHandler {


    public static void handleMessage(DataSink webSocket, String message) {
        try {
            JSONObject jSONObject = new JSONObject(message);
            String type = jSONObject.getString("type");
            String packageName = jSONObject.getString("packageName");

            JSONObject response = new JSONObject();
            if ("memory".equals(type)) {
                response.put("DeviceMemoryInfo", PerformanceHandler.getDeviceMemoryInfo(Main.activityManager));
                response.put("AppMemoryInfo", PerformanceHandler.getAppMemoryInfo(Main.activityManager, packageName));
            } else if ("cpu".equals(type)) {
                response.put("cpu", PerformanceHandler.getAppCpuInfo(Main.activityManager, packageName));
            } else if ("fps".equals(type)) {
                response.put("fps", PerformanceHandler.getFPSInfos(packageName));
            } else if ("network".equals(type)) {
                response.put("network", PerformanceHandler.getAppNetFlow(Main.activityManager, packageName));
            }

            WebSocketInputHandler.sendEvent(webSocket, response);
        } catch (Exception e) {
            Log.e(Main.LOGTAG, e.getMessage(), e);
        }

    }


}
