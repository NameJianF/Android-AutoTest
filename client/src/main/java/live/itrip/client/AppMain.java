package live.itrip.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import live.itrip.client.common.App;
import live.itrip.client.common.Config;
import live.itrip.client.device.DeviceManager;
import live.itrip.client.util.Logger;
import live.itrip.client.util.PageUtil;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Feng on 2017/6/12.
 */
public class AppMain extends Application {
    /**
     * load system config
     */
    static {
        Logger.debug(" >>>>>> Application LoadConfig Start ... <<<<<< ");
        loadConfig();
        Logger.debug(" >>>>>> Application LoadConfig End ... <<<<<< ");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        App.primaryStage = primaryStage;

        URL url = getClass().getResource("/ui/main.fxml");
        Parent root = FXMLLoader.load(url);
        primaryStage.setTitle("自动化测试-Client");
        primaryStage.setScene(new Scene(root, 1000, 600));
//        primaryStage.setFullScreen(true);
        primaryStage.getIcons().add(PageUtil.getLogo());
        primaryStage.show();

        DeviceManager.getInstance().start();
    }


    public static void main(String[] args) throws SQLException {

        Logger.debug(" >>>>>> Application Start ... <<<<<< ");

        launch(args);

        Logger.debug(" >>>>>> Application Closed ... <<<<<< ");
        System.exit(0);
    }


    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        Properties prop = new Properties();
//        URL uri = AppMain.class.getClass().getResource("/config.properties");
//        String fileName = uri.getFile();

        try {

//            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
//            prop.load(in);

            // 最前面有斜杠
            InputStream input = AppMain.class.getClass().getResourceAsStream("/config.properties");
            prop.load(input);
        } catch (Exception ex) {
            Logger.error(ex.getMessage(), new Throwable(ex));
        }

        Config.CLIENT_VERSION = prop.getProperty("client.version");
        Config.printValues();
    }
}
