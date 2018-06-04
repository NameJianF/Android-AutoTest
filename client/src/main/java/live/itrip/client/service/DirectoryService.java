package live.itrip.client.service;

import java.io.File;

/**
 * Created on 2017/6/13.
 *
 * @author JianF
 */
public class DirectoryService {

    private static String path = System.getProperty("user.dir");
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static String getBasePath() {
        return path;
    }

    public static String getAdbPath() {

        if (OS_NAME.startsWith("mac")) {
            return path + File.separator + "tools" + File.separator + "mac" + File.separator + "adb";
        } else if (OS_NAME.startsWith("linux")) {
            return "adb";
        }

        return path + File.separator + "tools" + File.separator + "adb.exe";

    }

    public static String getAaptPath() {
        if (OS_NAME.startsWith("mac")) {
            return path + File.separator + "tools" + File.separator + "mac" + File.separator + "aapt";
        } else if (OS_NAME.startsWith("linux")) {
            return "aapt";
        }

        return path + File.separator + "tools" + File.separator + "aapt.exe";
    }

    public static String getAgentPath() {
        return path + File.separator + "tools" + File.separator + "agent.apk";
    }

    public static String getMonkeyErrorFilePath(String packageName) {
        return path + File.separator + "test" + File.separator + packageName + File.separator + "monkey" + File.separator + "error.txt";
    }

    public static String getMonkeyLogFilePath(String packageName) {
        return path + File.separator + "test" + File.separator + packageName + File.separator + "monkey" + File.separator + "log.txt";
    }
}
