package live.itrip.client.util;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.image.Image;
import live.itrip.client.AppMain;
import live.itrip.client.bean.ui.PageInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created on 2017/5/23.
 *
 * @author JianF
 */
public class PageUtils {

    public static Image getLogo() {
        String logoUrl = AppMain.class.getResource("/img/logo.png").toExternalForm();
        Image logo = new Image(logoUrl);
        return logo;
    }

    public static PageInfo getPageInfo(String fxml) {
        Logger.debug("FXML path:" + fxml);
        PageInfo pageInfo = new PageInfo();
        URL url = AppMain.class.getResource(fxml);
        // 创建对象
        FXMLLoader loader = new FXMLLoader();
        // 设置BuilderFactory
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        // 设置路径基准
        loader.setLocation(url);
        InputStream in = null;
        try {
            in = url.openStream();
            // 对象方法的参数是InputStream，返回值是Object
            pageInfo.setNode(loader.load(in));
            pageInfo.setController(loader.getController());
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
            }
        }

        return pageInfo;
    }
    /**
     //
     //    public static GridPane getGridPane(String fxml) {
     //        Logger.debug("FXML path:" + fxml);
     //        URL url = AppMain.class.getResource(fxml);
     //        FXMLLoader loader = new FXMLLoader();// 创建对象
     //        loader.setBuilderFactory(new JavaFXBuilderFactory());// 设置BuilderFactory
     //        loader.setLocation(url);  // 设置路径基准
     //        GridPane page = null;
     //        InputStream in = null;
     //        try {
     //            in = url.openStream();
     //            page = loader.load(in);  // 对象方法的参数是InputStream，返回值是Object
     //
     //        } catch (IOException e) {
     //            Logger.error(e.getMessage(), e);
     //        } finally {
     //            try {
     //                if (in != null) {
     //                    in.close();
     //                }
     //            } catch (IOException e) {
     //                Logger.error(e.getMessage(), e);
     //            }
     //        }
     //
     //        return page;
     //    }
     //
     //    public static AnchorPane getAnchorPane(String fxml) {
     //        Logger.debug("FXML path:" + fxml);
     //        URL url = AppMain.class.getResource(fxml);
     //        FXMLLoader loader = new FXMLLoader();// 创建对象
     //        loader.setBuilderFactory(new JavaFXBuilderFactory());// 设置BuilderFactory
     //        loader.setLocation(url);  // 设置路径基准
     //        AnchorPane page = null;
     //        InputStream in = null;
     //        try {
     //            in = url.openStream();
     //            page = loader.load(in);  // 对象方法的参数是InputStream，返回值是Object
     //
     //        } catch (IOException e) {
     //            Logger.error(e.getMessage(), e);
     //        } finally {
     //            try {
     //                if (in != null) {
     //                    in.close();
     //                }
     //            } catch (IOException e) {
     //                Logger.error(e.getMessage(), e);
     //            }
     //        }
     //
     //        return page;
     //    }
     **/
}
