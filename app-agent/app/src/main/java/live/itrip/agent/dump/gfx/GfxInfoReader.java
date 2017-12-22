package live.itrip.agent.dump.gfx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import live.itrip.agent.Dumpsys;

/**
 * Created by Feng on 2017/9/19.
 */

public class GfxInfoReader {
    private String mAppPackage;

    public GfxInfoReader(String appPackage) {
        mAppPackage = appPackage;
    }

    public GfxInfoResult read() throws IOException, ParseException {
        String command = String.format("dumpsys %s %s", Dumpsys.gfxinfo, mAppPackage);

        InputStream is = Runtime.getRuntime().exec(command).getInputStream();
        String body = readFromStream(is);
        is.close();

        Pattern p = Pattern.compile("(\\s+[0-9,.]{4})(\\s+[0-9,.]{4})(\\s+[0-9,.]{4})(\\s+[0-9,.]{4})", Pattern.MULTILINE);
        Matcher m = p.matcher(body);
        DecimalFormat df = new DecimalFormat();

        int framescount = 0, jankCount = 0, vsync_overtime = 0;
        float countTime = 0;

        while (m.find()) {
            float oncetime = df.parse(m.group(1).trim()).floatValue()
                    + df.parse(m.group(2).trim()).floatValue()
                    + df.parse(m.group(3).trim()).floatValue()
                    + df.parse(m.group(4).trim()).floatValue();

            framescount += 1;
            countTime = countTime + oncetime;
            if (oncetime > 16.67) {
                jankCount += 1;
                if (oncetime % 16.67 == 0) {
                    vsync_overtime += oncetime / 16.67 - 1;
                } else {
                    vsync_overtime += oncetime / 16.67;
                }
            }

        }
        GfxInfoResult result = new GfxInfoResult();
        result.setFrameCount(framescount);

        if ((framescount + vsync_overtime) > 0) {
            float ffps = framescount * 60 / (framescount + vsync_overtime);
            result.setFps(ffps);
        }
        result.setJankyCount(jankCount);
        return result;
    }


    private String readFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        for (int len; (len = is.read(bytes)) > 0; ) {
            baos.write(bytes, 0, len);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }


    public static class GfxInfoResult {

        private int frameCount;
        private float fps;
        private int jankyCount;

        public GfxInfoResult() {

        }

        public int getFrameCount() {
            return frameCount;
        }

        public void setFrameCount(int frameCount) {
            this.frameCount = frameCount;
        }

        public float getFps() {
            return fps;
        }

        public void setFps(float fps) {
            this.fps = fps;
        }

        public int getJankyCount() {
            return jankyCount;
        }

        public void setJankyCount(int jankyCount) {
            this.jankyCount = jankyCount;
        }

        @Override
        public String toString() {
            return String.format("FPS:%s,Total frames:%s,Janky frames:%s", this.fps, this.frameCount, this.jankyCount);
        }
    }


    /**
     *
     */
    public class SetPropCommands {

        /**
         *  System property used to enable or disable hardware rendering profiling.
         * The default value of this property is assumed to be false.
         *
         * When profiling is enabled, the adb shell dumpsys gfxinfo command will
         * output extra information about the time taken to execute by the last
         * frames.
         *
         * Possible values:
         * "true", to enable profiling
         * "visual_bars", to enable profiling and visualize the results on screen
         * "false", to disable profiling
         */
//#define PROPERTY_PROFILE "debug.hwui.profile"
//            #define PROPERTY_PROFILE_VISUALIZE_BARS "visual_bars"
        /**
         * System property used to specify the number of frames to be used
         * when doing hardware rendering profiling.
         * The default value of this property is #PROFILE_MAX_FRAMES.
         * <p>
         * When profiling is enabled, the adb shell dumpsys gfxinfo command will
         * output extra information about the time taken to execute by the last
         * frames.
         * <p>
         * Possible values:
         * "60", to set the limit of frames to 60
         */
//            #define PROPERTY_PROFILE_MAXFRAMES "debug.hwui.profile.maxframes"


        public static final String PROPERTY_PROFILE_FALSE = "setprop debug.hwui.profile false";
        public static final String PROPERTY_PROFILE_VISUALIZE_BARS = "setprop debug.hwui.profile visual_bars";
        public static final String PROPERTY_PROFILE_TRUE = "setprop debug.hwui.profile true";
    }
}
