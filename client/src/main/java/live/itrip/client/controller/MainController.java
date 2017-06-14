package live.itrip.client.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.xsocket.CommandInfo;
import live.itrip.client.xsocket.ICommandCallback;
import live.itrip.client.xsocket.XSocketClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    Button btnSocket;
    @FXML
    ImageView imageVewScreenshot;

    private DeviceInfo deviceInfo;
    public static final String HOST = "192.168.50.110";
    public static int PORT = 7878;

    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void buttonSocketClick(MouseEvent event) {
        // get device
        initDevice();
        // connect device server
//        WebSocketClient client = new WebSocketClient();
//        URI uri = URI.create("ws://localhost:53516");
//        client.connectToWebSocket(uri, imageVewScreenshot);
    }

    private void initDevice() {
        XSocketClient socketClient = new XSocketClient("127.0.0.1", 7878, new ICommandCallback() {
            @Override
            public void handleScreenshot(final byte[] imageBytes) throws IOException {
                System.err.println(imageBytes.length);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        BufferedImage img = null;
                        try {
                            img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            Image image = SwingFXUtils.toFXImage(img, null);
                            imageVewScreenshot.setImage(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

            @Override
            public void handleStringMessage(String message) {
                System.err.println(String.valueOf(message));
            }
        });

        CommandInfo command = new CommandInfo();
        command.setType(CommandInfo.CommandType.normal);
        command.setCommand("command");
        try {
            socketClient.sendCommandByNBC(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 连接到Socket服务端
    private void connected() {
        new Thread() {
            @Override
            public void run() {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //发送数据
    private void sendMessage() {

    }
}
