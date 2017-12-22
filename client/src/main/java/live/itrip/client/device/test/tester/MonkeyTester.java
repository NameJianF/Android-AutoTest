package live.itrip.client.device.test.tester;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import live.itrip.client.bean.Message;
import live.itrip.client.bean.apk.ApkInfo;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.common.ErrorCode;
import live.itrip.client.device.AaptCmdExecutor;
import live.itrip.client.device.test.config.MonkeyConfig;
import live.itrip.client.service.DirectoryService;
import live.itrip.client.util.FileUtils;
import live.itrip.client.util.Logger;
import live.itrip.client.util.ThreadExecutor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * Description: 执行 monkey 测试
 *
 * @author JianF
 * Date:  2017/11/9
 * Time:  15:03
 * Modify:
 */
public class MonkeyTester extends BaseTester implements ITester {
    private static final String SCRIPT_FILE_PATH = "/sdcard/itrip/monkey/test.script";
    private static MonkeyTester instance;

    private MonkeyTester(DeviceInfo deviceInfo) {
        this.setDeviceInfo(deviceInfo);
    }

    /**
     * 单例模式，同步处理
     *
     * @return MonkeyTester
     */
    public static MonkeyTester getInstance(DeviceInfo deviceInfo) {
        if (instance == null) {
            synchronized (MonkeyTester.class) {
                if (instance == null) {
                    instance = new MonkeyTester(deviceInfo);
                }
            }
        }
        return instance;
    }

    @Override
    public Message execTest(String appPath) {
        // 设置设备状态为执行脚本测试
        getDeviceInfo().setTesting(true);

        ThreadExecutor.execute(() -> {

            Message message;

            // 1. aapt 解析apk
            ApkInfo apkInfo = analyzeApp(appPath);
            if (apkInfo == null) {
                // 解析失败
                return;
            }
            // 2. check device
            message = checkDevice(apkInfo);
            if (message.getCode() != ErrorCode.SUCCESS) {
                return;
            }
            // 3. install
            message = installApp(appPath);
            if (message.getCode() != ErrorCode.SUCCESS) {
                return;
            }
            // 4. 不需要启动app

            // 5. testing
            message = testing(apkInfo.getPackageName());
            if (message.getCode() != ErrorCode.SUCCESS) {
                return;
            }
            // 6. uninstal App
            message = uninstalApp(apkInfo.getPackageName());
            if (message.getCode() != ErrorCode.SUCCESS) {
                return;
            }
            // 7. report
            message = report();
            if (message.getCode() != ErrorCode.SUCCESS) {
                return;
            }
            // 8. clear datas
            message = clearDatas();
            if (message.getCode() != ErrorCode.SUCCESS) {
                return;
            }

            // 设置设备状态为 未 执行脚本测试
            getDeviceInfo().setTesting(false);
        });

        return null;
    }

    @Override
    protected ApkInfo analyzeApp(String appPath) {
        this.getClientLogListener().writeClientLog("Analyze app ...");
        ApkInfo apkInfo = AaptCmdExecutor.getInstance().getApkInfo(appPath);
        if (apkInfo != null) {
            this.getAppAnalyzeListener().showToWindow(apkInfo);
        }
        return apkInfo;
    }

    @Override
    protected Message checkDevice(ApkInfo apkInfo) {
        // TODO 添加业务逻辑代码
        Message message = new Message();
        message.setCode(ErrorCode.SUCCESS);
        return message;
    }

    @Override
    protected Message installApp(String appPath) {
        this.getClientLogListener().writeClientLog("Install app ...");
        return this.getDeviceInfo().installApk(appPath, true);
    }

    @Override
    protected Message startApp(String packageName, String activity) {
        return null;
    }

    /**
     * 执行monkey 命令运行monkey测试:
     * adb shell monkey -p com.simple.apptestarch --ignore-crashes --ignore-timeouts --ignore-native-crashes --pct-touch 40 --pct-motion 25 --pct-appswitch 10 --pct-rotation 5 -s 12358 -v -v -v --throttle 500 1000 2>~/monkey_error.txt 1>~/monkey_log.txt
     * 参数：
     * com.simple.apptestarch 应用包名
     * 1000代表事件的数量
     *
     * @return Message
     */
    @Override
    protected Message testing(String packageName) {
        this.getClientLogListener().writeClientLog(" App testing ...");

        Message message = new Message();
        MonkeyConfig testConfig = (MonkeyConfig) this.getTestConfig();
        String command = null;
        StringBuffer buffer = null;
        if (MonkeyConfig.EXEC_TYPE_COMMAND == testConfig.getExecType()) {
            // execute monkey command
            command = this.getCommand(testConfig, packageName);
            if (StringUtils.isNotEmpty(command)) {
                buffer = this.getDeviceInfo().executeMonkeyCommand(command);
                message.setCode(ErrorCode.SUCCESS);
            }
        } else if (MonkeyConfig.EXEC_TYPE_SCRIPT == testConfig.getExecType()) {
            // exec script file
            // TODO [--setup scriptfile]
            // TODO [--port port]
            if (testConfig.getScriptfile() != null && testConfig.getScriptfile().size() > 0) {
                StringBuilder builder = new StringBuilder(" monkey");
                for (String file : testConfig.getScriptfile()) {
                    if (StringUtils.isNotEmpty(file)) {
                        try {
                            // TODO Push to device
                            this.getDeviceInfo().pushFile(file, SCRIPT_FILE_PATH);
                            builder.append(" -f ").append(SCRIPT_FILE_PATH);
                        } catch (TimeoutException | AdbCommandRejectedException | SyncException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 执行1次
                builder.append(" 1");
                command = builder.toString();
                if (StringUtils.isNotEmpty(command)) {
                    buffer = this.getDeviceInfo().executeMonkeyScript(command);
                    message.setCode(ErrorCode.SUCCESS);
                }
            }
        }

        // write to file
        String log = DirectoryService.getMonkeyLogFilePath(packageName);
        try {
            FileUtils.stringBufferWrite2File(buffer, log);
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }

        return message;
    }

    @Override
    protected Message uninstalApp(String packageName) {
        this.getClientLogListener().writeClientLog("Uninstall app ...");
        return this.getDeviceInfo().uninstallApk(packageName);
    }

    @Override
    protected Message report() {
        this.getClientLogListener().writeClientLog("Report Test Results ...");

        // TODO 添加业务逻辑代码
        Message message = new Message();
        message.setCode(ErrorCode.SUCCESS);
        return message;
    }

    @Override
    protected Message clearDatas() {
        this.getClientLogListener().writeClientLog("Clear test datas ...");

        // TODO 添加业务逻辑代码
        Message message = new Message();
        message.setCode(ErrorCode.SUCCESS);
        return message;
    }

    private String getCommand(MonkeyConfig testConfig, String packageName) {
        StringBuilder builder = new StringBuilder(" monkey -p ");
        builder.append(packageName);
        // TODO param: [-c MAIN_CATEGORY [-c MAIN_CATEGORY] ...]

        // 选项
        if (testConfig.isDbgNoEvents()) {
            builder.append(" --dbg-no-events");
        }
        if (testConfig.isHprof()) {
            builder.append(" --hprof");
        }
        if (testConfig.isIgnoreCrashes()) {
            builder.append(" --ignore-crashes");
        }
        if (testConfig.isIgnoreNavtiveCrashes()) {
            builder.append(" --ignore-native-crashes");
        }
        if (testConfig.isIgnoreTimeouts()) {
            builder.append(" --ignore-timeouts");
        }
        if (testConfig.isIgnoreSecurityExceptions()) {
            builder.append(" --ignore-security-exceptions");
        }
        if (testConfig.isKillProcessAfterError()) {
            builder.append(" --kill-process-after-error");
        }
        if (testConfig.isMonitorNativeCrashes()) {
            builder.append(" --monitor-native-crashes");
        }

        // 参数
        if (testConfig.getPctTouch() != null) {
            builder.append(" --pct-touch ").append(testConfig.getPctTouch());
        }
        if (testConfig.getPctMotion() != null) {
            builder.append(" --pct-motion ").append(testConfig.getPctMotion());
        }
        if (testConfig.getPctTrackBall() != null) {
            builder.append(" --pct-trackball ").append(testConfig.getPctTrackBall());
        }
        if (testConfig.getPctSysKeys() != null) {
            builder.append(" --pct-syskeys ").append(testConfig.getPctSysKeys());
        }
        if (testConfig.getPctNav() != null) {
            builder.append(" --pct-nav ").append(testConfig.getPctNav());
        }
        if (testConfig.getPctMajorNav() != null) {
            builder.append(" --pct-majornav ").append(testConfig.getPctMajorNav());
        }
        if (testConfig.getPctAppSwitch() != null) {
            builder.append(" --pct-appswitch ").append(testConfig.getPctAppSwitch());
        }
        // TODO [--pct-flip PERCENT]

        if (testConfig.getPctAnyEvent() != null) {
            builder.append(" --pct-anyevent ").append(testConfig.getPctAnyEvent());
        }
        // TODO [--pct-pinchzoom PERCENT]
        // TODO [--pct-permission PERCENT]
        // TODO [--pkg-blacklist-file PACKAGE_BLACKLIST_FILE]
        // TODO [--pkg-whitelist-file PACKAGE_WHITELIST_FILE]

        if (testConfig.isWaitDbg()) {
            builder.append(" --wait-dbg");
        }

        if (testConfig.getSeed() != null) {
            builder.append(" -s ").append(testConfig.getSeed());
        }
        // 最详细日志信息
        builder.append(" -v -v -v");

        if (testConfig.getThrottle() != null) {
            builder.append(" --throttle ").append(testConfig.getThrottle());
        }

        // 发送事件总数
        if (testConfig.getEventCount() > 0) {
            builder.append(" ").append(testConfig.getEventCount());
        }

        /**
         // 测试代码
         String error = DirectoryService.getMonkeyErrorFilePath(packageName);
         File file = new File(error);
         if (!file.exists()) {
         file.getParentFile().mkdirs();
         }
         String log = DirectoryService.getMonkeyLogFilePath(packageName);
         builder.append(" 2>");
         builder.append(error);
         builder.append(" 1>");
         builder.append(log);
         **/
        // TODO [--randomize-throttle]
        // TODO [--profile-wait MILLISEC]
        // TODO [--device-sleep-time MILLISEC]
        // TODO [--randomize-script]
        // TODO [--script-log]
        // TODO [--bugreport]
        // TODO [--periodic-bugreport]
        // TODO [--permission-target-system]

        return builder.toString();
    }
}
