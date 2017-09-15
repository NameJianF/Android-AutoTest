package live.itrip.agent.callback;

import android.app.ActivityManager;
import android.util.Log;
import android.view.IWindowManager;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import live.itrip.agent.ExecCommands;
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
            JSONObject object = new JSONObject();

            for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
                if (method2.getName().equals("getMemoryInfo")) {
                    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    activityManager.getClass().getMethod("getMemoryInfo", ActivityManager.MemoryInfo.class).invoke(activityManager, memoryInfo);
                    JSONObject objMem = new JSONObject();
                    objMem.put("totalMem", memoryInfo.totalMem);//总内存
                    objMem.put("availMem", memoryInfo.availMem);//可用内存
                    objMem.put("lowMemory", memoryInfo.lowMemory);//是否达到最低内存
                    objMem.put("threshold", memoryInfo.threshold);//临界值，达到这个值，进程就要被杀死
                    object.put("MemoryInfo", objMem);
                } else if (method2.getName().equals("getRunningAppProcesses")) {
                    List<ActivityManager.RunningAppProcessInfo> processInfoList = new ArrayList<>();
                    processInfoList = (List<ActivityManager.RunningAppProcessInfo>) activityManager.getClass().getMethod("getRunningAppProcesses").invoke(activityManager);

                    JSONArray array = new JSONArray();
                    for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
                        JSONObject obj = new JSONObject();
                        obj.put("pid", processInfo.pid);
                        obj.put("importance", processInfo.importance);

                        JSONArray pkglist = new JSONArray();
                        for (int i = 0; i < processInfo.pkgList.length; i++) {
                            JSONObject pkg = new JSONObject();
                            pkg.put("pkg", processInfo.pkgList[i]);
                            pkglist.put(pkg);
                        }
                        obj.put("pkgList", pkglist);

                        obj.put("processName", processInfo.processName);
                        obj.put("uid", processInfo.uid);
                        array.put(obj);
                    }
                    object.put("RunningAppProcesses", array);
                }
            }


            // bugreport
//            object.put("dumpsysNetstats", ExecCommands.execCommands("dumpsys netstats").toString());

            // tombstones
//            JSONArray arrayTombstones = getTombstones();
//
//            if (arrayTombstones != null && arrayTombstones.length() > 0) {
//                object.put("tombstones", arrayTombstones);
//            } else {
//                object.put("tombstones", "no datas");
//            }

            // dumpsys  netstats
//            object.put("dumpsysNetstats", ExecCommands.execCommands("dumpsys netstats").toString());

            // dumpsys meminfo
//            object.put("dumpsysMeminfo", ExecCommands.execCommands("dumpsys meminfo").toString());

            // dumpsys cpuinfo
//            object.put("dumpsysCpuinfo", ExecCommands.execCommands("dumpsys cpuinfo").toString());

            // dumpsys gfxinfo
//            object.put("dumpsysGfxinfo", ExecCommands.execCommands("dumpsys gfxinfo").toString());

            // dumpsys display
//            object.put("dumpsysDisplay", ExecCommands.execCommands("dumpsys display").toString());


            // dumpsys diskstats
//            object.put("dumpsysDiskstats", ExecCommands.execCommands("dumpsys diskstats").toString());

            // dumpsys  battery
//            object.put("dumpsysBattery", ExecCommands.execCommands("dumpsys battery").toString());

            // dumpsys  batteryinfo
//            object.put("dumpsysBatteryinfo", ExecCommands.execCommands("dumpsys batteryinfo").toString());

            // dumpsys  usagestats
//            object.put("dumpsysUsagestats", ExecCommands.execCommands("dumpsys usagestats").toString());

            // dumpsys activity
//            object.put("dumpsysActivity", ExecCommands.execCommands("dumpsys activity").toString());

            // dump notification
//            object.put("dumpsysNotification", ExecCommands.execCommands("dumpsys notification").toString());

            conent = object.toString();
            response.send(conent);
        } catch (Exception e) {
            response.code(500);
            response.send(e.toString());
        }
    }


    private JSONArray getTombstones() {
//        StringBuffer buffer = ExecCommands.execCommands("chmod 777 /data/tombstones/");
//        Log.e(Main.LOGTAG, buffer.toString());

        JSONArray array = new JSONArray();
        try {
            File dir = new File("/data/tombstones/");
            if (dir.exists() && dir.listFiles() != null && dir.listFiles().length > 0) {
                Log.e(Main.LOGTAG, String.valueOf(dir.listFiles().length));
                for (File f : dir.listFiles()) {
                    JSONObject object = new JSONObject();
                    StringBuilder content = new StringBuilder();
                    String separator = System.getProperty("line.separator");
                    Scanner s = new Scanner(f);
                    while (s.hasNextLine()) {
                        content.append(s.nextLine());
                        content.append(separator);
                    }
                    object.put(f.getName(), content.toString());
                    array.put(object);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }

}
