package live.itrip.agent;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.FrameMetrics;
import android.view.Window;
import android.view.WindowAnimationFrameStats;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import live.itrip.agent.dump.surfaceflinger.SurfaceFlingerHelper;
import live.itrip.agent.handler.PerformanceHandler;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;
import live.itrip.agent.util.ProcessUtils;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;

/**
 * Created by Feng on 2017/9/11.
 * <p>
 * 测试代码
 */

public class TestDatas {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void testFrameMetricsObserver(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Window.OnFrameMetricsAvailableListener frameMetricsAvailableListener = new Window.OnFrameMetricsAvailableListener() {
                @Override
                public void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int i) {
                    FrameMetrics frameMetricsCopy = new FrameMetrics(frameMetrics);
                    String msg = String.format("Janky frame detected on %s with total duration: %.2fms\n", window.getClass().getName());
                    float layoutMeasureDurationMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION));
                    float drawDurationMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.DRAW_DURATION));
                    float gpuCommandMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION));

                    msg += String.format("Layout/measure: %.2fms, draw:%.2fms, gpuCommand:%.2fms \n", layoutMeasureDurationMs, drawDurationMs, gpuCommandMs);
                    Log.e("FrameMetrics", msg);
                }
            };
            window.addOnFrameMetricsAvailableListener(frameMetricsAvailableListener, new Handler());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void testFrameMetricsObserver() {

        ActivityFrameMetrics activityFrameMetrics = new ActivityFrameMetrics.Builder().build();
        while (true) {

            try {
                Activity activity = null;

                LogUtils.d("Activity Name : " + activity.getPackageName());
                activityFrameMetrics.startFrameMetrics(activity);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void testSurfaceControl() throws ClassNotFoundException {
        String surfaceClassName;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Class<?> clazzSurface = Class.forName(surfaceClassName);

        try {
            WindowAnimationFrameStats stats = null;
            Method method = clazzSurface.getMethod("getAnimationFrameStats", WindowAnimationFrameStats.class);
            Boolean ret = (Boolean) method.invoke(null, stats);
            LogUtils.d("invoke getAnimationFrameStats : " + ret);
            if (stats != null) {
                LogUtils.d("WindowAnimationFrameStats : " + stats.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    // 获取可视窗口列表
    public static void dumpsysSurfaceFlingerList() {
        // adb shell dumpsys SurfaceFlinger --list
    }

    public static void getFPS() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    String windowName = "SurfaceView";
//                    String windowName = "net.oschina.app/.improve.main.MainActivity";
//                     String windowName = "com.diygame.nguidemo01/com.unity3d.player.UnityPlayerNativeActivity";

                    SurfaceFlingerHelper.clearBuffer(windowName);

                    if (SurfaceFlingerHelper.dumpFrameLatency(windowName)) {
                        double fps = SurfaceFlingerHelper.getFrameRate();
                        LogUtils.e("FPS:" + fps);
                    }


                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


    public static void getCpuInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    LogUtils.d("call >>>>>>>>>>>>>> ");
                    String packageName = "net.oschina.app";
                    try {
                        JSONObject msg = PerformanceHandler.getAppCpuInfo(InternalApi.getActivityManager(), packageName);
                        LogUtils.d("CPU : " + msg);
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        LogUtils.d(e.getMessage());
                    } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public static void getTopActivityName() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Method method2 : InternalApi.getActivityManager().getClass().getDeclaredMethods()) {
            if (method2.getName().equalsIgnoreCase("getTopActivityName")) {

            }
        }
    }


    public static void activityManagerDeclaredMethods() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Method method2 : InternalApi.getActivityManager().getClass().getDeclaredMethods()) {
            LogUtils.d("DeclaredMethod >>: " + method2.getName());
        }
    }


    public static List<ActivityManager.RunningTaskInfo> getRunningTasks(Object activityManager, int maxNum) {
        List<ActivityManager.RunningTaskInfo> list = new ArrayList<>();

        try {
            for (Method method2 : activityManager.getClass().getDeclaredMethods()) {
                if (method2.getName().equals("getRunningTasks")) {
                    list = (List<ActivityManager.RunningTaskInfo>) method2.invoke(activityManager, maxNum);
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return list;
    }


    public static void hookInstrumentation() throws Exception {

        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        Method currentActivityThread = activityThread.getDeclaredMethod("currentActivityThread");
        LogUtils.d("currentActivityThread : " + currentActivityThread.toString());
        currentActivityThread.setAccessible(true);
        //获取主线程对象
        Object activityThreadObject = currentActivityThread.invoke(null);
        LogUtils.d("activityThreadObject : " + activityThreadObject.toString());


        //获取Instrumentation字段
        Field mInstrumentation = activityThread.getDeclaredField("mInstrumentation");
        LogUtils.d("mInstrumentation : " + mInstrumentation.toString());

        mInstrumentation.setAccessible(true);
        Instrumentation instrumentation = (Instrumentation) mInstrumentation.get(activityThreadObject);

        LogUtils.d("instrumentation : " + instrumentation.toString());


//        CustomInstrumentation customInstrumentation=new CustomInstrumentation(instrumentation);
//        //替换掉原来的,就是把系统的instrumentation替换为自己的Instrumentation对象
//        mInstrumentation.set(activityThreadObject,customInstrumentation);
        Log.d("[app]", "Hook Instrumentation成功");

    }

    public static void attachContext() throws Exception {
        // 先获取到当前的ActivityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(InternalApi.getActivityManager());

        if (currentActivityThread == null) {
            LogUtils.d("activityThreadClass is null >>>>>>>>>>>>>> ");

        }

        // 拿到原始的 mInstrumentation字段
        Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        mInstrumentationField.setAccessible(true);
        Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);

        if (mInstrumentation == null) {
            LogUtils.e("mInstrumentation is null ...");

        } else {
            LogUtils.e("mInstrumentation ..." + mInstrumentation.getContext().getPackageName());

        }

//        // 创建代理对象
//        Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);
//
//        // 偷梁换柱
//        mInstrumentationField.set(currentActivityThread, evilInstrumentation);
    }


    private static void fps() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    LogUtils.d("call >>>>>>>>>>>>>> ");
                    String packageName = "net.oschina.app";

                    try {
                        JSONObject jsonObject = ProcessUtils.getTopActivityInfo();
                        LogUtils.d(" TopActivityInfo >>>>>>>>>>>>>> " + jsonObject);


                        if (!jsonObject.isNull("topActivityName")) {
                            String topActivityName = jsonObject.getString("topActivityName");
                            LogUtils.d("topActivityName >>>>>>>>>>>>>> " + topActivityName);


//                            if (SurfaceFlingerHelper.dumpFrameLatency(windowName)) {
//                                LogUtils.d("getFrameRate >>>>>>>>>>>>>> " + SurfaceFlingerHelper.getFrameRate());
//                                LogUtils.d("getVsyncJankiness >>>>>>>>>>>>>> " + SurfaceFlingerHelper.getVsyncJankiness());
//                                LogUtils.d("getRefreshPeriod >>>>>>>>>>>>>> " + SurfaceFlingerHelper.getRefreshPeriod());
//                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        LogUtils.d(e.getMessage());
                    }
                }
            }
        }).start();

        LogUtils.d("fps Thread start >>>>>>>>>>>>>> ");

    }


    public static void writeTombstones() throws IOException {
        String content = "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***\n" +
                "Build fingerprint: 'Android-x86/android_x86/x86:5.1.1/LMY48W/woshijpf04211939:eng/test-keys'\n" +
                "Revision: '0'\n" +
                "ABI: 'x86'\n" +
                "pid: 1019, tid: 1019, name: surfaceflinger  >>> /system/bin/surfaceflinger <<<\n" +
                "signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x4\n" +
                "    eax a6265c06  ebx b7467d88  ecx b7631a22  edx a6265c06\n" +
                "    esi 00000000  edi b6867140\n" +
                "    xcs 00000073  xds 0000007b  xes 0000007b  xfs 00000000  xss 0000007b\n" +
                "    eip b745a639  ebp bfcfc1e8  esp bfcfc150  flags 00010282\n" +
                "\n" +
                "backtrace:\n" +
                "    #00 pc 00006639  /system/lib/libui.so (android::Fence::waitForever(char const*)+41)\n" +
                "    #01 pc 00034b86  /system/lib/libsurfaceflinger.so\n" +
                "    #02 pc 0003229e  /system/lib/libsurfaceflinger.so\n" +
                "    #03 pc 0002cb9c  /system/lib/libgui.so (android::BufferQueue::ProxyConsumerListener::onFrameAvailable(android::BufferItem const&)+652)\n" +
                "    #04 pc 000342f4  /system/lib/libgui.so (android::BufferQueueProducer::queueBuffer(int, android::IGraphicBufferProducer::QueueBufferInput const&, android::IGraphicBufferProducer::QueueBufferOutput*)+2580)\n" +
                "    #05 pc 0004eafb  /system/lib/libgui.so (android::Surface::queueBuffer(ANativeWindowBuffer*, int)+411)\n" +
                "    #06 pc 0004ce06  /system/lib/libgui.so (android::Surface::hook_queueBuffer(ANativeWindow*, ANativeWindowBuffer*, int)+38)\n" +
                "    #07 pc 00014bc6  /system/lib/egl/libGLES_android.so\n" +
                "    #08 pc 00017f73  /system/lib/egl/libGLES_android.so (eglSwapBuffers+163)\n" +
                "    #09 pc 00015fdb  /system/lib/libEGL.so (eglSwapBuffers+203)\n" +
                "    #10 pc 000013ea  /system/lib/hw/hwcomposer.x86.so\n" +
                "    #11 pc 00034730  /system/lib/libsurfaceflinger.so\n" +
                "    #12 pc 000256d4  /system/lib/libsurfaceflinger.so\n" +
                "    #13 pc 00024bf4  /system/lib/libsurfaceflinger.so\n" +
                "    #14 pc 000236fb  /system/lib/libsurfaceflinger.so\n" +
                "    #15 pc 0002338a  /system/lib/libsurfaceflinger.so\n" +
                "    #16 pc 0001e0ff  /system/lib/libsurfaceflinger.so\n" +
                "    #17 pc 0001d9ce  /system/lib/libutils.so (android::Looper::pollInner(int)+926)\n" +
                "    #18 pc 0001db73  /system/lib/libutils.so (android::Looper::pollOnce(int, int*, int*, void**)+67)\n" +
                "    #19 pc 0001e561  /system/lib/libsurfaceflinger.so\n" +
                "    #20 pc 00022ce7  /system/lib/libsurfaceflinger.so (android::SurfaceFlinger::run()+39)\n" +
                "    #21 pc 00000ca3  /system/bin/surfaceflinger\n" +
                "    #22 pc 0001365a  /system/lib/libc.so (__libc_init+106)\n" +
                "    #23 pc 00000da8  /system/bin/surfaceflinger\n" +
                "\n" +
                "stack:\n" +
                "         bfcfc110  00000000  \n" +
                "         bfcfc114  b6839270  \n" +
                "         bfcfc118  00000000  \n" +
                "         bfcfc11c  00000000  \n" +
                "         bfcfc120  b68394e0  \n" +
                "         bfcfc124  00000002  \n" +
                "         bfcfc128  00000002  \n" +
                "         bfcfc12c  b75d8185  /system/lib/libutils.so (android::RefBase::incStrong(void const*) const+53)\n" +
                "         bfcfc130  b6839270  \n" +
                "         bfcfc134  bfcfc1e8  [stack]\n" +
                "         bfcfc138  00000002  \n" +
                "         bfcfc13c  a6265c06  \n" +
                "         bfcfc140  b7467d88  /system/lib/libui.so\n" +
                "         bfcfc144  00000000  \n" +
                "         bfcfc148  b6867140  \n" +
                "         bfcfc14c  b745a639  /system/lib/libui.so (android::Fence::waitForever(char const*)+41)\n" +
                "    #00  bfcfc150  b683af18  \n" +
                "         bfcfc154  bfcfc1e8  [stack]\n" +
                "         bfcfc158  00000000  \n" +
                "         bfcfc15c  00000000  \n" +
                "         bfcfc160  00000000  \n" +
                "         bfcfc164  b683af18  \n" +
                "         bfcfc168  b75ec9c4  /system/lib/libutils.so\n" +
                "         bfcfc16c  b75d8285  /system/lib/libutils.so (android::RefBase::weakref_type::decWeak(void const*)+37)\n" +
                "         bfcfc170  00000000  \n" +
                "         bfcfc174  00000000  \n" +
                "         bfcfc178  00000000  \n" +
                "         bfcfc17c  00000000  \n" +
                "         bfcfc180  b7642968  /system/lib/libsurfaceflinger.so\n" +
                "         bfcfc184  bfcfc1e8  [stack]\n" +
                "         bfcfc188  b6867140  \n" +
                "         bfcfc18c  b7622b87  /system/lib/libsurfaceflinger.so";

//        StringBuilder builder = ExecCommands.execCommands("su");
//        LogUtils.e(builder.toString());

        StringBuffer buffer = ExecCommands.execCommands("chmod 777 /data/tombstones/");
        LogUtils.e(buffer.toString());

        File file1 = new File("/data/tombstones/tombstone_00");
        if (file1.exists()) {
            file1.createNewFile();
        }

        FileOutputStream outStream = new FileOutputStream(file1);
        outStream.write(content.getBytes());
        outStream.close();

        File file2 = new File("/data/tombstones/tombstone_01");
        if (file2.exists()) {
            file2.createNewFile();
        }
        FileOutputStream outStream2 = new FileOutputStream(file2);
        outStream2.write(content.getBytes());
        outStream2.close();
    }
}
