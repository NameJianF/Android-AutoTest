package live.itrip.client.device;

import live.itrip.client.util.Logger;

import java.io.File;

/**
 * Created by Feng on 2017/6/13.
 */
public class DirectoryService {

    private static String path = System.getProperty("user.dir");
    private static String adbPath;
    private static String aaptPath;
    private static String agentPath;

    public static String getBasePath() {
        return path;
    }

    public static String getAdbPath() {
        adbPath = path + File.separator + "tools/adb.exe";
        Logger.debug("adb path : " + adbPath);
        return adbPath;
    }

    public static String getAaptPath() {
        aaptPath = path + File.separator + "tools/aapt2.exe";
        Logger.debug("aapt path : " + aaptPath);
        return aaptPath;
    }

    public static String getAgentPath() {
        agentPath = path + File.separator + "tools/agent.apk";
        Logger.debug("agent path : " + agentPath);
        return agentPath;
    }
}
