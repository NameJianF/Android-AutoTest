package live.itrip.client.device;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.service.DirectoryService;
import live.itrip.client.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Feng on 2017/6/13.
 */
public class DeviceManager {
    private List<DeviceInfo> deviceList = new ArrayList<>();
    private DeviceChangeListener deviceChangeListener = new DeviceChangeListener();
    private static DeviceManager instance;

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public DeviceInfo getDeviceInfo(int index) {
        return deviceList.get(index);
    }

    public List<DeviceInfo> getDeviceList() {
        return deviceList;
    }

    public void start() {
        AndroidDebugBridge.init(false);
        String adbPath = DirectoryService.getAdbPath();
        AndroidDebugBridge.createBridge(adbPath, false);
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
        Logger.info("device manager started.......");
    }

    /**
     * 多设备列表监听器
     *
     * @author
     */
    private final class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

        @Override
        public void deviceConnected(IDevice device) {
            // device.isOnline() maybe false
            if (device.isOnline()) {
                for (DeviceInfo info : deviceList) {
                    if (info.getDevice().getSerialNumber().equalsIgnoreCase(device.getSerialNumber())) {
                        // 已存在,remove
                        deviceList.remove(info);
                        break;
                    }
                }
                deviceList.add(new DeviceInfo(device));
                Logger.info("device connected : " + device.getSerialNumber());
            }
        }

        @Override
        public void deviceDisconnected(IDevice device) {
            // 设备断开
            if (device != null) {
                for (DeviceInfo info : deviceList) {
                    if (info.getDevice().getSerialNumber().equalsIgnoreCase(device.getSerialNumber())) {
                        // 已存在,remove
                        deviceList.remove(info);
                        break;
                    }
                }
                Logger.info("device disconnected : " + device.getSerialNumber());

            }
        }

        @Override
        public void deviceChanged(IDevice device, int changeMask) {
            if ((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
                deviceConnected(device);
            }
            Logger.info(String.format("device status: %s " + device.getState()));
        }
    }

}
