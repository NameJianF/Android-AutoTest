package live.itrip.client.device;

import live.itrip.client.service.DirectoryService;

/**
 * Created by Feng on 2017/6/13.
 */
class AdbCmdFormat {
    /**
     *
     */
    private final static String SERIAL = "-s";


    /**
     * 格式化adb shell指令
     *
     * @param serialNumber
     * @param command
     * @return
     */
    static String formatAdbShellCommand(String serialNumber, String command) {
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
//    public static String formatAdbCommand(String serialNumber, String command) {
//        String cmd = String.format("%s %s \"%s\" %s",
//                DirectoryService.getAdbPath(), SERIAL, serialNumber, command);
//        return cmd;
//    }

}
