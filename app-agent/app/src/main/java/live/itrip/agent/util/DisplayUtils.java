package live.itrip.agent.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.IWindowManager;

import java.lang.reflect.Method;

/**
 * Created by Feng on 2017/9/7.
 */
public class DisplayUtils {
    /***
     * Retrieves the device actual display size.
     *
     * @return {@link Point}
     */
    public static Point getCurrentDisplaySize() {
        try {
            Point localPoint = new Point();

            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getDeclaredMethod("getService", String.class);

            // WindowManager
            Object ws = getService.invoke(null, Context.WINDOW_SERVICE);

            Object localObject = IWindowManager.Stub.asInterface((IBinder) ws);
            IWindowManager iWindowManager = (IWindowManager) localObject;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                iWindowManager.getInitialDisplaySize(0, localPoint);
            } else {
                iWindowManager.getRealDisplaySize(localPoint);
            }

            System.out.println(">>> Dimension: " + localPoint);
            return localPoint;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
