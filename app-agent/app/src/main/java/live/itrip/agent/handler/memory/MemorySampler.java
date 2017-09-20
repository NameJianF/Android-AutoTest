package live.itrip.agent.handler.memory;

import android.app.ActivityManager;
import android.os.Debug;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;

import live.itrip.agent.Main;
import live.itrip.agent.util.ProcessUtils;

/**
 * Created by Feng on 2017/9/20.
 */

public class MemorySampler {

    private MemorySampler() {
    }


    /**
     * 设备内存信息
     *
     * @param activityManager
     * @return
     */
    public static JSONObject getDeviceMemoryInfo(Object activityManager) {
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

    /**
     * 获取App内存占用  kb
     *
     * @param activityManager
     * @param packageName
     * @return
     */
    public static JSONObject getAppMemoryInfo(Object activityManager, String packageName) throws JSONException {
        JSONObject objMem = new JSONObject();

        List<ActivityManager.RunningAppProcessInfo> appProcessList = ProcessUtils.getRunningAppProcessInfo(activityManager);

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
            // 进程ID号
            int pid = appProcessInfo.pid;
            // 用户ID 类似于Linux的权限不同，ID也就不同 比如 root等
            int uid = appProcessInfo.uid;
            // 进程名，默认是包名或者由属性android：process=""指定
            String processName = appProcessInfo.processName;
            if (packageName.equals(processName)) {
                // 获得该进程占用的内存
                int[] myMempid = new int[]{pid};
                // 此MemoryInfo位于android.os.Debug.MemoryInfo包中，用来统计进程的内存信息
                Debug.MemoryInfo[] memoryInfos = ProcessUtils.getProcessMemoryInfo(activityManager, myMempid);// activityManager.getProcessMemoryInfo(myMempid);
                if (memoryInfos != null) {
                    // 获取进程占内存用信息 kb单位
                    for (int i = 0; i < memoryInfos.length; i++) {
                        Debug.MemoryInfo memoryInfo = memoryInfos[i];
                        objMem.put("dalvikPrivateDirty[" + i + "]", memoryInfo.dalvikPrivateDirty);
                    }

                    Log.i(Main.LOGTAG, "processName: " + processName + "  pid: " + pid
                            + " uid:" + uid + " memorySize is -->" + memoryInfos[0].dalvikPrivateDirty + "kb");
                    break;
                }


                // 获得每个进程里运行的应用程序(包),即每个应用程序的包名
//                String[] packageList = appProcessInfo.pkgList;
//                Log.i(Main.LOGTAG, "process id is " + pid + "has " + packageList.length);
//                for (String pkg : packageList) {
//                    Log.i(Main.LOGTAG, "packageName " + pkg + " in process id is -->" + pid);
//                }
            }
        }
        return objMem;
    }
}
