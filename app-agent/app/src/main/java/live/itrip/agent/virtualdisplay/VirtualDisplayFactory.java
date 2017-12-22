package live.itrip.agent.virtualdisplay;

import android.os.Handler;
import android.view.Surface;

/**
 * Created on 2017/9/8.
 * @author JianF
 */

public interface VirtualDisplayFactory {

    VirtualDisplay createVirtualDisplay(String str, int i, int i2, int i3, int i4, Surface surface, Handler handler);

    void release();
}
