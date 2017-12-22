package live.itrip.agent.service;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.InvocationTargetException;

import live.itrip.agent.Main;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;

/**
 * Created on 2017/12/5.
 *
 * @author Feng
 *         Description :
 *         Update :
 */

public class AgentService extends android.app.Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        LogUtils.e("AgentService start....");


        Context context = this.getApplicationContext();
        DisplayManager dm = null;
        dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE); //InternalApi.getDisplayManager();

        if (dm == null) {
            LogUtils.e("AgentService DisplayManager is null ....");
        }

        int flag = 0;
        while (flag < 100 && dm != null) {
//            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Display[] displays = dm.getDisplays();
            if (displays != null) {
                LogUtils.e(String.format("DisplayManager displays size[%s]", displays.length));
                for (Display display : displays) {
                    float fps = display.getRefreshRate();
                    LogUtils.e(String.format("AgentService FPS[%s]", fps));
                }
            }
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            flag++;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onLowMemory() {
        LogUtils.w("Low memory");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.i("Stopping agent service");
    }
}
