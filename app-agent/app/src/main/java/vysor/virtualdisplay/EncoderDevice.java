package vysor.virtualdisplay;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

//import com.koushikdutta.async.util.StreamUtility;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.xml.sax.Attributes;

public abstract class EncoderDevice {
    static final boolean assertionsDisabled = (!EncoderDevice.class.desiredAssertionStatus());
    public final String LOGTAG = getClass().getSimpleName();
    int colorFormat;
    Point encSize;
    int height;
    Thread lastEncoderThread;
    public String name;
    boolean useSurface = true;
    public VirtualDisplayFactory vdf;
    public MediaCodec venc;
    public VirtualDisplay virtualDisplay;
    int width;

    protected abstract class EncoderRunnable implements Runnable {
        MediaCodec venc;

        protected abstract void encode() throws Exception;

        public EncoderRunnable(MediaCodec venc) {
            this.venc = venc;
        }

        protected void cleanup() {
            EncoderDevice.this.destroyDisplaySurface(this.venc);
            this.venc = null;
        }

        public final void run() {
            try {
                encode();
            } catch (Exception e) {
                Log.e(EncoderDevice.this.LOGTAG, "Encoder error", e);
            }
            cleanup();
            Log.i(EncoderDevice.this.LOGTAG, "=======ENCODING COMPELTE=======");
        }
    }

    private static class VideoEncoderCap {
        int maxBitRate;
        int maxFrameHeight;
        int maxFrameRate;
        int maxFrameWidth;

        public VideoEncoderCap(Attributes attributes) {
            this.maxFrameWidth = Integer.valueOf(attributes.getValue("maxFrameWidth")).intValue();
            this.maxFrameHeight = Integer.valueOf(attributes.getValue("maxFrameHeight")).intValue();
            this.maxBitRate = Integer.valueOf(attributes.getValue("maxBitRate")).intValue();
            this.maxFrameRate = Integer.valueOf(attributes.getValue("maxFrameRate")).intValue();
        }
    }

    protected abstract EncoderRunnable onSurfaceCreated(MediaCodec mediaCodec);

    public boolean isConnected() {
        return this.venc != null;
    }

    public void registerVirtualDisplay(Context context, VirtualDisplayFactory vdf, int densityDpi) {
        if (assertionsDisabled || this.virtualDisplay == null) {
            Surface surface = createDisplaySurface();
            if (surface == null) {
                Log.e(this.LOGTAG, "Unable to create surface");
                return;
            }
            Log.e(this.LOGTAG, "Created surface");
            this.vdf = vdf;
            this.virtualDisplay = vdf.createVirtualDisplay(this.name, this.width, this.height, densityDpi, 3, surface, null);
            return;
        }
        throw new AssertionError();
    }

    public EncoderDevice(String name, int width, int height) {
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public void stop() {
        if (VERSION.SDK_INT >= 18) {
            signalEnd();
        }
        this.venc = null;
        if (this.virtualDisplay != null) {
            this.virtualDisplay.release();
            this.virtualDisplay = null;
        }
        if (this.vdf != null) {
            this.vdf.release();
            this.vdf = null;
        }
    }

    void destroyDisplaySurface(MediaCodec venc) {
        if (venc != null) {
            try {
                venc.stop();
                venc.release();
            } catch (Exception e) {
            }
            if (this.venc == venc) {
                this.venc = null;
                if (this.virtualDisplay != null) {
                    this.virtualDisplay.release();
                    this.virtualDisplay = null;
                }
                if (this.vdf != null) {
                    this.vdf.release();
                    this.vdf = null;
                }
            }
        }
    }

    @TargetApi(18)
    void signalEnd() {
        if (this.venc != null) {
            try {
                this.venc.signalEndOfInputStream();
            } catch (Exception e) {
            }
        }
    }

    @TargetApi(18)
    Surface createInputSurface() {
        return this.venc.createInputSurface();
    }

    @TargetApi(18)
    void setSurfaceFormat(MediaFormat video) {
        this.colorFormat = 2130708361;
        video.setInteger("color-format", 2130708361);
    }

    private int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) throws Exception {
        CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        Log.i(this.LOGTAG, "Available color formats: " + capabilities.colorFormats.length);
        for (int colorFormat : capabilities.colorFormats) {
            if (isRecognizedFormat(colorFormat)) {
                Log.i(this.LOGTAG, "Using: " + colorFormat);
                return colorFormat;
            }
            Log.i(this.LOGTAG, "Not using: " + colorFormat);
        }
        throw new Exception("Unable to find suitable color format");
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case 19:
            case 20:
            case 21:
            case 39:
            case 2130706688:
                return true;
            default:
                return false;
        }
    }

    public void useSurface(boolean useSurface) {
        this.useSurface = useSurface;
    }

    public boolean supportsSurface() {
        return VERSION.SDK_INT >= 19 && this.useSurface;
    }

    public MediaCodec getMediaCodec() {
        return this.venc;
    }

    public Point getEncodingDimensions() {
        return this.encSize;
    }

    public int getColorFormat() {
        return this.colorFormat;
    }

    public static int getSupportedDimension(int dim) {
        if (dim <= 144) {
            return 144;
        }
        if (dim <= 176) {
            return 176;
        }
        if (dim <= 240) {
            return 240;
        }
        if (dim <= 288) {
            return 288;
        }
        if (dim <= 320) {
            return 320;
        }
        if (dim <= 352) {
            return 352;
        }
        if (dim <= 480) {
            return 480;
        }
        if (dim <= 576) {
            return 576;
        }
        if (dim <= 720) {
            return 720;
        }
        if (dim <= 1024) {
            return 1024;
        }
        if (dim <= 1280) {
            return 1280;
        }
        return 1920;
    }


    @SuppressLint("WrongConstant")
    public Surface createDisplaySurface() {
        if (VERSION.SDK_INT >= 18) {
            signalEnd();
        }
        this.venc = null;
        MediaCodecInfo codecInfo = null;
        try {
            int numCodecs = MediaCodecList.getCodecCount();
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo found = MediaCodecList.getCodecInfoAt(i);
                if (found.isEncoder()) {
                    for (String type : found.getSupportedTypes()) {
                        if (type.equalsIgnoreCase("video/avc")) {
                            if (codecInfo == null) {
                                codecInfo = found;
                            }
                            Log.i(this.LOGTAG, found.getName());
                            CodecCapabilities caps = found.getCapabilitiesForType("video/avc");
                            for (int colorFormat : caps.colorFormats) {
                                Log.i(this.LOGTAG, "colorFormat: " + colorFormat);
                            }
                            for (CodecProfileLevel profileLevel : caps.profileLevels) {
                                Log.i(this.LOGTAG, "profile/level: " + profileLevel.profile + "/" + profileLevel.level);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        int maxWidth;
        int maxHeight;
        int bitrate;
        int maxFrameRate;
        try {
//            String xml = StreamUtility.readFile("/system/etc/media_profiles.xml");
//            h hVar = new h("MediaSettings");
//            c encoder = hVar.b("VideoEncoderCap");
            final ArrayList<VideoEncoderCap> encoders = new ArrayList();
//            encoder.a((d) new d() {
//                public void end() {
//                }
//
//                public void start(Attributes attributes) {
//                    if (TextUtils.equals(attributes.getValue("name"), "h264")) {
//                        encoders.add(new VideoEncoderCap(attributes));
//                    }
//                }
//            });
//            g.a(new StringReader(xml), hVar.b());
            if (encoders.size() != 1) {
                throw new Exception("derp");
            }
            VideoEncoderCap v = (VideoEncoderCap) encoders.get(0);
            maxWidth = v.maxFrameWidth;
            maxHeight = v.maxFrameHeight;
            bitrate = v.maxBitRate;
            maxFrameRate = v.maxFrameRate;
            int max = Math.max(maxWidth, maxHeight);
            int min = Math.min(maxWidth, maxHeight);
            double ratio;
            if (this.width > this.height) {
                if (this.width > max) {
                    ratio = ((double) max) / ((double) this.width);
                    this.width = max;
                    this.height = (int) (((double) this.height) * ratio);
                }
                if (this.height > min) {
                    ratio = ((double) min) / ((double) this.height);
                    this.height = min;
                    this.width = (int) (((double) this.width) * ratio);
                }
            } else {
                if (this.height > max) {
                    ratio = ((double) max) / ((double) this.height);
                    this.height = max;
                    this.width = (int) (((double) this.width) * ratio);
                }
                if (this.width > min) {
                    ratio = ((double) min) / ((double) this.width);
                    this.width = min;
                    this.height = (int) (((double) this.height) * ratio);
                }
            }
            this.width /= 16;
            this.width *= 16;
            this.height /= 16;
            this.height *= 16;
            Log.i(this.LOGTAG, "Width: " + this.width + " Height: " + this.height);
            this.encSize = new Point(this.width, this.height);
            MediaFormat video = MediaFormat.createVideoFormat("video/avc", this.width, this.height);
            bitrate = getBitrate(bitrate);
            Log.i(this.LOGTAG, "Bitrate: " + bitrate);
            video.setInteger("bitrate", bitrate);
            video.setInteger("frame-rate", maxFrameRate);
            Log.i(this.LOGTAG, "Frame rate: " + maxFrameRate);
            video.setLong("repeat-previous-frame-after", TimeUnit.MILLISECONDS.toMicros((long) (1000 / maxFrameRate)));
            video.setInteger("i-frame-interval", 30);
            Log.i(this.LOGTAG, "Creating encoder");
            try {
                if (supportsSurface()) {
                    setSurfaceFormat(video);
                } else {
                    int selectColorFormat = selectColorFormat(codecInfo, "video/avc");
                    this.colorFormat = selectColorFormat;
                    video.setInteger("color-format", selectColorFormat);
                }
                this.venc = MediaCodec.createEncoderByType("video/avc");
                Log.i(this.LOGTAG, "Created encoder");
                Log.i(this.LOGTAG, "Configuring encoder");
                this.venc.configure(video, null, null, MediaCodec.BUFFER_FLAG_KEY_FRAME);
                Log.i(this.LOGTAG, "Creating input surface");
                Surface surface = null;
                if (VERSION.SDK_INT >= 18 && this.useSurface) {
                    surface = this.venc.createInputSurface();
                }
                Log.i(this.LOGTAG, "Starting Encoder");
                this.venc.start();
                Log.i(this.LOGTAG, "Surface ready");
                this.lastEncoderThread = new Thread(onSurfaceCreated(this.venc), "Encoder");
                this.lastEncoderThread.start();
                Log.i(this.LOGTAG, "Encoder ready");
                return surface;
            } catch (Exception e2) {
                Log.e(this.LOGTAG, "Exception creating venc", e2);
                throw new AssertionError(e2);
            }
        } catch (Exception e22) {
            Log.e(this.LOGTAG, "Error getting media profiles", e22);
            CamcorderProfile profile = null;
            try {
                profile = CamcorderProfile.get(6);
            } catch (Throwable ex) {
                Log.e(this.LOGTAG, "Error getting camcorder profiles", ex);
            }
            if (profile == null) {
                try {
                    profile = CamcorderProfile.get(5);
                } catch (Throwable ex2) {
                    Log.e(this.LOGTAG, "Error getting camcorder profiles", ex2);
                }
            }
            if (profile == null) {
                maxWidth = 640;
                maxHeight = 480;
                bitrate = 2000000;
                maxFrameRate = 30;
            } else {
                maxWidth = profile.videoFrameWidth;
                maxHeight = profile.videoFrameHeight;
                bitrate = profile.videoBitRate;
                maxFrameRate = profile.videoFrameRate;
            }
        }
        return null;
    }

    public void joinEncoderThread() {
        try {
            if (this.lastEncoderThread != null) {
                this.lastEncoderThread.join();
            }
        } catch (InterruptedException e) {
        }
    }

    public int getBitrate(int maxBitrate) {
        return 2000000;
    }
}
