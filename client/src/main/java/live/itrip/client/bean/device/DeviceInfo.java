package live.itrip.client.bean.device;

import com.android.ddmlib.IDevice;

/**
 * Created by Feng on 2017/6/13.
 */
public class DeviceInfo {
    private IDevice device;
    private String imei;
    private String serialNumber;
    private int sdkVer;
    private String releaseVer;
    private String brandName;
    private String modelName;
    private String agentClassPath;

    private int agentHttpServerPort = 53516;
    private int agentSocketPort = 53517;
    private int agentAsyncServerPort = 53518;


    public IDevice getDevice() {
        return device;
    }

    public void setDevice(IDevice device) {
        this.device = device;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getSdkVer() {
        return sdkVer;
    }

    public void setSdkVer(int sdkVer) {
        this.sdkVer = sdkVer;
    }

    public String getReleaseVer() {
        return releaseVer;
    }

    public void setReleaseVer(String releaseVer) {
        this.releaseVer = releaseVer;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAgentClassPath() {
        return agentClassPath;
    }

    public void setAgentClassPath(String agentClassPath) {
        this.agentClassPath = agentClassPath;
    }
}
