package live.itrip.agent.virtualdisplay;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Build.VERSION;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.koushikdutta.async.util.StreamUtility;

import org.xml.sax.Attributes;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import live.itrip.agent.util.Parsers;

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
    public MediaCodec encoder;
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

    protected abstract EncoderRunnable onSurfaceCreated(MediaCodec mediaCodec);

    public void registerVirtualDisplay(VirtualDisplayFactory vdf, int densityDpi) {
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
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            signalEnd();
        }
        this.encoder = null;
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
            if (this.encoder == venc) {
                this.encoder = null;
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void signalEnd() {
        if (this.encoder != null) {
            try {
                this.encoder.signalEndOfInputStream();
            } catch (Exception e) {
            }
        }
    }

    void setSurfaceFormat(MediaFormat video) {
        this.colorFormat = 2130708361;
        video.setInteger(MediaFormat.KEY_COLOR_FORMAT, 2130708361);
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
        return this.encoder;
    }

    public Point getEncodingDimensions() {
        return this.encSize;
    }

    public int getColorFormat() {
        return this.colorFormat;
    }


    public Surface createDisplaySurface() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            signalEnd();
        }
        this.encoder = null;
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
            String xml = StreamUtility.readFile("/system/etc/media_profiles.xml");
            RootElement root = new RootElement("MediaSettings");
            Element encoderElement = root.requireChild("VideoEncoderCap");
            ArrayList<VideoEncoderCap> encoders = new ArrayList();
            XmlListener mXmlListener = new XmlListener(encoders);
            encoderElement.setElementListener(mXmlListener);
            Reader mReader = new StringReader(xml);
            Parsers.parse(mReader, root.getContentHandler());

            if (encoders.size() != 1) {
                throw new Exception("derp");
            }

            VideoEncoderCap videoEncoderCap = encoders.get(0);
            maxWidth = videoEncoderCap.maxFrameWidth;
            maxHeight = videoEncoderCap.maxFrameHeight;
            bitrate = videoEncoderCap.maxBitRate;
            maxFrameRate = videoEncoderCap.maxFrameRate;
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
            MediaFormat mMediaFormat = MediaFormat.createVideoFormat("video/avc", this.width, this.height);
            Log.i(this.LOGTAG, "Bitrate: " + bitrate);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, maxFrameRate);
            Log.i(this.LOGTAG, "Frame rate: " + maxFrameRate);
            mMediaFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, TimeUnit.MILLISECONDS.toMicros((long) (1000 / maxFrameRate)));
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30);
            Log.i(this.LOGTAG, "Creating encoder");
            try {
                if (supportsSurface()) {
                    setSurfaceFormat(mMediaFormat);
                } else {
                    int selectColorFormat = selectColorFormat(codecInfo, "video/avc");
                    this.colorFormat = selectColorFormat;
                    mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, selectColorFormat);
                }
                this.encoder = MediaCodec.createEncoderByType("video/avc");
                Log.i(this.LOGTAG, "Created encoder");
                Log.i(this.LOGTAG, "Configuring encoder");
                this.encoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                Log.i(this.LOGTAG, "Creating input surface");
                Surface surface = null;
                if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && this.useSurface) {
                    surface = this.encoder.createInputSurface();
                }
                Log.i(this.LOGTAG, "Starting Encoder");
                this.encoder.start();
                Log.i(this.LOGTAG, "Surface ready");
                this.lastEncoderThread = new Thread(onSurfaceCreated(this.encoder), "Encoder");
                this.lastEncoderThread.start();
                Log.i(this.LOGTAG, "Encoder ready");
                return surface;
            } catch (Exception e2) {
                Log.e(this.LOGTAG, "Exception creating encoder", e2);
            }
        } catch (Exception e22) {
            Log.e(this.LOGTAG, "Error getting media profiles", e22);
        }
        return null;
    }

    private class XmlListener implements ElementListener {
        final ArrayList<VideoEncoderCap> encoders;

        XmlListener(ArrayList mList) {
            this.encoders = mList;
        }

        @Override
        public void end() {
        }

        @Override
        public void start(Attributes attributes) {
            if (TextUtils.equals(attributes.getValue("name"), "h264")) {
                this.encoders.add(new VideoEncoderCap(attributes));
            }
        }
    }

    private static class VideoEncoderCap {
//        <VideoEncoderCap name="h264" enabled="true"
//        minBitRate="64000" maxBitRate="40000000"
//        minFrameWidth="176" maxFrameWidth="1920"
//        minFrameHeight="144" maxFrameHeight="1080"
//        minFrameRate="15" maxFrameRate="30" />

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
}
