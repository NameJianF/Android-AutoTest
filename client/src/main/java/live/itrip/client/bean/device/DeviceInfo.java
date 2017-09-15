package live.itrip.client.bean.device;

import com.android.ddmlib.IDevice;

/**
 * Created by Feng on 2017/6/13.
 */
public class DeviceInfo {
    public static int AGENT_HTTP_SERVER_PORT = 53516;

    private IDevice device;

//    private int agentSocketPort = 53517;
//    private int agentAsyncServerPort = 53518;

    public DeviceInfo(IDevice device) {
        this.device = device;
    }

    public IDevice getDevice() {
        return device;
    }

    public void setDevice(IDevice device) {
        this.device = device;
    }
}
