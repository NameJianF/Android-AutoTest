package live.itrip.agent.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import live.itrip.agent.Main;
import live.itrip.agent.util.LogUtils;

/**
 * Created by Feng on 2017/9/19.
 */

public class StartServiceReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(intent);
        String pkg = intent.getStringExtra("pkg");
        String serviceName = intent.getStringExtra("serviceName");

        LogUtils.i("start service : " + serviceName + " ......");

        i.setComponent(new ComponentName(pkg, serviceName));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(i);
        LogUtils.i("service : " + serviceName + " started......");
    }
}
