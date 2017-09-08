package live.itrip.agent.callback;

import android.app.ActivityManager;
import android.util.Log;
import android.view.IWindowManager;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONObject;

import java.lang.reflect.Method;

import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/7.
 */
public class PerformanceRequestCallback implements HttpServerRequestCallback {
    private IWindowManager iWindowManager;

    public PerformanceRequestCallback(IWindowManager wm) {
        this.iWindowManager = wm;
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.getHeaders().set("Cache-Control", "no-cache");
        Log.i(Main.LOGTAG, "performance success");
        try {
            response.setContentType("application/json;charset=utf-8");
            Object activityManager = Class.forName("android.app.ActivityManagerNative").getDeclaredMethod("getDefault", new Class[0]).invoke(null, new Object[0]);
            String conent = "no datas";
            for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
                if (method2.getName().equals("getMemoryInfo")) {
                    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    activityManager.getClass().getMethod("getMemoryInfo", ActivityManager.MemoryInfo.class).invoke(activityManager, memoryInfo);

                    JSONObject object = new JSONObject();
                    object.put("totalMem", memoryInfo.totalMem);//总内存
                    object.put("availMem", memoryInfo.availMem);//可用内存
                    object.put("lowMemory", memoryInfo.lowMemory);//是否达到最低内存
                    object.put("threshold", memoryInfo.threshold);//临界值，达到这个值，进程就要被杀死
                    conent = object.toString();
                }
            }


            response.send(conent);
        } catch (Exception e) {
            response.code(500);
            response.send(e.toString());
        }
    }
}
