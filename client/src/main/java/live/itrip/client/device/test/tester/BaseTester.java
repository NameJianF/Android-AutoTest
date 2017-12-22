package live.itrip.client.device.test.tester;

import live.itrip.client.bean.Message;
import live.itrip.client.bean.apk.ApkInfo;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.controller.IAppAnalyzeListener;
import live.itrip.client.controller.IClientLogListener;
import live.itrip.client.device.test.config.ITestConfig;

/**
 * Created by IntelliJ IDEA.
 * Description:
 *
 * @author JianF
 * Date:  2017/11/9
 * Time:  15:23
 * Modify:
 */
public abstract class BaseTester {
    private DeviceInfo deviceInfo;
    private ITestConfig testConfig;
    private IClientLogListener clientLogListener;
    private IAppAnalyzeListener appAnalyzeListener;

    /**
     * 解析app
     *
     * @param appPath app path
     * @return apk info
     */
    protected abstract ApkInfo analyzeApp(String appPath);

    /**
     * 检查设备是否符合app要求
     *
     * @return Message
     */
    protected abstract Message checkDevice(ApkInfo apkInfo);

    /**
     * 安装app
     *
     * @return Message
     */
    protected abstract Message installApp(String appPath);

    /**
     * 启动app
     *
     * @return Message
     */
    protected abstract Message startApp(String packageName, String activity);

    /**
     * 执行脚本测试
     *
     * @return Message
     */
    protected abstract Message testing(String packageName);


    /**
     * 卸载app
     *
     * @return Message
     */
    protected abstract Message uninstalApp(String packageName);

    /**
     * 生成测试报告
     *
     * @return Message
     */
    protected abstract Message report();

    /**
     * 清理相关数据
     *
     * @return Message
     */
    protected abstract Message clearDatas();


    public ITestConfig getTestConfig() {
        return testConfig;
    }

    public void setTestConfig(ITestConfig testConfig) {
        this.testConfig = testConfig;
    }

    DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public IClientLogListener getClientLogListener() {
        return clientLogListener;
    }

    public void setClientLogListener(IClientLogListener clientLogListener) {
        this.clientLogListener = clientLogListener;
    }

    public IAppAnalyzeListener getAppAnalyzeListener() {
        return appAnalyzeListener;
    }

    public void setAppAnalyzeListener(IAppAnalyzeListener appAnalyzeListener) {
        this.appAnalyzeListener = appAnalyzeListener;
    }
}
