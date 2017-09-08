package live.itrip.client.controller;

import com.sun.deploy.net.HttpResponse;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.util.FFmpegFXImageDecoder;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.View;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class MainController implements Initializable {

    @FXML
    Button btnScreenshot;
    @FXML
    Button btnStop;
    @FXML
    ImageView imageView;
    @FXML
    VBox vBox;

    @FXML
    MediaView mediaView;

    private DeviceInfo deviceInfo;
    private Thread threadLive;

    public void initialize(URL location, ResourceBundle resources) {
        // get device
        initDevice();
    }

    private void initDevice() {

    }

    @FXML
    private void btnScreenshotClick(MouseEvent event) {
        String imageSource = "http://localhost:53516/screenshot.jpg";
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
        String url = "http://localhost:53516/h264";

        if (threadLive == null) {
            threadLive = new Thread(() -> FFmpegFXImageDecoder.streamToImageView(url, imageView, "h264", 96, 25000000, "ultrafast", 0)
            );
            threadLive.start();
        } else {
            threadLive.interrupt();
        }
    }

    @FXML
    private void btnStopClick(MouseEvent event) throws IOException {

//        String source = "http://localhost:53516/h264";
//        Media pick = new Media(source);
//
//        MediaPlayer player = new MediaPlayer(pick);
//        player.setAutoPlay(true);
//        player.play();
//
//        mediaView.setMediaPlayer(player);
    }

}
