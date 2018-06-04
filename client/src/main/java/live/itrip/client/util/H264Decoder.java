package live.itrip.client.util;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;

/**
 * Created on 2017/5/23.
 * <p>
 * Desp:h264 decoder by ffmpeg
 *
 * @author JianF
 */
public class H264Decoder {

    public static void streamToImageView(String url,
                                         final ImageView imageView,
                                         final int numBuffers) {
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try (final FrameGrabber grabber = new FFmpegFrameGrabber(url)) {
            double frameRate = 500000;
            String codeFormatH264 = "h264";
            int bitRate = 96;
            String presetUltraFast = "ultrafast";

            grabber.setFrameRate(frameRate);
            grabber.setFormat(codeFormatH264);
            grabber.setVideoBitrate(bitRate);
            grabber.setVideoOption("preset", presetUltraFast);
            grabber.setNumBuffers(numBuffers);
            grabber.setImageWidth((int) imageView.getFitWidth());
            grabber.setImageHeight((int) imageView.getFitHeight());
            grabber.start();
            while (!Thread.interrupted()) {
                final Frame frame = grabber.grab();
                if (frame != null) {
                    final BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        bufferedImage.flush();
                        Platform.runLater(() -> {
//                            Logger.debug(String.format("get BufferedImage【%s * %s】", bufferedImage.getWidth(), bufferedImage.getHeight()));
                            imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                        });
                    }
                }
            }
        } catch (FrameGrabber.Exception e) {
            Logger.error(e.getMessage(), e);
        }
    }

}
