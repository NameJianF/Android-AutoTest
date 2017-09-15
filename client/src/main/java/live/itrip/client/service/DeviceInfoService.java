package live.itrip.client.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.controller.MainController;
import live.itrip.client.util.HttpUtils;
import org.json.JSONObject;

import javafx.scene.control.ListView;

import java.util.Iterator;


public class DeviceInfoService {

    public static void getDeviceInformations(ListView listView) {
        String deviceUrl = String.format("%s:%s/device.json", MainController.HTTP_URL, DeviceInfo.AGENT_HTTP_SERVER_PORT);
        String strJson = HttpUtils.httpGet(deviceUrl).toString();
        ObservableList items = FXCollections.observableArrayList();

        JSONObject jsonObject = new JSONObject(strJson);
        if (!jsonObject.isNull("android.os.Build")) {
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
        if (!jsonObject.isNull("systemProperties")) {
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
        if (!jsonObject.isNull("cpuinfo")) {
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

        listView.setItems(items);
    }
}
