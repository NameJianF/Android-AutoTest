package live.itrip.client.device;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.TimeoutException;
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

    @Override
    public Message installAgent() {
        return null;
    }

    @Override
    public Message getAgentClassPath() {
        return null;
    }

    @Override
    public Message startAgentServer(boolean complete) {
        return null;
    }

    @Override
    public Message createForward(int localPort, int remotePort) {
        return null;
    }

    @Override
    public Message removeForward(int localPort, int remotePort) {
        return null;
    }

    @Override
    public Message executeShellCommand(String command, IShellOutputReceiver rcvr) {
        String cmd = AdbCmdFormat.formAdbShellRequest(this.deviceInfo.getSerialNumber(), command);
        Message msg = new Message();
        try {
            IDevice device = deviceInfo.getDevice();
            device.executeShellCommand(command, rcvr);
            // msg.obj = res;
            msg.setCode(ErrorCode.SUCCESS);
        } catch (TimeoutException e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(cmd, e);
        } catch (AdbCommandRejectedException e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(cmd, e);
        } catch (com.android.ddmlib.ShellCommandUnresponsiveException e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(cmd, e);
        } catch (IOException e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(cmd, e);
        } catch (Exception e) {
            msg.setCode(ErrorCode.EXEC_ERROR);
            msg.setContent(e.getMessage());
            Logger.error(cmd, e);
        }
        return msg;
    }

    @Override
    public Message executeCommand(String command, IShellOutputReceiver rcvr, int timeout) {
        return null;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
