package live.itrip.agent.callback;

import android.os.Build;
import android.util.Log;
import android.view.IWindowManager;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/7.
 */
public class DeviceInfoRequestCallback implements HttpServerRequestCallback {
    private IWindowManager iWindowManager;

    public DeviceInfoRequestCallback(IWindowManager wm) {
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
            JSONObject object = new JSONObject();

            /*
            Build.BOARD (主板)
            Build.BOOTLOADER（boos 版本）
            Build.BRAND（android系统定制商）
            Build.TIME (编译时间)
            Build.VERSION.SDK_INT (版本号)
            Build.MODEL (版本)
            Build.SUPPORTED_ABIS (cpu指令集)
            Build.DEVICE (设备参数)
            Build.ID (修订版本列表)
            */

            JSONObject build = new JSONObject();
            build.put("PRODUCT", Build.PRODUCT);
            build.put("CPU_ABI", Build.CPU_ABI);
            build.put("TAGS", Build.TAGS);
            build.put("VERSION_CODES.BASE", Build.VERSION_CODES.BASE);
            build.put("MODEL", Build.MODEL);
            build.put("VERSION.SDK", Build.VERSION.SDK);
            build.put("VERSION.RELEASE", Build.VERSION.RELEASE);
            build.put("DEVICE", Build.DEVICE);
            build.put("DISPLAY", Build.DISPLAY);
            build.put("BRAND", Build.BRAND);
            build.put("BOARD", Build.BOARD);
            build.put("FINGERPRINT", Build.FINGERPRINT);
            build.put("ID", Build.ID);
            build.put("MANUFACTURER", Build.MANUFACTURER);
            build.put("USER", Build.USER);
            build.put("HARDWARE", Build.HARDWARE);

            object.put("android.os.Build", build);


            // System Properties
            JSONObject objProperties = new JSONObject();
            Properties properties = System.getProperties();
            for (Map.Entry entry : properties.entrySet()) {
                objProperties.put(entry.getKey().toString(), entry.getValue());
            }
//            objProperties.put("os.version", System.getProperty("os.version"));
//            objProperties.put("os.name", System.getProperty("os.name"));
//            objProperties.put("os.arch", System.getProperty("os.arch"));
//            objProperties.put("user.home", System.getProperty("user.home"));
//            objProperties.put("user.name", System.getProperty("user.name"));
//            objProperties.put("user.dir", System.getProperty("user.dir"));

            object.put("systemProperties", objProperties);

            // cpuinfo
            object.put("cpuinfo", getCpuInfo());
            // stat
//            object.put("stat", getStatInfo());


            conent = object.toString();
            response.send(conent);
        } catch (Exception e) {
            response.code(500);
            response.send(e.toString());
        }
    }

    private JSONObject getStatInfo() {
        JSONObject object = new JSONObject();
        try {
            Scanner s = new Scanner(new File("/proc/stat"));
            while (s.hasNextLine()) {
                String[] vals = s.nextLine().split(": ");
                if (vals.length > 1) {
                    object.put(vals[0].trim(), vals[1].trim());
                }
            }
        } catch (Exception e) {
            Log.e("getCpuInfoMap", Log.getStackTraceString(e));
        }
        return object;
    }

    private JSONObject getCpuInfo() {
        JSONObject object = new JSONObject();
        try {
            int i = 0;
            Scanner s = new Scanner(new File("/proc/cpuinfo"));
            while (s.hasNextLine()) {
                String[] vals = s.nextLine().split(": ");
                if (vals.length > 1) {
                    if (object.has(vals[0].trim())) {
                        object.put(vals[0].trim() + " " + i, vals[1].trim());
                    } else {
                        object.put(vals[0].trim(), vals[1].trim());
                    }

                }
            }
        } catch (Exception e) {
            Log.e("getCpuInfoMap", Log.getStackTraceString(e));
        }
        return object;
    }

}
