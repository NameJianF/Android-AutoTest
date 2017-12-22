package live.itrip.agent.service;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket;

import java.util.UUID;

import live.itrip.agent.R;


/**
 * Created by Feng on 2017/9/29.
 */

public class IMEService extends InputMethodService {
    private static final int NOTIFICATION_ID = 101;

    BroadcastReceiver receiver;
    String switchAction = ("live.itrip.agent.IMEService." + UUID.randomUUID().toString() + ".SWITCH_KEYBOARD");
    WebSocket ws;

    private void disableSelf() {
        if (getCurrentInputBinding() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            IBinder token = getWindow().getWindow().getAttributes().token;
            if (token != null) {
                imm.switchToLastInputMethod(token);
            } else {
                imm.showInputMethodPicker();
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ((InputMethodManager) IMEService.this.getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
            }
        };
        registerReceiver(this.receiver, new IntentFilter(this.switchAction));
        connectSocket();
    }

    void connectSocket() {
        if (this.ws != null) {
            this.ws.setClosedCallback(null);
            this.ws.close();
            this.ws = null;
        }
        AsyncHttpClient.getDefaultInstance().websocket("http://localhost:53516/ime", "ime-protocol", new WebSocketConnectCallback() {
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    IMEService.this.disableSelf();
                    return;
                }
                IMEService.this.ws = webSocket;
                IMEService.this.ws.setClosedCallback(new CompletedCallback() {
                    public void onCompleted(Exception ex) {
                        Log.i("IMEService", "disabling self due to socket disconnect");
                        IMEService.this.disableSelf();
                    }
                });
                if (IMEService.this.getCurrentInputBinding() == null) {
                    IMEService.this.ws.send("unbind");
                } else {
                    IMEService.this.ws.send("bind");
                }
            }
        });
    }

    public void onDestroy() {
        hideNotification();
        unregisterReceiver(this.receiver);
        if (this.ws != null) {
            this.ws.close();
        }
        Log.i("IMEService", "disabling self due to destroy");
        disableSelf();
        super.onDestroy();
    }

    private static void sendKeyEvent(InputConnection connection, int keyCode, boolean shift) {
        long now = SystemClock.uptimeMillis();
        int meta = shift ? 1 : 0;
        connection.sendKeyEvent(new KeyEvent(now, now, 0, keyCode, 0, meta, -1, 0, 0, InputDeviceCompat.SOURCE_KEYBOARD));
        connection.sendKeyEvent(new KeyEvent(now, now, 1, keyCode, 0, meta, -1, 0, 0, InputDeviceCompat.SOURCE_KEYBOARD));
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        InputConnection connection;
        if (intent != null && intent.hasExtra("keychar")) {
            String text = intent.getStringExtra("keychar");
            connection = getCurrentInputConnection();
            if (connection != null) {
                connection.commitText(text, 1);
            }
        }
        if (intent != null && intent.hasExtra("keycode")) {
            int keycode = intent.getIntExtra("keycode", 0);
            boolean shift = intent.getBooleanExtra("shift", false);
            connection = getCurrentInputConnection();
            if (connection != null) {
                sendKeyEvent(connection, keycode, shift);
            }
        }
        if (intent != null && intent.hasExtra("connect")) {
            connectSocket();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID,
                new Builder(this)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_stat_hardware_keyboard)
                        .setContentText(getString(R.string.tap_to_switch))
                        .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(this.switchAction), 0))
                        .setContentTitle(getString(R.string.agent_hiding_keyboard))
                        .build());
    }

    private void hideNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }

    public void onBindInput() {
        super.onBindInput();
        showNotification();
        if (this.ws != null) {
            this.ws.send("bind");
        }
    }

    public void onUnbindInput() {
        super.onUnbindInput();
        hideNotification();
        if (this.ws != null) {
            this.ws.send("unbind");
        }
    }
}
