package live.itrip.client.device;

import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import live.itrip.client.bean.Message;

/**
 * Created by Feng on 2017/6/13.
 */
public interface IDeviceShell {

    Message installAgent(String pkg, boolean reinstall);

    Message unInstallAgent(String pkg);

    void startAgentMainClass(String command, IShellOutputReceiver rcvr);

    void createForward(int localPort, String remotePort) throws ShellCommandUnresponsiveException;

    /**
     * exec adb shell commands
     *
     * @param command
     * @param rcvr
     * @return
     */
    Message executeShellCommand(String command, IShellOutputReceiver rcvr);
}
