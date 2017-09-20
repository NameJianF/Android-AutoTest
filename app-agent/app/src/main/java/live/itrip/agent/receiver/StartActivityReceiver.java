package live.itrip.agent.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Feng on 2017/9/19.
 */

public class StartActivityReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(intent);
        String pkg = intent.getStringExtra("pkg");
        String activity = intent.getStringExtra("activity");
        i.setComponent(new ComponentName(pkg, activity));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
