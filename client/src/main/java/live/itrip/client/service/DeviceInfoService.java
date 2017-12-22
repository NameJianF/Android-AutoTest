package live.itrip.client.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.common.Config;
import live.itrip.client.controller.MainController;
import live.itrip.client.util.HttpUtils;
import live.itrip.client.util.Logger;
import org.json.JSONObject;

import javafx.scene.control.ListView;

import java.util.Iterator;


public class DeviceInfoService {

    public static void setProfileTure() {
        String deviceUrl = String.format("%s:%s/setGfxOpen", Config.HTTP_URL, Config.PORT_53516);
        String content = HttpUtils.httpGet(deviceUrl);
        Logger.debug("setProfileTure >>> " + content);
    }

    public static ObservableList<String> getDeviceInformations(DeviceInfo deviceInfo) {
        String deviceUrl = String.format("%s:%s/device.json", Config.HTTP_URL, Config.PORT_53516);
        String strJson = HttpUtils.httpGet(deviceUrl);
        ObservableList<String> items = FXCollections.observableArrayList();

        JSONObject jsonObject = new JSONObject(strJson);
        if (jsonObject.has("android.os.Build")) {
            items.add(" ================== android.os.Build =====================");
            JSONObject build = jsonObject.getJSONObject("android.os.Build");
            Iterator<String> sIterator = build.keys();
            while (sIterator.hasNext()) {
                // 获得key
                String key = sIterator.next();
                // 根据key获得value
                String value = build.get(key).toString();
                items.add(String.format("%s : %s", key, value));
            }
        }
        if (jsonObject.has("systemProperties")) {
            items.add(" ================== system properties =====================");
            JSONObject systemProperties = jsonObject.getJSONObject("systemProperties");
            Iterator<String> sIterator = systemProperties.keys();
            while (sIterator.hasNext()) {
                // 获得key
                String key = sIterator.next();
                // 根据key获得value
                String value = systemProperties.get(key).toString();
                items.add(String.format("%s : %s", key, value));
            }
        }
        if (jsonObject.has("cpuinfo")) {
            items.add(" ================== cpuinfo =====================");
            JSONObject cpuinfo = jsonObject.getJSONObject("cpuinfo");
            Iterator<String> sIterator = cpuinfo.keys();
            while (sIterator.hasNext()) {
                // 获得key
                String key = sIterator.next();
                // 根据key获得value
                String value = cpuinfo.get(key).toString();
                items.add(String.format("%s : %s", key, value));
            }
        }
        if (jsonObject.has("display")) {
            JSONObject display = jsonObject.getJSONObject("display");
            deviceInfo.setScreenWidth(display.getInt("screenWidth"));
            deviceInfo.setScreenHeight(display.getInt("screenHeight"));
            deviceInfo.setNavShow(display.getBoolean("nav"));
        }
        return items;
    }
}
