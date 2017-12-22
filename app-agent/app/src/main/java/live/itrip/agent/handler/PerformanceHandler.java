package live.itrip.agent.handler;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import live.itrip.agent.common.AppType;
import live.itrip.agent.dump.gfx.GfxInfoReader;
import live.itrip.agent.dump.surfaceflinger.SurfaceFlingerHelper;
import live.itrip.agent.handler.cpu.CpuSampler;
import live.itrip.agent.handler.memory.MemorySampler;
import live.itrip.agent.handler.network.NetWorkSampler;
import live.itrip.agent.util.ProcessUtils;

/**
 * Created by Feng on 2017/9/14.
 *
 * @author JianF
 */

public class PerformanceHandler {
    private static final String KEY_TOTAL_FRAMES = "totalFrames";
    private static final String KEY_JANKY_FRAMES = "jankyFrames";
    private static final String KEY_FPS = "fps";

    /**
     * 设备内存信息
     *
     * @param activityManager ActivityManager
     * @return JSONObject
     */
    static JSONObject getDeviceMemoryInfo(Object activityManager) {
        return MemorySampler.getDeviceMemoryInfo(activityManager);
    }

    /**
     * 获取App内存占用 kb
     *
     * @param activityManager ActivityManager
     * @param packageName     Package Name
     * @return JSONObject
     */
    static JSONObject getAppMemoryInfo(Object activityManager, String packageName) throws JSONException {
        return MemorySampler.getAppMemoryInfo(activityManager, packageName);
    }

    /**
     * get cpu info
     *
     * @param activityManager ActivityManager
     * @param packageName     Package Name
     * @return JSONObject
     */
    public static JSONObject getAppCpuInfo(Object activityManager, String packageName) {
        int pid = 0;
        if (packageName != null && !packageName.equalsIgnoreCase("")) {
            pid = ProcessUtils.getPidByPackageName(activityManager, packageName);
        }

        return CpuSampler.getInstance().getCpuRateInfo(pid);
    }

    /**
     * 获取 app fps
     *
     * @param packageName Package Name
     * @return JSONObject
     * @throws JSONException
     * @throws IOException
     * @throws ParseException
     */
    static JSONObject getFPSInfos(String packageName, int appType) throws JSONException, IOException, ParseException {
        JSONObject jsonObject = new JSONObject();

        if (AppType.APPLICATION == appType) {
            // 应用： dump gfxinfo 方式
            GfxInfoReader.GfxInfoResult result = new GfxInfoReader(packageName).read();
            jsonObject.put(KEY_TOTAL_FRAMES, result.getFrameCount());
            jsonObject.put(KEY_JANKY_FRAMES, result.getJankyCount());
            jsonObject.put(KEY_FPS, result.getFps());
        } else if (AppType.GAME == appType) {
            // 游戏：更加android版本区分
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // version >=24, android 7.0
                jsonObject.put(KEY_TOTAL_FRAMES, 0);
                jsonObject.put(KEY_JANKY_FRAMES, 0);
                jsonObject.put(KEY_FPS, 0);
            } else {
                // version <= 23
                // 1. get topactivity
                String windowName = "SurfaceView";
                // 2. dumpFrameLatency
                if (SurfaceFlingerHelper.dumpFrameLatency(windowName)) {
                    // 3. Calculate
                    // get jankiness count
                    int jankinessCount = SurfaceFlingerHelper.getVsyncJankiness();
                    // get frame rate:   NaN or -1.0 or 59.95896528333939
                    double frameRate = SurfaceFlingerHelper.getFrameRate();
                    // get max accumulated frames
                    int maxDeltaVsync = SurfaceFlingerHelper.getMaxDeltaVsync();

                    jsonObject.put(KEY_TOTAL_FRAMES, maxDeltaVsync);
                    jsonObject.put(KEY_JANKY_FRAMES, jankinessCount);
                    if (frameRate > 0) {
                        jsonObject.put(KEY_FPS, frameRate);
                    } else {
                        jsonObject.put(KEY_FPS, 0);
                    }
                }

                // 4. clear SurfaceFlinger buffer
                SurfaceFlingerHelper.clearBuffer(windowName);
            }
        }
        return jsonObject;
    }

    /**
     * 获取网络流量信息
     *
     * @param packageName Package Name
     * @return JSONObject
     * @throws JSONException
     */
    static JSONObject getAppNetFlow(Object activityManager, String packageName) throws JSONException {
        return NetWorkSampler.getAppNetFlow(activityManager, packageName);
    }
}
