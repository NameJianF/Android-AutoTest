package live.itrip.agent;

/**
 * Created by Feng on 2017/9/14.
 */

public class AdbRotationHelper {
    public static void forceRotation(final int rotation) {
        new Thread() {
            public void run() {
                try {
                    Runtime.getRuntime().exec("/system/bin/content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:0").waitFor();
                    Runtime.getRuntime().exec("/system/bin/content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:" + rotation).waitFor();
                    Runtime.getRuntime().exec("/system/bin/content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:1").waitFor();
                } catch (Exception e) {
                }
            }
        }.start();
    }
}
