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
     * @param command
     * @return
     */
    public static String formatAdbShellCommand(String serialNumber, String command) {
        String cmd = String.format("%s %s \"%s\" shell %s",
                DirectoryService.getAdbPath(), SERIAL, serialNumber, command);
        return cmd;
    }

    /**
     * 格式化adb指令
     *
     * @param command
     * @return
     */
    public static String formatAdbCommand(String serialNumber, String command) {
        String cmd = String.format("%s %s \"%s\" %s",
                DirectoryService.getAdbPath(), SERIAL, serialNumber, command);
        return cmd;
    }

}
