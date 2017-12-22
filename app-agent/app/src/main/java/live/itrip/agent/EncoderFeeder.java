package live.itrip.agent;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.MediaCodec;
import android.os.SystemClock;
import android.view.IWindowManager;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import live.itrip.agent.renderscript.YuvConverter;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;


/**
 * Created on 2017/9/7.
 *
 * @author JianF
 */

public class EncoderFeeder {
    private int colorFormat;
    private int height;
    private int rotation;
    private MediaCodec mediaCodec;
    private int width;
    private boolean stopFeed = false;

    public EncoderFeeder(MediaCodec codec, int width, int height, int colorFormat) {
        this.mediaCodec = codec;
        this.width = width;
        this.height = height;
        this.colorFormat = colorFormat;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void feed() {
        this.stopFeed = false;
        new Thread(new Runnable() {
            public void run() {
                try {
                    EncoderFeeder.this.feedMe();
                } catch (Exception e) {
                    LogUtils.e("Error", e);
                }
            }
        }).start();
    }

    public void setStopFeed() {
        this.stopFeed = true;
    }

    private void feedMe() throws Exception {
        LogUtils.i("EncoderFeeder >>> feedMe");
        ByteBuffer[] incs = null;
        Method screenshot = InternalApi.getMethodScreenshot();
        YuvConverter y = YuvConverter.createYPlaneConverter();
        YuvConverter uv = null;
        YuvConverter u = null;
        YuvConverter v = null;
        if (isSemiPlanarYUV(this.colorFormat)) {
            uv = YuvConverter.createUVConverter();
        } else {
            u = YuvConverter.createUConverter();
            v = YuvConverter.createVConverter();
        }
        while (!stopFeed) {
            int buffer = this.mediaCodec.dequeueInputBuffer(-1);
            if (buffer >= 0) {
                if (incs == null) {
                    incs = this.mediaCodec.getInputBuffers();
                }
                ByteBuffer inc = incs[buffer];
                inc.clear();
                Bitmap b = (Bitmap) screenshot.invoke(null, new Object[]{this.width, this.height});
                if (this.rotation != 0) {
                    Matrix m = new Matrix();
                    if (this.rotation == 1) {
                        m.postRotate(-90.0f);
                    } else if (this.rotation == 2) {
                        m.postRotate(-180.0f);
                    } else if (this.rotation == 3) {
                        m.postRotate(-270.0f);
                    }
                    b = Bitmap.createScaledBitmap(Bitmap.createBitmap(b, 0, 0, this.width, this.height, m, false), this.width, this.height, false);
                }
                LogUtils.i("Bitmap info " + b.getWidth() + " " + b.getHeight() + " " + b.getRowBytes());
                Bitmap scaled = Bitmap.createScaledBitmap(b, this.width / 2, this.height / 2, false);
                int size = y.convert(b, inc);
                /**
                 if (Build.VERSION.SDK_INT < 18 && this.mediaCodec.getName().toLowerCase().contains("qcom")) {
                 int padding = size % 2048;
                 if (padding != 0) {
                 padding = 2048 - padding;
                 inc.position(inc.position() + padding);
                 size += padding;
                 }
                 }
                 **/
                if (uv != null) {
                    size += uv.convert(scaled, inc);
                } else {
                    size = (size + u.convert(scaled, inc)) + v.convert(scaled, inc);
                }
                inc.clear();
                this.mediaCodec.queueInputBuffer(buffer, 0, size, TimeUnit.MILLISECONDS.toMicros(SystemClock.elapsedRealtime()), 0);
            }
        }

    }

    private static boolean isSemiPlanarYUV(int colorFormat) {
        switch (colorFormat) {
            case 19:
            case 20:
                return false;
            case 21:
            case 39:
            case 2130706688:
                return true;
            default:
                throw new RuntimeException("unknown format " + colorFormat);
        }
    }

    public static Bitmap screenshot(IWindowManager wm) throws Exception {
        Point size = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize(false);
        Bitmap b = (Bitmap) InternalApi.getMethodScreenshot()
                .invoke(null, new Object[]{size.x, size.y});
        int rotation = wm.getRotation();
        if (rotation == 0) {
            return b;
        }
        Matrix m = new Matrix();
        if (rotation == 1) {
            m.postRotate(-90.0f);
        } else if (rotation == 2) {
            m.postRotate(-180.0f);
        } else if (rotation == 3) {
            m.postRotate(-270.0f);
        }
        return Bitmap.createBitmap(b, 0, 0, size.x, size.y, m, false);
    }
}
