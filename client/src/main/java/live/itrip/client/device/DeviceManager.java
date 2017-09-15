package live.itrip.client.device;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import live.itrip.client.bean.Message;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.util.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Feng on 2017/6/13.
 */
public class DeviceManager {
    private DeviceInfo deviceInfo;
    private DeviceChangeListener deviceChangeListener = new DeviceChangeListener();

    private static DeviceManager instance;

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
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
                if (deviceInfo == null) {
                    deviceInfo = new DeviceInfo(device);
                }
                Logger.info("device connected : " + device.getSerialNumber());

            }
        }

        @Override
        public void deviceDisconnected(IDevice device) {
            if (device != null) {
                if (deviceInfo != null) {
                    deviceInfo.setDevice(device);
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
