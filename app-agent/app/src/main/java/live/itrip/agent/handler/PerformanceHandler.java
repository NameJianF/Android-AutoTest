package live.itrip.agent.handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import live.itrip.agent.dump.gfx.GfxInfoReader;
import live.itrip.agent.handler.cpu.CpuSampler;
import live.itrip.agent.handler.memory.MemorySampler;
import live.itrip.agent.handler.network.NetWorkSampler;
import live.itrip.agent.util.ProcessUtils;

/**
 * Created by Feng on 2017/9/14.
 */

public class PerformanceHandler {

    /**
     * 设备内存信息
     *
     * @param activityManager
     * @return
     */
    public static JSONObject getDeviceMemoryInfo(Object activityManager) {
        return MemorySampler.getDeviceMemoryInfo(activityManager);
    }

    /**
     * 获取App内存占用 kb
     *
     * @param activityManager
     * @param packageName
     * @return
     */
    public static JSONObject getAppMemoryInfo(Object activityManager, String packageName) throws JSONException {
        return MemorySampler.getAppMemoryInfo(activityManager, packageName);
    }

    /**
     * get cpu info
     *
     * @param activityManager
     * @param packageName
     * @return
     */
    public static String getAppCpuInfo(Object activityManager, String packageName) {
        int pid = ProcessUtils.getPidByPackageName(activityManager, packageName);
        StringBuilder stringBuilder = CpuSampler.getInstance().getCpuRateInfo(pid);
        return stringBuilder.toString();
    }

    /**
     * 获取 app fps
     *
     * @param packageName
     * @return
     * @throws JSONException
     * @throws IOException
     * @throws ParseException
     */
    public static JSONObject getFPSInfos(String packageName) throws JSONException, IOException, ParseException {

        GfxInfoReader.GfxInfoResult result = new GfxInfoReader("", packageName).read();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalFrames", result.getFrameCount());
        jsonObject.put("jankyFrames", result.getJankyCount());
        jsonObject.put("fps", result.getFps());
        return jsonObject;
    }

    /**
     * 获取网络流量信息
     *
     * @param packageName
     * @return
     * @throws JSONException
     */
    public static JSONObject getAppNetFlow(Object activityManager, String packageName) throws JSONException {
        return NetWorkSampler.getAppNetFlow(activityManager, packageName);
    }
}
