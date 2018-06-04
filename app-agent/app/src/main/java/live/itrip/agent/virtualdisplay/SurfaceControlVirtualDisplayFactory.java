package live.itrip.agent.virtualdisplay;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.Surface;

import java.lang.reflect.Method;

import live.itrip.agent.Main;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;


/**
 * Created on 2017/9/8.
 *
 * @author JianF
 */
public class SurfaceControlVirtualDisplayFactory implements VirtualDisplayFactory {
    private Rect displayRect;
    private Point displaySize = getCurrentDisplaySize();

    public static Point getCurrentDisplaySize() {
        return getCurrentDisplaySize(true);
    }

    public static Point getCurrentDisplaySize(boolean rotate) {
        try {
            Point displaySize = new Point();
            IWindowManager wm = InternalApi.getWindowManager();
            int rotation;
            wm.getInitialDisplaySize(0, displaySize);
            rotation = wm.getRotation();
            if ((rotate && rotation == 1) || rotation == 3) {
                int swap = displaySize.x;
                displaySize.x = displaySize.y;
                displaySize.y = swap;
            }
            return displaySize;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int dpi, int flags, Surface surface, Handler handler) {
        try {
            Class surfaceControlClass = Class.forName("android.view.SurfaceControl");
//            Class cls = surfaceControlClass;
            final IBinder token = (IBinder) surfaceControlClass.getDeclaredMethod("createDisplay", new Class[]{String.class, Boolean.TYPE}).invoke(null, new Object[]{name, Boolean.valueOf(false)});
            Method setDisplaySurfaceMethod = surfaceControlClass.getDeclaredMethod("setDisplaySurface", new Class[]{IBinder.class, Surface.class});
            final Method setDisplayProjectionMethod = surfaceControlClass.getDeclaredMethod("setDisplayProjection", new Class[]{IBinder.class, Integer.TYPE, Rect.class, Rect.class});
            Method setDisplayLayerStackMethod = surfaceControlClass.getDeclaredMethod("setDisplayLayerStack", new Class[]{IBinder.class, Integer.TYPE});
            final Method openTransactionMethod = surfaceControlClass.getDeclaredMethod("openTransaction", new Class[0]);
            final Method closeTransactionMethod = surfaceControlClass.getDeclaredMethod("closeTransaction", new Class[0]);
            final Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
            this.displayRect = new Rect(0, 0, width, height);
            Rect layerStackRect = new Rect(0, 0, this.displaySize.x, this.displaySize.y);
            openTransactionMethod.invoke(null, new Object[0]);
            setDisplaySurfaceMethod.invoke(null, new Object[]{token, surface});
            setDisplayProjectionMethod.invoke(null, new Object[]{token, Integer.valueOf(0), layerStackRect, this.displayRect});
            setDisplayLayerStackMethod.invoke(null, new Object[]{token, Integer.valueOf(0)});
            closeTransactionMethod.invoke(null, new Object[0]);
            final Method destroyDisplayMethod = surfaceControlClass.getDeclaredMethod("destroyDisplay", new Class[]{IBinder.class});
            return new VirtualDisplay() {
                IRotationWatcher watcher;
                IWindowManager wm = Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
                @Override
                public void release() {
                    LogUtils.i("VirtualDisplay released");
                    this.wm = null;
                    this.watcher = null;
                    try {
                        destroyDisplayMethod.invoke(null, new Object[]{token});
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                }
            };
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public Point getDisplaySize() {
        return new Point(this.displaySize);
    }

    public Rect getDisplayRect() {
        return this.displayRect;
    }

    @Override
    public void release() {
        LogUtils.i("SurfaceControlVirtualDisplayFactory released");
    }

}
