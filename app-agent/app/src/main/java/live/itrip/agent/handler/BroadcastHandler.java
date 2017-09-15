package live.itrip.agent.handler;

import android.content.Intent;

import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/14.
 */

public class BroadcastHandler {

    /**
     * @param intent
     * @throws Exception
     */
    public static void sendBroadcast(Intent intent) throws Exception {
        if (Main.broadcastIntent.getParameterTypes().length == 11) {
            Main.broadcastIntent.invoke(Main.activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        } else if (Main.broadcastIntent.getParameterTypes().length == 12) {
            Main.broadcastIntent.invoke(Main.activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        } else if (Main.broadcastIntent.getParameterTypes().length == 13) {
            Main.broadcastIntent.invoke(Main.activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        }
    }
}
