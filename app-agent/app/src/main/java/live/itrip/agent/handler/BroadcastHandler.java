package live.itrip.agent.handler;

import android.content.Intent;

import java.lang.reflect.Method;

import live.itrip.agent.Main;
import live.itrip.agent.util.InternalApi;

/**
 * Created on 2017/9/14.
 * @author JianF
 */

public class BroadcastHandler {

    /**
     * @param intent
     * @throws Exception
     */
    public static void sendBroadcast(Intent intent) throws Exception {
        Method broadcastIntent = InternalApi.getBroadcastIntent();
        Object activityManager = InternalApi.getActivityManager();
        if (broadcastIntent.getParameterTypes().length == 11) {
            broadcastIntent.invoke(activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        } else if (broadcastIntent.getParameterTypes().length == 12) {
            broadcastIntent.invoke(activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        } else if (broadcastIntent.getParameterTypes().length == 13) {
            broadcastIntent.invoke(activityManager, new Object[]{null, intent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        }
    }
}
