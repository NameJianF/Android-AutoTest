package live.itrip.client.device;

import com.android.ddmlib.*;
import live.itrip.client.bean.Message;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.common.Config;
import live.itrip.client.common.ErrorCode;
import live.itrip.client.device.receiver.DefaultShellOutputReceiver;
import live.itrip.client.service.DirectoryService;
import live.itrip.client.util.Logger;
import live.itrip.client.util.ThreadExecutor;

import java.io.IOException;

/**
 * Created by Feng on 2017/6/13.
 *
 * @author JianF
 */
public class AndroidShell implements IDeviceShell {
    private DeviceInfo deviceInfo;

    public AndroidShell(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public Message installAgent(String agentPath, boolean reinstall) {
        return this.installApk(agentPath, reinstall);
    }

    @Override
    public Message uninstallAgent(String packageName) {
        return uninstallApk(packageName);
    }

    @Override
    public Message installApk(String appPath, boolean reinstall) {
        Logger.info("Install apk: " + appPath);
        Message msg = new Message();
        try {
            this.deviceInfo.getDevice().installPackage(appPath, reinstall);
            msg.setCode(ErrorCode.SUCCESS);
        } catch (InstallException e) {
            e.printStackTrace();
        }
        Logger.info(String.format("Install finished, status: %s, content: %s", msg.getCode(), msg.getContent()));
        return msg;
    }

    @Override
    public Message uninstallApk(String packageName) {
        Message msg = new Message();
        try {
            String retContent = this.deviceInfo.getDevice().uninstallPackage(packageName);
            msg.setCode(ErrorCode.SUCCESS);
            msg.setContent(retContent);
        } catch (InstallException e) {
            e.printStackTrace();
        }
        Logger.info(String.format("Uninstall finished, status: %s, content: %s", msg.getCode(), msg.getContent()));
        return msg;
    }

    @Override
    public Message startActivity(String packageName, String activity) {
        String command = String.format(
                "am start -a android.intent.action.MAIN -n '%s/%s'",
                packageName, activity);
        return executeShellCommand(command, new DefaultShellOutputReceiver());
    }

    @Override
    public void startAgentMainClass(String command) {
        ThreadExecutor.execute(() -> {
            Thread.currentThread().setName("thread-StartAgentMainClass");
            String cmd = AdbCmdFormat.formatAdbShellCommand(this.deviceInfo.getDevice().getSerialNumber(), command);
            try {
                System.err.println("AgentMainClass Shell Command: " + cmd);
                StringBuffer buffer = AdbCmdExecutor.executeCommandStartMain(cmd);
                System.err.println("AgentMainClassClosed : " + buffer.toString());
                this.deviceInfo.onAgentMainClassClosed(buffer.toString());
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void createForward(int localPort, String remotePort) throws ShellCommandUnresponsiveException {
        try {
            this.deviceInfo.getDevice().createForward(Config.PORT_53516, Config.PORT_53516);
        } catch (IOException e) {
            Logger.error("create forward  error.", e);
        } catch (AdbCommandRejectedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * execute adb shell command by device
     *
     * @param command command
     * @param receiver    receiver
     * @return Message
     */
    @Override
    public Message executeShellCommand(String command, IShellOutputReceiver receiver) {
        Logger.debug("exec shell command : " + command);
        Message msg = new Message();
        try {
            IDevice device = deviceInfo.getDevice();
            if (device != null && device.isOnline()) {
                device.executeShellCommand(command, receiver);
                msg.setCode(ErrorCode.SUCCESS);
            }
        } catch (TimeoutException | AdbCommandRejectedException | com.android.ddmlib.ShellCommandUnresponsiveException | IOException e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(command, e);
        }
        return msg;
    }

    @Override
    public StringBuffer executeCommandByProcess(String command) {
        return AdbCmdExecutor.executeCommandByProcess(command);
    }
}
