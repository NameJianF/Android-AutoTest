package live.itrip.client.handler;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.common.InputType;
import live.itrip.client.util.Logger;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 处理鼠标事件
 */
public class MouseEventHandler {
    private DeviceInfo deviceInfo;
    private double rate = 1; // 图片与设备屏幕比率
    private long timeMousePress = 0L;

    public MouseEventHandler(DeviceInfo deviceInfo, double imageWidth) {
        this.deviceInfo = deviceInfo;
        rate = imageWidth / deviceInfo.getScreenWidth();
    }

    public void onMouseClicked(MouseEvent event) {
        Logger.debug(String.format("Click(X:%s,Y:%s)", event.getX(), event.getY()));
    }

    public void onMouseDragged(MouseEvent event) {
//        Logger.debug(String.format("MouseDragged(X:%s,Y:%s)", event.getX(), event.getY()));
    }

    public void onMouseMoved(MouseEvent event) {
        float x = (float) (event.getX() / rate);
        float y = (float) (event.getY() / rate);
        long downDelta = this.timeMousePress;
        this.sendMouseMove(x, y, downDelta);
    }

    public void onMousePressed(MouseEvent event) {
        timeMousePress = System.currentTimeMillis();
        float x = (float) (event.getX() / rate);
        float y = (float) (event.getY() / rate);
        this.sendMouseDown(x, y);
    }

    public void onMouseReleased(MouseEvent event) {
        float x = (float) (event.getX() / rate);
        float y = (float) (event.getY() / rate);
        long downDelta = this.timeMousePress;
        this.sendMouseUp(x, y, downDelta);
    }

    public void onScroll(ScrollEvent scrollEvent) {

        float x = (float) (scrollEvent.getX() / rate);
        float y = (float) (scrollEvent.getY() / rate);
        double deltaX = scrollEvent.getDeltaX() / rate;
        double deltaY = scrollEvent.getDeltaY() / rate;
        this.sendScroll(x, y, deltaX, deltaY);
    }

    private void sendWakeUp() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.WAKEUP);
        this.send(jSONObject);
    }

    private void sendHome() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.HOME);
        this.send(jSONObject);
    }

    private void sendBack() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.BACK);
        this.send(jSONObject);
    }

    private void sendMenu() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.MENU);
        this.send(jSONObject);
    }

    private void sendRotate() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.ROTATE);
        this.send(jSONObject);
    }

    private void sendMouseMove(float x, float y, long downDelta) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.MOUSEMOVE);
        jSONObject.put("clientX", x);
        jSONObject.put("clientY", y);
        jSONObject.put("downDelta", downDelta);
        this.send(jSONObject);
    }

    private void sendMouseUp(float x, float y, long downDelta) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.MOUSEUP);
        jSONObject.put("clientX", x);
        jSONObject.put("clientY", y);
        jSONObject.put("downDelta", downDelta);
        this.send(jSONObject);
    }

    private void sendMouseDown(float x, float y) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.MOUSEDOWN);
        jSONObject.put("clientX", x);
        jSONObject.put("clientY", y);
        this.send(jSONObject);
    }

    private void sendScroll(float x, float y, double deltaX, double deltaY) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", InputType.SCROLL);
        jSONObject.put("clientX", x);
        jSONObject.put("clientY", y);
        jSONObject.put("deltaX", deltaX);
        jSONObject.put("deltaY", deltaY);
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

    public void BackClick() {
        this.sendBack();
    }

    public void HomeClick() {
        this.sendHome();
    }

    public void MenuClick() {
        this.sendMenu();
    }

}
