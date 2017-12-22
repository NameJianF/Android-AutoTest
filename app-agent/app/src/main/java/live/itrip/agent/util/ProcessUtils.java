package live.itrip.agent.util;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Debug;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import live.itrip.agent.Dumpsys;
import live.itrip.agent.ExecCommands;

/**
 * Created on 2017/9/18.
 * @author JianF
 */

public class ProcessUtils {

    /**
     * set process name
     *
     * @param text
     */
    public static void setArgV0(String text) {
        try {
            Method setter = android.os.Process.class.getMethod("setArgV0", String.class);
            setter.invoke(android.os.Process.class, text);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据包名获取pid
     *
     * @return pid
     */
    public static int getPidByPackageName(Object activityManager, String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Process p = Runtime.getRuntime().exec("top -m 100 -n 1");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(packageName)) {
                        line = line.trim();
                        String[] splitLine = line.split("\\s+");
                        if (packageName.equals(splitLine[splitLine.length - 1])) {
                            return Integer.parseInt(splitLine[0]);
                        }
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            for (ActivityManager.RunningAppProcessInfo runningProcess : getRunningAppProcessInfo(activityManager)) {
                if ((runningProcess.processName != null) && runningProcess.processName.equals(packageName)) {
                    return runningProcess.pid;
                }
            }
        }
        return 0;
    }

    /**
     * 获取当前应用进程的pid
     */
    public static int getCurrentPid() {
        return android.os.Process.myPid();
    }

    /**
     * 根据包名获取uid
     *
     * @return pid
     */
    public static int getUidByPackageName(Object activityManager, String packageName) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                Process p = Runtime.getRuntime().exec("top -m 100 -n 1");
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                String line = "";
//                while ((line = bufferedReader.readLine()) != null) {
//                    if (line.contains(packageName)) {
//                        line = line.trim();
//                        String[] splitLine = line.split("\\s+");
//                        if (packageName.equals(splitLine[splitLine.length - 1])) {
//                            return Integer.parseInt(splitLine[splitLine.length - 2]);
//                        }
//                    }
//                }
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        } else
        {
            for (ActivityManager.RunningAppProcessInfo runningProcess : getRunningAppProcessInfo(activityManager)) {
                if ((runningProcess.processName != null) && runningProcess.processName.equals(packageName)) {
                    return runningProcess.uid;
                }
            }
        }
        return 0;
    }

    /**
     * 获取当前应用进程的uid
     */
    public static int getCurrentUid() {
        return android.os.Process.myUid();
    }


    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessInfo(Object activityManager) {
        List<ActivityManager.RunningAppProcessInfo> processInfoList = new ArrayList<>();
        try {
            for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
                if (method2.getName().equals("getRunningAppProcesses")) {
                    processInfoList = (List<ActivityManager.RunningAppProcessInfo>) method2.invoke(activityManager);
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return processInfoList;
    }

    public static Debug.MemoryInfo[] getProcessMemoryInfo(Object activityManager, int[] myMempid) {
        Debug.MemoryInfo[] memoryInfos = null;
        try {
            for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
                if (method2.getName().equals("getProcessMemoryInfo")) {
                    memoryInfos = (Debug.MemoryInfo[]) method2.invoke(activityManager, myMempid);
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return memoryInfos;
    }

    public static JSONObject getTopActivityInfo() throws JSONException {
        JSONObject jsonObject = new JSONObject();
//        String command = String.format("dumpsys activity activities | grep mFocusedActivity", Dumpsys.activity);
        String command = String.format("dumpsys %s activities", Dumpsys.activity);

        List<String> list = ExecCommands.execCommands2List(command, "mFocusedActivity");
        if (list.size() > 0) {
            String str = list.get(0).trim();
            String[] strings = str.split(" ");

            if (strings.length >= 4) {
                String tmp = strings[3];
                String[] ts = tmp.split("\\/");
                jsonObject.put("packageName", ts[0]);
                jsonObject.put("topActivityName", ts[0] + ts[1]);
            }
        }
        return jsonObject;
    }


//    public static void main(String[] args){
//        String[] strings = "mFocusedActivity: ActivityRecord{3b70017 u0 live.itrip.app/.ui.activity.MainActivity t3796}".split(" ");
//
//        if (strings.length >= 4) {
//            String tmp = strings[3];
//            String[] ts = tmp.split("\\/");
//            System.err.println(ts[0]);
//            System.err.println(ts[0] + ts[1]);
//
//        }
//    }
}
