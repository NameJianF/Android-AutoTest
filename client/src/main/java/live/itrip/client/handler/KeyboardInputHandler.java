package live.itrip.client.handler;

import javafx.scene.input.KeyEvent;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.common.InputType;
import live.itrip.client.util.Logger;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author JianF
 * <p>
 * 处理键盘输入
 */
public class KeyboardInputHandler {
    private DeviceInfo deviceInfo;

    public KeyboardInputHandler(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void onKeyPressed(KeyEvent event) {
        Logger.debug(String.format("KeyTyped(EventType:%s,Code:%s)", event.getEventType(), event.getCode()));
    }

    private void sendDelete() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.DELETE);
        this.send(jSONObject);
    }

    private void sendBackspace() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.BACKSPACE);
        this.send(jSONObject);
    }

    private void sendUp() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.UP);
        this.send(jSONObject);
    }

    private void sendDown() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.DOWN);
        this.send(jSONObject);
    }

    private void sendLeft() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.LEFT);
        this.send(jSONObject);
    }

    private void sendRight() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.RIGHT);
        this.send(jSONObject);
    }

    private void sendKeycode(int keycode, boolean shift) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.KEYCODE);
        jSONObject.put("keycode", keycode);
        jSONObject.put("shift", shift);
        this.send(jSONObject);

    }

    private void sendKeychar(String keychar) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.KEYCHAR);
        jSONObject.put("keychar", keychar);

        this.send(jSONObject);
    }

    private void sendBitrate(int bitrate) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.RIGHT);
        jSONObject.put("bitrate", bitrate);
        this.send(jSONObject);
    }

    private void sendSyncFrame() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.SYNC_FRAME);
        this.send(jSONObject);
    }

    private void sendRequestSync() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.REQUEST_SYNC);
        this.send(jSONObject);
    }

    private void send(JSONObject jSONObject) {
        if (this.deviceInfo != null && this.deviceInfo.getInputWebsocketClient() != null) {
            try {
                this.deviceInfo.getInputWebsocketClient().sendText(jSONObject.toString());
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
            }
        }
    }

}
