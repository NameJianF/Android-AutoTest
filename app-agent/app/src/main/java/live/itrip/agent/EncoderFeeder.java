package live.itrip.agent;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.MediaCodec;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.IWindowManager;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import live.itrip.agent.renderscript.YuvConverter;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;


/**
 * Created by Feng on 2017/9/7.
 */

public class EncoderFeeder {
    int colorFormat;
    int height;
    int rotation;
    MediaCodec mediaCodec;
    int width;

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
        new Thread(new Runnable() {
            public void run() {
                try {
                    EncoderFeeder.this.feedMe();
                } catch (Exception e) {
                    Log.e(Main.LOGTAG, "Error", e);
                }
            }
        }).start();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void feedMe() throws Exception {
        String surfaceClassName;
        ByteBuffer[] incs = null;
        if (Build.VERSION.SDK_INT <= 17) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Class cls = Class.forName(surfaceClassName);
        Method screenshot = cls.getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE});
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
        while (true) {
            int buffer = this.mediaCodec.dequeueInputBuffer(-1);
            if (buffer >= 0) {
                if (incs == null) {
                    incs = this.mediaCodec.getInputBuffers();
                }
                ByteBuffer inc = incs[buffer];
                inc.clear();
                Bitmap b = (Bitmap) screenshot.invoke(null, new Object[]{Integer.valueOf(this.width), Integer.valueOf(this.height)});
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
                Log.i(Main.LOGTAG, "Bitmap info " + b.getWidth() + " " + b.getHeight() + " " + b.getRowBytes());
                Bitmap scaled = Bitmap.createScaledBitmap(b, this.width / 2, this.height / 2, false);
                int size = 0 + y.convert(b, inc);
                if (Build.VERSION.SDK_INT < 18 && this.mediaCodec.getName().toLowerCase().contains("qcom")) {
                    int padding = size % 2048;
                    if (padding != 0) {
                        padding = 2048 - padding;
                        inc.position(inc.position() + padding);
                        size += padding;
                    }
                }
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
        String surfaceClassName;
        Point size = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Bitmap b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(size.x), Integer.valueOf(size.y)});
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
