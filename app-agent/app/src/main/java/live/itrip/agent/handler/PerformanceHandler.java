package live.itrip.agent.handler;

import android.app.ActivityManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import live.itrip.agent.ExecCommands;
import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/14.
 */

public class PerformanceHandler {

    public static JSONObject getMemoryInfo(Object activityManager, String packageName) {
        JSONObject objMem = new JSONObject();
        try {
            for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
                if (method2.getName().equals("getMemoryInfo")) {
                    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    activityManager.getClass().getMethod("getMemoryInfo", ActivityManager.MemoryInfo.class).invoke(activityManager, memoryInfo);
                    objMem.put("totalMem", memoryInfo.totalMem);//总内存
                    objMem.put("availMem", memoryInfo.availMem);//可用内存
                    objMem.put("lowMemory", memoryInfo.lowMemory);//是否达到最低内存
                    objMem.put("threshold", memoryInfo.threshold);//临界值，达到这个值，进程就要被杀死
//                object.put("MemoryInfo", objMem);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(Main.LOGTAG, e.getMessage(), e);
        }
        return objMem;
    }

    public static JSONObject getFPSInfos(String packageName) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fps", Main.lastFPSValue);
        return jsonObject;
    }
}
