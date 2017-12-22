package live.itrip.agent.util;

import android.util.Log;

/**
 * Created on 2017/12/7.
 *
 * @author Feng
 *         Description : 自定义日志输出
 *         Update :
 */

public class LogUtils {
    private static final String LOGTAG = "itrip-agent";

    public static void v(String message) {
        Log.v(LOGTAG, message);
    }

    public static void i(String message) {
        Log.i(LOGTAG, message);
    }

    public static void d(String message) {
        Log.d(LOGTAG, message);
    }

    public static void w(String message) {
        Log.w(LOGTAG, message);
    }

    public static void w(String message, Throwable throwable) {
        Log.w(LOGTAG, message, throwable);
    }

    public static void e(String message) {
        Log.e(LOGTAG, message);
    }

    public static void e(String message, Throwable throwable) {
        Log.e(LOGTAG, message, throwable);
    }

}
