package live.itrip.agent.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.util.ArrayMap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/18.
 */

public class ProcessUtils {
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
            Log.e(Main.LOGTAG, e.getMessage(), e);
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
            Log.e(Main.LOGTAG, e.getMessage(), e);
        }
        return memoryInfos;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Activity getRunningActivity() {
        try {

            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Didn't find the running activity");
    }

    public static Application getApplicationUsingReflection() throws Exception {
        Application app = (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
        Log.d(Main.LOGTAG, "Application >>>>>>>>>>>>>> " + null);

        return app;
    }

    public static Application getApplicationUsingReflectionByAppGlobals() throws Exception {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }
}
