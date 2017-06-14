package live.itrip.client.device;

import live.itrip.client.common.App;

/**
 * Created by Feng on 2017/6/13.
 */
public class AdbCmdFormat {
    /**
     *
     */
    public static String SERIAL = "-s";


    /**
     * 格式化adb shell指令
     *
     * @param req
     * @return
     */
    public static String formAdbShellRequest(String serialNumber, String req) {
        String cmd = String.format("%s %s \"%s\" shell %s",
                App.getAdbPath(), SERIAL, serialNumber, req);
        return cmd;
    }

    /**
     * 格式化adb指令
     *
     * @param req
     * @return
     */
    public static String formAdbRequest(String serialNumber, String req) {
        String cmd = String.format("%s %s \"%s\" %s",
                App.getAdbPath(), SERIAL, serialNumber, req);
        return cmd;
    }

    /**
     * 发送adb 请求
     *
     * @param req
     * @return
     */
    public static String formAdbRequest(String req) {
        String cmd = String.format("%s %s", App.getAdbPath(), req);
        return cmd;
    }
}
