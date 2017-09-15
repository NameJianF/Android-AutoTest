package live.itrip.agent;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Feng on 2017/9/11.
 */

public class TestDatas {

    public static void writeTombstones() throws IOException {
        String content = "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***\n" +
                "Build fingerprint: 'Android-x86/android_x86/x86:5.1.1/LMY48W/woshijpf04211939:eng/test-keys'\n" +
                "Revision: '0'\n" +
                "ABI: 'x86'\n" +
                "pid: 1019, tid: 1019, name: surfaceflinger  >>> /system/bin/surfaceflinger <<<\n" +
                "signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x4\n" +
                "    eax a6265c06  ebx b7467d88  ecx b7631a22  edx a6265c06\n" +
                "    esi 00000000  edi b6867140\n" +
                "    xcs 00000073  xds 0000007b  xes 0000007b  xfs 00000000  xss 0000007b\n" +
                "    eip b745a639  ebp bfcfc1e8  esp bfcfc150  flags 00010282\n" +
                "\n" +
                "backtrace:\n" +
                "    #00 pc 00006639  /system/lib/libui.so (android::Fence::waitForever(char const*)+41)\n" +
                "    #01 pc 00034b86  /system/lib/libsurfaceflinger.so\n" +
                "    #02 pc 0003229e  /system/lib/libsurfaceflinger.so\n" +
                "    #03 pc 0002cb9c  /system/lib/libgui.so (android::BufferQueue::ProxyConsumerListener::onFrameAvailable(android::BufferItem const&)+652)\n" +
                "    #04 pc 000342f4  /system/lib/libgui.so (android::BufferQueueProducer::queueBuffer(int, android::IGraphicBufferProducer::QueueBufferInput const&, android::IGraphicBufferProducer::QueueBufferOutput*)+2580)\n" +
                "    #05 pc 0004eafb  /system/lib/libgui.so (android::Surface::queueBuffer(ANativeWindowBuffer*, int)+411)\n" +
                "    #06 pc 0004ce06  /system/lib/libgui.so (android::Surface::hook_queueBuffer(ANativeWindow*, ANativeWindowBuffer*, int)+38)\n" +
                "    #07 pc 00014bc6  /system/lib/egl/libGLES_android.so\n" +
                "    #08 pc 00017f73  /system/lib/egl/libGLES_android.so (eglSwapBuffers+163)\n" +
                "    #09 pc 00015fdb  /system/lib/libEGL.so (eglSwapBuffers+203)\n" +
                "    #10 pc 000013ea  /system/lib/hw/hwcomposer.x86.so\n" +
                "    #11 pc 00034730  /system/lib/libsurfaceflinger.so\n" +
                "    #12 pc 000256d4  /system/lib/libsurfaceflinger.so\n" +
                "    #13 pc 00024bf4  /system/lib/libsurfaceflinger.so\n" +
                "    #14 pc 000236fb  /system/lib/libsurfaceflinger.so\n" +
                "    #15 pc 0002338a  /system/lib/libsurfaceflinger.so\n" +
                "    #16 pc 0001e0ff  /system/lib/libsurfaceflinger.so\n" +
                "    #17 pc 0001d9ce  /system/lib/libutils.so (android::Looper::pollInner(int)+926)\n" +
                "    #18 pc 0001db73  /system/lib/libutils.so (android::Looper::pollOnce(int, int*, int*, void**)+67)\n" +
                "    #19 pc 0001e561  /system/lib/libsurfaceflinger.so\n" +
                "    #20 pc 00022ce7  /system/lib/libsurfaceflinger.so (android::SurfaceFlinger::run()+39)\n" +
                "    #21 pc 00000ca3  /system/bin/surfaceflinger\n" +
                "    #22 pc 0001365a  /system/lib/libc.so (__libc_init+106)\n" +
                "    #23 pc 00000da8  /system/bin/surfaceflinger\n" +
                "\n" +
                "stack:\n" +
                "         bfcfc110  00000000  \n" +
                "         bfcfc114  b6839270  \n" +
                "         bfcfc118  00000000  \n" +
                "         bfcfc11c  00000000  \n" +
                "         bfcfc120  b68394e0  \n" +
                "         bfcfc124  00000002  \n" +
                "         bfcfc128  00000002  \n" +
                "         bfcfc12c  b75d8185  /system/lib/libutils.so (android::RefBase::incStrong(void const*) const+53)\n" +
                "         bfcfc130  b6839270  \n" +
                "         bfcfc134  bfcfc1e8  [stack]\n" +
                "         bfcfc138  00000002  \n" +
                "         bfcfc13c  a6265c06  \n" +
                "         bfcfc140  b7467d88  /system/lib/libui.so\n" +
                "         bfcfc144  00000000  \n" +
                "         bfcfc148  b6867140  \n" +
                "         bfcfc14c  b745a639  /system/lib/libui.so (android::Fence::waitForever(char const*)+41)\n" +
                "    #00  bfcfc150  b683af18  \n" +
                "         bfcfc154  bfcfc1e8  [stack]\n" +
                "         bfcfc158  00000000  \n" +
                "         bfcfc15c  00000000  \n" +
                "         bfcfc160  00000000  \n" +
                "         bfcfc164  b683af18  \n" +
                "         bfcfc168  b75ec9c4  /system/lib/libutils.so\n" +
                "         bfcfc16c  b75d8285  /system/lib/libutils.so (android::RefBase::weakref_type::decWeak(void const*)+37)\n" +
                "         bfcfc170  00000000  \n" +
                "         bfcfc174  00000000  \n" +
                "         bfcfc178  00000000  \n" +
                "         bfcfc17c  00000000  \n" +
                "         bfcfc180  b7642968  /system/lib/libsurfaceflinger.so\n" +
                "         bfcfc184  bfcfc1e8  [stack]\n" +
                "         bfcfc188  b6867140  \n" +
                "         bfcfc18c  b7622b87  /system/lib/libsurfaceflinger.so";

//        StringBuilder builder = ExecCommands.execCommands("su");
//        Log.e(Main.LOGTAG, builder.toString());

        StringBuffer buffer = ExecCommands.execCommands("chmod 777 /data/tombstones/");
        Log.e(Main.LOGTAG, buffer.toString());

        File file1 = new File("/data/tombstones/tombstone_00");
        if (file1.exists()) {
            file1.createNewFile();
        }

        FileOutputStream outStream = new FileOutputStream(file1);
        outStream.write(content.getBytes());
        outStream.close();

        File file2 = new File("/data/tombstones/tombstone_01");
        if (file2.exists()) {
            file2.createNewFile();
        }
        FileOutputStream outStream2 = new FileOutputStream(file2);
        outStream2.write(content.getBytes());
        outStream2.close();
    }
}
