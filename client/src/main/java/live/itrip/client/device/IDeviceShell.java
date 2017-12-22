package live.itrip.client.device;

import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import live.itrip.client.bean.Message;

/**
 * Created on 2017/6/13.
 *
 * @author JianF
 */
public interface IDeviceShell {

    /**
     * install agent
     *
     * @param agentPath agent path
     * @param reinstall is reinstall
     * @return Message
     */
    Message installAgent(String agentPath, boolean reinstall);

    /**
     * uninstall agent
     *
     * @param packageName agent package name
     * @return Message
     */
    Message uninstallAgent(String packageName);

    /**
     * install app
     *
     * @param appPath   app path
     * @param reinstall is reinstall
     * @return Message
     */
    Message installApk(String appPath, boolean reinstall);

    /**
     * uninstall app
     *
     * @param packageName package name
     * @return Message
     */
    Message uninstallApk(String packageName);

    /**
     * start app activity
     *
     * @param packageName package name
     * @param activity    activity name
     * @return Message
     */
    Message startActivity(String packageName, String activity);

    /**
     * start agent main class
     *
     * @param command command
     */
    void startAgentMainClass(String command);

    /**
     * adb create forward tcp
     *
     * @param localPort  local port
     * @param remotePort remote port
     * @throws ShellCommandUnresponsiveException ShellCommandUnresponsiveException
     */
    void createForward(int localPort, String remotePort) throws ShellCommandUnresponsiveException;

    /**
     * execute adb shell command by device
     *
     * @param command  command
     * @param receiver receiver
     * @return Message
     */
    Message executeShellCommand(String command, IShellOutputReceiver receiver);

    /**
     * execute adb shell command by new process
     *
     * @param command command
     * @return Message
     */
    StringBuffer executeCommandByProcess(String command);

}
