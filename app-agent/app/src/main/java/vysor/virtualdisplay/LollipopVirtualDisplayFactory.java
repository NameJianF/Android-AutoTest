package vysor.virtualdisplay;

import android.annotation.TargetApi;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.view.Surface;

@TargetApi(21)
public class LollipopVirtualDisplayFactory implements VirtualDisplayFactory {
    MediaProjection mediaProjection;

    public LollipopVirtualDisplayFactory(MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int dpi, int flags, Surface surface, Handler handler) {
        final VirtualDisplay vd = this.createVirtualDisplay(name, width, height, dpi, flags, surface, handler);
        return new VirtualDisplay() {
            public void release() {
                vd.release();
            }
        };
    }

    public void release() {
        this.mediaProjection.stop();
    }
}
