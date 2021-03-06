package live.itrip.agent.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import live.itrip.agent.service.IMEService;
import live.itrip.agent.ui.MainActivity;

/**
 * Created by Feng on 2017/9/19.
 */

public class CharCodeReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(intent);
        i.setComponent(new ComponentName(context, IMEService.class));
        context.startService(i);
    }
}
