package live.itrip.client.device.test.tester;

import live.itrip.client.bean.Message;
import live.itrip.client.bean.apk.ApkInfo;
import live.itrip.client.bean.device.DeviceInfo;

/**
 * Created by IntelliJ IDEA.
 * Description: 接口定义测试流程
 *
 * @author JianF
 * Date:  2017/11/9
 * Time:  15:13
 * Modify:
 */
public interface ITester {
    /**
     * 执行测试
     *
     * @param appPath app path
     * @return Message
     */
    Message execTest(String appPath);
}
