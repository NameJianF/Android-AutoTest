package live.itrip.agent.callback;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import live.itrip.agent.Main;
import live.itrip.agent.util.InternalApi;

/**
 * Created on 2017/12/4.
 *
 * @author Feng
 *         Description :
 *         Update :
 */

public class ImeSocketRequestCallback implements AsyncHttpServer.WebSocketRequestCallback {
    private static final String STRING_BIND = "bind";
    private static final String STRING_UNBIND = "unbind";

    @Override
    public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
        Method broadcastIntent = null;
        try {
            broadcastIntent = InternalApi.getBroadcastIntent();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (broadcastIntent == null) {
            webSocket.close();
            return;
        }

        webSocket.setStringCallback(new WebSocket.StringCallback() {
            @Override
            public void onStringAvailable(String s) {
                if (STRING_BIND.equals(s)) {
                    Main.isImeRunning = true;
                } else if (STRING_UNBIND.equals(s)) {
                    Main.isImeRunning = false;
                }
            }
        });
        webSocket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                Main.isImeRunning = false;
            }
        });
    }
}
