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
 * Created by Feng on 2017/5/23.
 */
public class FFmpegFXImageDecoder {
    public static void streamToImageView(String url,
                                         final ImageView view,
                                         final String format,
                                         final double frameRate,
                                         final int bitrate,
                                         final String preset,
                                         final int numBuffers,
                                         final Alert dialog) {
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try (final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url)) {
            grabber.setFrameRate(frameRate);
            grabber.setFormat(format);
            grabber.setVideoBitrate(bitrate);
            grabber.setVideoOption("preset", preset);
            grabber.setNumBuffers(numBuffers);
            grabber.start();
            while (!Thread.interrupted()) {
                final Frame frame = grabber.grab();
                if (frame != null) {
                    final BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        bufferedImage.flush();
                        Platform.runLater(() ->
                        {
                            view.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                            if (dialog != null && dialog.isShowing()) {
                                dialog.close();
                            }
                        });
                    }
                }
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

}
