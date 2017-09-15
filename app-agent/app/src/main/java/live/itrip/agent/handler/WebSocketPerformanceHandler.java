package live.itrip.agent.handler;

import android.util.Log;

import com.koushikdutta.async.DataSink;

import org.json.JSONObject;

import live.itrip.agent.Main;
import live.itrip.agent.handler.cpu.CpuSampler;

/**
 * Created by Feng on 2017/9/14.
 */

public class WebSocketPerformanceHandler {


    public static void handleMessage(DataSink webSocket, String message) {
        try {
            JSONObject jSONObject = new JSONObject(message);
            String type = jSONObject.getString("type");
            String packageName = jSONObject.getString("packageName");
//            packageName = "live.itrip.agent";

            JSONObject response = new JSONObject();
            if ("memory".equals(type)) {
                response.put("MemoryInfo", PerformanceHandler.getMemoryInfo(Main.activityManager, packageName));
            } else if ("cpu".equals(type)) {
                int pid = 0;
                StringBuilder stringBuilder = CpuSampler.getInstance().getCpuRateInfo(pid);
                response.put("cpu", stringBuilder.toString());
            } else if ("fps".equals(type)) {
                response.put("fps", PerformanceHandler.getFPSInfos(packageName));
            } else if ("gpu".equals(type)) {

            } else if ("network".equals(type)) {

            }

            WebSocketInputHandler.sendEvent(webSocket, response);
        } catch (Exception e) {
            Log.e(Main.LOGTAG, e.getMessage(), e);
        }

    }


}
