package live.itrip.agent.dump.surfaceflinger;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/19.
 */

public class SurfaceFlingerReader {
    //清空之前采样的数据，防止统计重复的时间
    private static String clearCommand = "dumpsys SurfaceFlinger --latency-clear";

    //计算fps 通过SurfaceFlinger --latency获取
   public static void getFps(String layerName) throws IOException {
        //      test data
        layerName = "com.diygame.nguidemo01/com.unity3d.player.UnityPlayerNativeActivity";

        String command = "dumpsys SurfaceFlinger --latency " + layerName;
        double MillSecds = 1000000.0;
        double NANOS = 1000000000.0;

        BufferedReader br = null, br2 = null, br3 = null;
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("#.0");
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("#.00");
        java.text.DecimalFormat df3 = new java.text.DecimalFormat("#.000");
        double refreshPriod = 0; //设备刷新周期
        try {
            Process p = Runtime.getRuntime().exec(command);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String returnInfo = null;
            double b = 0;
            int frames = 0, jank = 0;
            double totalCountPeriod = 0;
            String beginRenderTime = "0.0", endRenderTime = "0.0";
            double r = 0;
            int count = 1;
            while ((returnInfo = br.readLine()) != null) {
                if (!"".equals(returnInfo) && returnInfo.length() > 0) {
                    count++;
                    int frameSize = returnInfo.split("\\s{1,}").length;
                    if (frameSize == 1) {
                        refreshPriod = Double.parseDouble(returnInfo) / MillSecds;
                        b = 0;
                        frames = 0;
                        r = refreshPriod;
                    } else {

                        if (frameSize == 3) {
                            String[] timeStamps = returnInfo.split("\\s{1,}");
                            double t0 = Double.parseDouble(timeStamps[0]);
                            double t1 = Double.parseDouble(timeStamps[1]);
                            double t2 = Double.parseDouble(timeStamps[2]);
                            if (t1 > 0 && !"9223372036854775807".equals(timeStamps[1])) {
                                if (b == 0) {
                                    b = t1;
                                    jank = 0;
                                } else {
                                    double countPeriod = (t1 - b) / MillSecds; //统计周期，大于500ms重新置为0
                                    if (countPeriod > 500) {
                                        if (frames > 0) {
                                            Log.d(Main.LOGTAG, " totalCountPeriod / 1000 " + totalCountPeriod / 1000);
                                            String msg = "SurfaceFlinger方式(超时了) | 开始采样时间点：" + beginRenderTime + "s   "
                                                    + "|结束采样时间点：" + df3.format(b / NANOS) + "s   "
                                                    + "|fps：" + df2.format(frames * 1000 / totalCountPeriod)
                                                    + "   |Frames：" + frames
                                                    + "   |单帧平均渲染时间：" + df2.format(totalCountPeriod / frames) + "ms";
                                            Log.d(Main.LOGTAG, msg);
                                        }
                                        b = t1;
                                        frames = 0;
                                        totalCountPeriod = 0;
                                        jank = 0;
                                    } else {
                                        frames += 1;
                                        if (countPeriod > r) {
                                            totalCountPeriod += countPeriod;
                                            if ((t2 - t0) / MillSecds > r) {
                                                jank += 1;
                                            }
                                            b = t1;
                                        } else {
                                            totalCountPeriod += r;
                                            b = Double.parseDouble(df1.format(b + r * MillSecds));
                                        }
                                    }
                                }
                                if (frames == 0) {
                                    beginRenderTime = df3.format(t1 / NANOS);
                                }
                            }
                        }
                    }
                }
            }
            if (frames > 0) {
                String msg = "SurfaceFlinger方式 | 开始采样时间点：" + beginRenderTime + "s   "
                        + "|结束采样时间点：" + df3.format(b / NANOS) + "s   "
                        + "|fps：" + df2.format(frames * 1000 / totalCountPeriod)
                        + "   |Frames：" + frames
                        + "   |单帧平均渲染时间：" + df2.format(totalCountPeriod / frames) + "ms";
                Log.d(Main.LOGTAG, msg);
            } else {
                Log.d(Main.LOGTAG, "获取的层不正确  or 当前没有渲染操作,请拖动屏幕");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    Runtime.getRuntime().exec(clearCommand);
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
