package live.itrip.client.controller;

import com.android.ddmlib.ShellCommandUnresponsiveException;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import live.itrip.client.bean.Message;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.device.AdbCmdExecutor;
import live.itrip.client.device.AndroidShell;
import live.itrip.client.device.DeviceManager;
import live.itrip.client.device.DirectoryService;
import live.itrip.client.service.input.InputWebsocketService;
import live.itrip.client.service.performance.PerformanceWebsocketService;
import live.itrip.client.util.FFmpegFXImageDecoder;
import live.itrip.client.util.Logger;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    Button btnScreenshot;
    @FXML
    Button btnStop;
    @FXML
    ImageView imageView;
    @FXML
    VBox vBox;
    //    @FXML
//    MediaView mediaView;
    @FXML
    ListView listViewDeviceInfo;
    @FXML
    ListView listViewAppInfo;
    @FXML
    LineChart lineChartMemory;
    @FXML
    ScrollPane scrollPaneMemory;

    public static final String HTTP_URL = "http://localhost";
    private DeviceInfo deviceInfo;
    private AndroidShell androidShell;
    private Thread threadLive;

    public void initialize(URL location, ResourceBundle resources) {
    }

    private void initDevice() {
        this.deviceInfo = DeviceManager.getInstance().getDeviceInfo();
        if (this.deviceInfo == null) {
            Alert dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setContentText("没有发现设备");
            dialog.showAndWait();
            return;
        }
        if (!this.deviceInfo.getDevice().isOnline()) {
            Alert dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setContentText("设备离线状态");
            dialog.showAndWait();
            return;
        }
        // init shell
        androidShell = new AndroidShell(this.deviceInfo);
        Message msg;

        // uninstall agent
        msg = androidShell.unInstallAgent("live.itrip.agent");
        Logger.debug(String.format("unInstallAgent Code:%s,msg:%s", msg.getCode(), msg.getContent()));

        // install agent
        String agentPath = DirectoryService.getAgentPath();
        msg = androidShell.installAgent(agentPath, true);
        Logger.debug(String.format("installAgent Code:%s,msg:%s", msg.getCode(), msg.getContent()));

        // start main
        Logger.debug("startAgentMainClass...........");

        AdbCmdExecutor.DefaultShellOutputReceiver rcvr = new AdbCmdExecutor.DefaultShellOutputReceiver();
        String cmd = "CLASSPATH=/data/app/live.itrip.agent-1/base.apk exec app_process /system/bin live.itrip.agent.Main";
        androidShell.startAgentMainClass(cmd, rcvr);
        Logger.debug("startAgentMainClass end...........");

        // forward port
        Logger.debug("createForward...........");
        try {
            androidShell.createForward(DeviceInfo.AGENT_HTTP_SERVER_PORT, String.valueOf(DeviceInfo.AGENT_HTTP_SERVER_PORT));
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        Logger.debug("createForward end...........");

    }

    @FXML
    public void btnConnectClick(MouseEvent mouseEvent) {
        initDevice();
    }

    @FXML
    private void btnScreenshotClick(MouseEvent event) {
        String imageSource = String.format("%s:%s/screenshot.jpg", HTTP_URL, DeviceInfo.AGENT_HTTP_SERVER_PORT);
        Image image = new Image(imageSource);
        // set imageVew
        if (imageView == null) {
            return;
        }
        imageView.setImage(image);
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }

    @FXML
    private void btnLiveClick(MouseEvent event) throws IOException {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("初始化图像数据");
        dialog.setHeaderText("正在初始化数据，请稍等...");

        // image view
        String url = String.format("%s:%s/h264", HTTP_URL, DeviceInfo.AGENT_HTTP_SERVER_PORT);
        if (threadLive == null) {
            threadLive = new Thread(() -> FFmpegFXImageDecoder.streamToImageView(url, imageView, "h264", 96, 25000000, "ultrafast", 0, dialog)
            );
            threadLive.start();
            Logger.debug("threadLive.start:" + System.currentTimeMillis());

        } else {
            threadLive.interrupt();
        }

        dialog.showAndWait();
        dialog.close();


        // media View
//        Media media = new Media(url);
//        MediaPlayer player = new MediaPlayer(media);
//        player.setAutoPlay(true);
//        player.play();
//        mediaView.setMediaPlayer(player);
    }

    @FXML
    private void btnStopClick(MouseEvent event) throws IOException {
//        threadLive.interrupt();

//        DeviceInfoService.getDeviceInformations(listViewDeviceInfo);
//        PerformanceWebsocketService.getInstance().dynamicLoadMemory(scrollPaneMemory, lineChartMemory);

        /*
        InputWebsocketService clientService = new InputWebsocketService();
        final String name = "inputService";
        clientService.setName(name);

        clientService.setOnSucceeded(handle -> {
            Logger.debug("Said hello to " + name + ", response " + clientService.getValue());
        });

        clientService.setOnFailed(handle ->
                Logger.error("Unable to say hello to " + name, clientService.getException()));
        clientService.restart();

        JSONObject back = new JSONObject();
        back.put("type", "back");
        back.put("clientX", 300);
        back.put("clientY", 400);
        clientService.sendText(back.toString());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONObject home = new JSONObject();
        home.put("type", "home");
        home.put("clientX", 300);
        home.put("clientY", 400);
        clientService.sendText(home.toString());
        */


        PerformanceWebsocketService clientService = new PerformanceWebsocketService();
        final String name = "inputService";
        clientService.setName(name);

        clientService.setOnSucceeded(handle -> {
            Logger.debug("Said hello to " + name + ", response " + clientService.getValue());
        });

        clientService.setOnFailed(handle ->
                Logger.error("Unable to say hello to " + name, clientService.getException()));
        clientService.restart();

        for (int i = 0; i < 10; i++) {
            JSONObject home = new JSONObject();
            home.put("type", "memory");
            home.put("packageName", "live.itrip.agent");
            clientService.sendText(home.toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JSONObject back = new JSONObject();
            back.put("type", "fps");
            back.put("packageName", "live.itrip.agent");
            back.put("clientX", 300);
            back.put("clientY", 400);
            clientService.sendText(back.toString());


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JSONObject cpu = new JSONObject();
            cpu.put("type", "cpu");
            cpu.put("packageName", "live.itrip.agent");
            cpu.put("clientX", 300);
            cpu.put("clientY", 400);
            clientService.sendText(cpu.toString());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
