package live.itrip.client.device;

import com.android.ddmlib.IShellOutputReceiver;
import live.itrip.client.bean.Message;

/**
 * Created by Feng on 2017/6/13.
 */
public interface IDeviceShell {

    /**
     * @return
     */
    Message getAgentClassPath();


    /***
     * 安装代理程序
     *
     * @return
     */
    Message installAgent();

    /**
     * 启动Agent中的Socket Server.
     *
     * @param complete 是否完全重新启动，重新安装Agent，如果端口被占用将使用新的端口。
     * @return 执行的信息。
     */
    Message startAgentServer(boolean complete);

    /**
     * 创建端口映射
     *
     * @param localPort
     * @param remotePort
     * @return
     */
    Message createForward(int localPort, int remotePort);

    /**
     * 删除端口映射
     *
     * @param localPort
     * @param remotePort
     * @return
     */
    Message removeForward(int localPort, int remotePort);

    /**
     * exec adb shell commands
     *
     * @param command
     * @param rcvr
     * @return
     */
    Message executeShellCommand(String command, IShellOutputReceiver rcvr);

    /**
     * exec adb commands
     *
     * @param command
     * @param rcvr
     * @param timeout
     * @return
     */
    Message executeCommand(String command, IShellOutputReceiver rcvr, int timeout);
}
