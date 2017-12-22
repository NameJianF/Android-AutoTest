package live.itrip.client.controller;

import javafx.collections.ObservableList;
import org.json.JSONObject;

/**
 * 设备状态监听
 *
 * @author JianF
 */
public interface IDeviceStatusListener {
    /**
     * 设备初始化
     *
     * @param msg
     */
    void initing(String msg);

    /**
     * 设备初始化完成
     *
     * @param msg
     */
    void inited(String msg);

    /**
     * 获取设备信息
     *
     * @param items
     */
    void getDeviceInfos(ObservableList<String> items);

    /**
     * 获取设备性能信息
     *
     * @param json
     */
    void handPerformanceDatas(JSONObject json);

    void onAgentMainClassClosed();
}
