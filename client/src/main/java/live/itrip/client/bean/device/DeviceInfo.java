package live.itrip.client.bean.device;

import com.android.ddmlib.*;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import live.itrip.client.bean.Message;
import live.itrip.client.common.Config;
import live.itrip.client.common.ErrorCode;
import live.itrip.client.controller.IDeviceStatusListener;
import live.itrip.client.device.AdbCmdExecutor;
import live.itrip.client.device.AndroidShell;
import live.itrip.client.service.DeviceInfoService;
import live.itrip.client.service.DirectoryService;
import live.itrip.client.service.input.InputMethodClient;
import live.itrip.client.service.performance.PerformanceWebsocketClient;
import live.itrip.client.util.H264Decoder;
import live.itrip.client.util.Logger;
import live.itrip.client.util.ThreadExecutor;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.websocket.DeploymentException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;

/**
 * Created on 2017/6/13.
 *
 * @author Feng
 */
public class DeviceInfo {
    private IDevice device;
    private int screenWidth;
    private int screenHeight;
    private boolean navShow = true;

    private AndroidShell androidShell;
    private IDeviceStatusListener deviceStatusListener;
    private boolean performanceRuning = true;
    private InputMethodClient inputWebsocketClient;
    private final static String AGENT_PACKAGE_NAME = "live.itrip.agent";
    private final static String AGENT_CLASS_PATH = "CLASSPATH=/data/app/live.itrip.agent-1/base.apk exec app_process /system/bin live.itrip.agent.Main";
    private final static int LOGCAT_MAX_LINE = 100;

    /*
        private int agentSocketPort = 53517;
        private int agentAsyncServerPort = 53518;
    */

    /**
     * 是否正在执行脚本测试
     */
    private boolean testing = false;

    public DeviceInfo(IDevice device) {
        this.device = device;
    }

    /**
     * 初始化设备
     * 1. 安装 agent.apk
     */
    public void initDevice() {
        if (!this.device.isOnline()) {
            Alert dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setContentText("设备离线状态");
            dialog.showAndWait();
            return;
        }

        deviceStatusListener.initing("device init start .......");

        // init shell
        androidShell = new AndroidShell(this);

        ThreadExecutor.execute(() -> {
            // set Thread Name
            Thread.currentThread().setName("thread-InitDevice");

            Message msg;
            // uninstall agent
            msg = androidShell.uninstallAgent(AGENT_PACKAGE_NAME);
            Logger.debug(String.format("unInstallAgent Code:%s,msg:%s", msg.getCode(), msg.getContent()));

            // install agent
            String agentPath = DirectoryService.getAgentPath();
            msg = androidShell.installAgent(agentPath, true);
            Logger.debug(String.format("installAgent Code:%s,msg:%s", msg.getCode(), msg.getContent()));

            // start main
            Logger.debug("startAgentMainClass...........");
            androidShell.startAgentMainClass(AGENT_CLASS_PATH);
            Logger.debug("startAgentMainClass end...........");

            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Logger.debug("createForward...........");
            try {
                androidShell.createForward(Config.PORT_53516, String.valueOf(Config.PORT_53516));
            } catch (ShellCommandUnresponsiveException e) {
                e.printStackTrace();
            }
            Logger.debug("createForward end...........");

            // TODO >>> 需要先打开gfxinfo,再启动app
            // 打开 gfxinfo 属性：setprop debug.hwui.profile true
            DeviceInfoService.setProfileTure();

            deviceStatusListener.inited("device init success .......");

            // 显示设备信息
            ObservableList<String> items = DeviceInfoService.getDeviceInformations(this);
            deviceStatusListener.getDeviceInfos(items);
        });
    }

    /**
     * 截图
     *
     * @return Image
     */
    public Image screenshot() {
        String imageSource = String.format("%s:%s/screenshot.jpg", Config.HTTP_URL, Config.PORT_53516);
        return new Image(imageSource);
    }

    /**
     * 截图 并保存
     */
    public void saveScreenshot() {
        String imageSource = String.format("%s:%s/screenshot.jpg", Config.HTTP_URL, Config.PORT_53516);
        Image image = new Image(imageSource);
        ThreadExecutor.execute(() -> {
            // set Thread Name
            Thread.currentThread().setName("thread-Screenshot");

            // save to file
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            jfc.showDialog(new JLabel(), "选择");

            File outputFile = null;
            File file = jfc.getSelectedFile();
            if (file != null) {
                if (file.isDirectory()) {
                    System.out.println("文件夹:" + file.getAbsolutePath());
                    outputFile = new File(file.getAbsoluteFile() + File.separator + "screenshot.jpg");
                } else if (file.isFile()) {
                    System.out.println("文件:" + file.getAbsolutePath());
                    outputFile = file;
                }

                BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
                try {
                    ImageIO.write(bImage, "jpg", outputFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void screenLive(ImageView imageView) {
        // image view
        String url = String.format("%s:%s/h264", Config.HTTP_URL, Config.PORT_53516);

        ThreadExecutor.execute(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // set Thread Name
                    Thread.currentThread().setName("thread-ScreenLive");
                    H264Decoder.streamToImageView(url, imageView, 0);
                }
        );
        Logger.debug("threadLive.start:" + System.currentTimeMillis());
    }

    public void performanceInfos(String packageName, int appType) throws IOException {
        performanceRuning = true;
        ThreadExecutor.execute(() -> {
            // set Thread Name
            Thread.currentThread().setName("thread-Performance");

            PerformanceWebsocketClient websocketClient = new PerformanceWebsocketClient("PerformanceService");

            String serviceEndPointAddress = String.format("ws://localhost:%s/infos", Config.PORT_53516);
            Logger.error(serviceEndPointAddress);

            ClientManager client = ClientManager.createClient();
            try {
                client.connectToServer(websocketClient, URI.create(serviceEndPointAddress));
            } catch (DeploymentException | IOException e) {
                e.printStackTrace();
            }
            websocketClient.setMessageCallback(message -> {
                if (StringUtils.isNotEmpty(message)) {
                    deviceStatusListener.handPerformanceDatas(new JSONObject(message));
                }
            });
            while (performanceRuning) {
                try {
                    JSONObject all = new JSONObject();
                    all.put("type", "all");
                    all.put("packageName", packageName);
                    all.put("appType", appType);
                    websocketClient.sendText(all.toString());

                    // 10 秒取一次数据
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopPerformance() {
        performanceRuning = false;
    }

    public void startInputService() {
        inputWebsocketClient = new InputMethodClient("InputService");
        String url = String.format("%s:%s/input", Config.HTTP_URL, Config.PORT_53516);
        Logger.error(url);

        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(inputWebsocketClient, URI.create(url));
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
        inputWebsocketClient.setMessageCallback(message -> {
            if (StringUtils.isNotEmpty(message)) {
                Logger.debug(String.format("input service call back msg >>> %s", message));
            }
        });
    }

    public void stopInputService() {
        if (inputWebsocketClient != null) {
            inputWebsocketClient.onClose();
        }
    }

    public Message installApk(String appPath, boolean reInstall) {
        if (this.androidShell != null) {
            return this.androidShell.installApk(appPath, reInstall);
        }
        return null;
    }

    public Message uninstallApk(String packageName) {
        Message message = null;
        if (this.androidShell != null) {
            message = this.androidShell.uninstallApk(packageName);
        } else {
            message = new Message();
            message.setCode(ErrorCode.EXEC_ERROR);
            message.setContent("object[androidShell] is null");
        }
        return message;
    }

    /**
     * 启动 apk
     *
     * @param packageName
     * @param activity
     */
    public Message startActivity(String packageName, String activity) {
        return this.androidShell.startActivity(packageName, activity);
    }

    /**
     * agent Main.main close
     */
    public void onAgentMainClassClosed(String stringBuffer) {
        deviceStatusListener.onAgentMainClassClosed();
    }

    public void getDeviceLogcat(String tag, ObservableList<ClientLogCatMessage> tableViewLogcatData, TableView tableViewLogcat) {
        if (this.device != null && this.device.isOnline()) {
            LogCatReceiverTask receiverTask = new LogCatReceiverTask(this.device);
            receiverTask.addLogCatListener(list -> {
                for (LogCatMessage message : list) {
                    if (StringUtils.isNotEmpty(tag)) {
                        if (tag.equals(message.getTag())) {
                            addLogcat2TableView(tableViewLogcatData, tableViewLogcat, message);
                        }
                    } else {
                        addLogcat2TableView(tableViewLogcatData, tableViewLogcat, message);
                    }
                }
            });
            ThreadExecutor.execute(receiverTask);
        }
    }

    /**
     * 显示logcat日志到界面
     *
     * @param tableViewLogcatData
     * @param tableViewLogcat
     * @param message
     */
    private void addLogcat2TableView(ObservableList<ClientLogCatMessage> tableViewLogcatData, TableView tableViewLogcat, LogCatMessage message) {
        Platform.runLater(() -> {
            if (tableViewLogcatData.size() > LOGCAT_MAX_LINE) {
                tableViewLogcatData.remove(0);
            }
            tableViewLogcatData.add(new ClientLogCatMessage(message));
            tableViewLogcat.scrollTo(tableViewLogcatData.size() - 1);
        });
    }

    public Message executeShellCommand(String command, IShellOutputReceiver receiver) {
        return this.androidShell.executeShellCommand(command, receiver);
    }

    /**
     * 启动 agent main class
     *
     * @param command
     * @return
     * @throws IOException
     */
    public StringBuffer executeCommandStartMain(String command) throws IOException {
        return AdbCmdExecutor.executeCommandStartMain(command);
    }

    /**
     * push file to device
     *
     * @param local  local file path
     * @param remote device file path
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws SyncException
     * @throws IOException
     */
    public void pushFile(String local, String remote) throws TimeoutException, AdbCommandRejectedException, SyncException, IOException {
        this.device.pushFile(local, remote);
    }


    /**
     * 执行 monkey
     *
     * @param command command
     * @return StringBuffer
     */
    public StringBuffer executeMonkeyCommand(String command) {
        String cmd = String.format("%s -s %s shell %s", DirectoryService.getAdbPath(), this.getSerialNumber(), command);
        return this.androidShell.executeCommandByProcess(cmd);
    }

    /**
     * 执行 monkey 脚本
     *
     * @param filePath script file
     * @return StringBuffer
     */
    public StringBuffer executeMonkeyScript(String filePath) {
        String cmd = String.format("%s -s %s shell %s", DirectoryService.getAdbPath(), this.getSerialNumber(), filePath);
        return this.androidShell.executeCommandByProcess(cmd);
    }

    // ======  get set =========

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }

    public void setDeviceStatusListener(IDeviceStatusListener deviceStatusListener) {
        this.deviceStatusListener = deviceStatusListener;
    }

    public InputMethodClient getInputWebsocketClient() {
        return inputWebsocketClient;
    }

    public IDevice getDevice() {
        return device;
    }

    public void setDevice(IDevice device) {
        this.device = device;
    }

    public String getDeviceModel() {
        return this.getDevice().getProperty(IDevice.PROP_DEVICE_MODEL);
    }

    public String getSerialNumber() {
        return this.device.getSerialNumber();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public boolean isNavShow() {
        return navShow;
    }

    public void setNavShow(boolean navShow) {
        this.navShow = navShow;
    }

}
