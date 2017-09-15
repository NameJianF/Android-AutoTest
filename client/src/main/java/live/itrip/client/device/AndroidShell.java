package live.itrip.client.device;

import com.android.ddmlib.*;
import live.itrip.client.bean.Message;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.common.ErrorCode;
import live.itrip.client.util.Logger;

import java.io.IOException;

/**
 * Created by Feng on 2017/6/13.
 */
public class AndroidShell implements IDeviceShell {
    private DeviceInfo deviceInfo;

    public AndroidShell(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public Message installAgent(String appPath, boolean reinstall) {
        Logger.info("Install Agent: " + appPath);
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
    public Message unInstallAgent(String pkg) {
        Message msg = new Message();
        try {
            String retContent = this.deviceInfo.getDevice().uninstallPackage(pkg);
            msg.setContent(retContent);
        } catch (InstallException e) {
            e.printStackTrace();
        }
        Logger.info(String.format("Uninstall finished, status: %s, content: %s", msg.getCode(), msg.getContent()));
        return msg;
    }

    @Override
    public void startAgentMainClass(String command, IShellOutputReceiver rcvr) {
        executeShellCommand(command, rcvr);
    }

    @Override
    public void createForward(int localPort, String remotePort) throws ShellCommandUnresponsiveException {
        try {
            String command = DirectoryService.getAdbPath() + " forward tcp:53516 tcp:53516";
           String content = AdbCmdExecutor.executeCommand(DirectoryService.getBasePath(), command, 5000);
            Logger.error(content);
//            if (device != null && device.isOnline()) {
//                device.c.createForward(localPort, remotePort, IDevice.DeviceUnixSocketNamespace.ABSTRACT);
//            } else {
//                Logger.error("Device is not online.");
//            }
        } catch (IOException e) {
            Logger.error("create forward  error.", e);
        }
    }


    @Override
    public Message executeShellCommand(String command, IShellOutputReceiver rcvr) {
        String cmd = AdbCmdFormat.formatAdbShellCommand(this.deviceInfo.getDevice().getSerialNumber(), command);
        Message msg = new Message();
        try {
            IDevice device = deviceInfo.getDevice();
            if (device != null && device.isOnline()) {
                device.executeShellCommand(command, rcvr);
                msg.setCode(ErrorCode.SUCCESS);
            }
        } catch (TimeoutException | AdbCommandRejectedException | com.android.ddmlib.ShellCommandUnresponsiveException | IOException e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(cmd, e);
        }
        return msg;
    }

}
