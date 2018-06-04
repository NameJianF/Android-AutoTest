package live.itrip.agent.callback;

import android.graphics.Point;
import android.os.RemoteException;
import android.view.IRotationWatcher;
import android.view.IWindowManager;

import com.koushikdutta.async.BufferedDataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.lang.reflect.InvocationTargetException;

import live.itrip.agent.EncoderFeeder;
import live.itrip.agent.Main;
import live.itrip.agent.StdOutDevice;
import live.itrip.agent.handler.WebSocketInputHandler;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;
import live.itrip.agent.virtualdisplay.SurfaceControlVirtualDisplayFactory;

/**
 * Created on 2017/12/4.
 *
 * @author Feng
 *         Description :
 *         Update :
 */

public class ScreenLiveRequestCallback implements HttpServerRequestCallback {
    private StdOutDevice currentDevice;

    public ScreenLiveRequestCallback(StdOutDevice currentDevice) {
        this.currentDevice = currentDevice;
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        LogUtils.i("h264 new http connection accepted.");
        try {
            // 点亮设备屏幕
            WebSocketInputHandler.turnScreenOn(InternalApi.getInputManager()
                    , InternalApi.getInjectInputEventMethod()
                    , InternalApi.getPowerManager());
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.getHeaders().set("Access-Control-Allow-Origin", "*");
        response.getHeaders().set("Connection", "close");
        try {
            final StdOutDevice device = writer(new BufferedDataSink(response), InternalApi.getWindowManager());
            response.setClosedCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    LogUtils.i("Connection terminated.");
                    if (ex != null) {
                        LogUtils.e("Error", ex);
                    }
                    device.stop();
                }
            });
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private StdOutDevice writer(BufferedDataSink sink, IWindowManager wm) {
        if (this.currentDevice != null) {
            this.currentDevice.stop();
            this.currentDevice = null;
        }
        Point encodeSize = getEncodeSize();
        SurfaceControlVirtualDisplayFactory factory = new SurfaceControlVirtualDisplayFactory();
        StdOutDevice device = new StdOutDevice(encodeSize.x, encodeSize.y, sink);
        if (Main.resolution != 0.0d) {
//            device.setUseEncodingConstraints(false);
        }
        this.currentDevice = device;
        LogUtils.i("registering virtual display");
        if (device.supportsSurface()) {
            device.registerVirtualDisplay(factory, 0);
        } else {
            LogUtils.i("Using legacy path");
            device.createDisplaySurface();
            final EncoderFeeder feeder = new EncoderFeeder(device.getMediaCodec(), device.getEncodingDimensions().x, device.getEncodingDimensions().y, device.getColorFormat());
            IRotationWatcher watcher = new IRotationWatcher.Stub() {
                @Override
                public void onRotationChanged(int rotation) throws RemoteException {
                    feeder.setRotation(rotation);
                }
            };
            try {
                int displayId = 1;
                wm.watchRotation(watcher, displayId);
            } catch (RemoteException e) {
                LogUtils.e(e.getMessage());
            }

            feeder.feed();
        }
        LogUtils.i("virtual display registered");
        return device;
    }

    private Point getEncodeSize() {
        Point encodeSize = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
        if (Main.resolution == 0.0d) {
            if (encodeSize.x >= 1280 || encodeSize.y >= 1280) {
                encodeSize.x /= 2;
                encodeSize.y /= 2;
            }
            while (true) {
                if (encodeSize.x <= 1280 && encodeSize.y <= 1280) {
                    break;
                }
                encodeSize.x /= 2;
                encodeSize.y /= 2;
            }
        } else {
            encodeSize.x = (int) (((double) encodeSize.x) * Main.resolution);
            encodeSize.y = (int) (((double) encodeSize.y) * Main.resolution);
        }
        return encodeSize;
    }

}
