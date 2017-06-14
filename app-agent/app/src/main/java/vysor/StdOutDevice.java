package vysor;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import vysor.virtualdisplay.EncoderDevice;

public class StdOutDevice extends EncoderDevice {
    int bitrate = 500000;

    public StdOutDevice(String name, int width, int height) {
        super(name, width, height);
    }

    @Override
    protected EncoderRunnable onSurfaceCreated(MediaCodec mediaCodec) {
        return null;
    }
//    BufferedDataSink sink;

//    class Writer extends EncoderRunnable {
//        public Writer(MediaCodec venc) {
//            super(venc);
//        }
//
//        protected void encode() throws Exception {
//            Log.i(StdOutDevice.this.LOGTAG, "Writer started.");
//            ByteBuffer[] encouts = null;
//            boolean doneCoding = false;
//            boolean hasReceivedBuffer = false;
//            while (!doneCoding) {
//                BufferInfo info = new BufferInfo();
//                int bufIndex = StdOutDevice.this.venc.dequeueOutputBuffer(info, -1);
//                if (bufIndex >= 0) {
//                    if (!hasReceivedBuffer) {
//                        hasReceivedBuffer = true;
//                        Log.i(StdOutDevice.this.LOGTAG, "Got first buffer");
//                    }
//                    if (encouts == null) {
//                        encouts = StdOutDevice.this.venc.getOutputBuffers();
//                    }
//                    ByteBuffer b = encouts[bufIndex];
//                    ByteBuffer copy = ByteBufferList.obtain(info.size + 12).order(ByteOrder.LITTLE_ENDIAN);
//                    copy.putInt((info.size + 12) - 4);
//                    copy.putLong(info.presentationTimeUs);
//                    b.position(info.offset);
//                    b.limit(info.offset + info.size);
//                    copy.put(b);
//                    copy.flip();
//                    b.clear();
//                    StdOutDevice.this.sink.write(new ByteBufferList(copy));
//                    int rem = StdOutDevice.this.sink.remaining();
//                    if (rem != 0) {
//                        Log.i(StdOutDevice.this.LOGTAG, "Buffered: " + rem);
//                    }
//                    StdOutDevice.this.venc.releaseOutputBuffer(bufIndex, false);
//                    doneCoding = (info.flags & 4) != 0;
//                } else if (bufIndex == -3) {
//                    encouts = null;
//                    Log.i(StdOutDevice.this.LOGTAG, "MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED");
//                } else if (bufIndex == -2) {
//                    Log.i(StdOutDevice.this.LOGTAG, "MediaCodec.INFO_OUTPUT_FORMAT_CHANGED");
//                    MediaFormat outputFormat = StdOutDevice.this.venc.getOutputFormat();
//                    Log.i(StdOutDevice.this.LOGTAG, "output width: " + outputFormat.getInteger("width"));
//                    Log.i(StdOutDevice.this.LOGTAG, "output height: " + outputFormat.getInteger("height"));
//                }
//            }
//            StdOutDevice.this.sink.end();
//            Log.i(StdOutDevice.this.LOGTAG, "Writer done");
//        }
//    }
//
//    public StdOutDevice(int width, int height, BufferedDataSink sink) {
//        super("stdout", width, height);
//        this.sink = sink;
//    }
//
//    public int getBitrate() {
//        return this.bitrate;
//    }
//
//    @TargetApi(19)
//    public void setBitrate(int bitrate) {
//        Log.i(this.LOGTAG, "Bitrate: " + bitrate);
//        this.bitrate = bitrate;
//        Bundle bundle = new Bundle();
//        bundle.putInt("video-bitrate", bitrate);
//        this.venc.setParameters(bundle);
//    }
//
//    @TargetApi(19)
//    public void requestSyncFrame() {
//        Bundle bundle = new Bundle();
//        bundle.putInt("request-sync", 0);
//        this.venc.setParameters(bundle);
//    }
//
//    protected EncoderRunnable onSurfaceCreated(MediaCodec venc) {
//        return new Writer(venc);
//    }
}
