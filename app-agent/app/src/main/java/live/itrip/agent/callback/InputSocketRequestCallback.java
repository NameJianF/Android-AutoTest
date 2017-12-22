package live.itrip.agent.callback;

import android.graphics.Point;
import android.os.RemoteException;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.KeyCharacterMap;

import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

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

public class InputSocketRequestCallback implements AsyncHttpServer.WebSocketRequestCallback {
    private IRotationWatcher watcher;
    private DataSink webSocketDataSink = null;
    private CompletedCallback webSocketClosedCallback;
    private StdOutDevice currentDevice;

    public InputSocketRequestCallback(StdOutDevice currentDevice) {
        this.currentDevice = currentDevice;
        watcher = new IRotationWatcher.Stub() {
            @Override
            public void onRotationChanged(int rotation) throws RemoteException {
                if (webSocketDataSink != null) {
                    sendDisplayInfo();
                }
            }
        };
        webSocketClosedCallback = new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                LogUtils.i("Websocket closed...");
                Main.looper.quit();
            }
        };
    }

    @Override
    public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
        if (webSocketDataSink != null) {
            webSocketDataSink.setClosedCallback(null);
        }
        LogUtils.i("input websocket onConnected...");

        final KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(-1);
        webSocketDataSink = webSocket;
        webSocketDataSink.setClosedCallback(webSocketClosedCallback);
        try {
            webSocket.setStringCallback(WebSocketInputHandler.createWebSocketHandler(
                    InternalApi.getInjectInputEventMethod()
                    , InternalApi.getWindowManager()
                    , InternalApi.getInputManager()
                    , keyCharacterMap
                    , InternalApi.getPowerManager(), currentDevice));
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        try {

            watcher.onRotationChanged(0);
        } catch (RemoteException e) {
            throw new AssertionError(e);
        }
    }

    private void sendDisplayInfo() {
        if (webSocketDataSink != null) {
            Point displaySize = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
            JSONObject json = new JSONObject();
            try {
                json.put("type", "displaySize");
                json.put("screenWidth", displaySize.x);
                json.put("screenHeight", displaySize.y);
                json.put("nav", Main.hasNavBar());
                WebSocketInputHandler.sendEvent(webSocketDataSink, json);
            } catch (JSONException ignored) {
            }
            WebSocketInputHandler.sendEncodeSize(webSocketDataSink);
        }
//        encodeSizeThrottle.postThrottled(null);
    }
}
