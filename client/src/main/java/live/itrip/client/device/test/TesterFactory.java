package live.itrip.client.device.test;

import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.controller.IAppAnalyzeListener;
import live.itrip.client.controller.IClientLogListener;
import live.itrip.client.device.test.tester.BaseTester;
import live.itrip.client.device.test.tester.MonkeyTester;

/**
 * Created by IntelliJ IDEA.
 * Description: 创建 Tester
 *
 * @author JianF
 * Date:  2017/11/22
 * Time:  9:53
 * Modify:
 */
public class TesterFactory {
    /**
     * 更加测试类型，创建 Tester
     *
     * @param testType   测试类型
     * @param deviceInfo 设备
     * @return BaseTester
     */
    public static BaseTester createTester(TestTypeEnum testType
            , DeviceInfo deviceInfo
            , IClientLogListener iClientLogListener
            , IAppAnalyzeListener appAnalyzeListener) {
        BaseTester tester = null;
        switch (testType) {
            case Money:
                tester = MonkeyTester.getInstance(deviceInfo);
                break;
            default:
                break;
        }
        if (tester != null) {
            tester.setClientLogListener(iClientLogListener);
            tester.setAppAnalyzeListener(appAnalyzeListener);
        }
        return tester;
    }
}
