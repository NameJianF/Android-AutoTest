package live.itrip.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import live.itrip.client.bean.Message;
import live.itrip.client.bean.apk.ApkInfo;
import live.itrip.client.bean.device.ClientLogCatMessage;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.bean.ui.PageInfo;
import live.itrip.client.common.App;
import live.itrip.client.common.ModalReturnValue;
import live.itrip.client.controller.window.MonkeyConfigController;
import live.itrip.client.device.test.TestTypeEnum;
import live.itrip.client.device.test.TesterFactory;
import live.itrip.client.device.test.config.MonkeyConfig;
import live.itrip.client.device.test.tester.MonkeyTester;
import live.itrip.client.handler.KeyboardInputHandler;
import live.itrip.client.handler.LineChartHandler;
import live.itrip.client.handler.MouseEventHandler;
import live.itrip.client.handler.PerformanceDataHandler;
import live.itrip.client.util.DatetimeFormatUtils;
import live.itrip.client.util.Logger;
import live.itrip.client.util.PageUtils;
import live.itrip.client.view.ChoiceDialogHelper;
import live.itrip.client.view.LogcatTableCell;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Feng
 */
@SuppressWarnings("ALL")
public class MainController implements IClientLogListener, IDeviceStatusListener, IAppAnalyzeListener, Initializable {
    @FXML
    ImageView imgAppLogo;
    @FXML
    TextField textFieldAppLable;
    @FXML
    TextField textFieldPkgName;
    @FXML
    TextField textFieldVerCode;
    @FXML
    TextField textFieldVerName;
    @FXML
    TextField textFieldMinSDK;
    @FXML
    TextField textFieldTargetSDK;
    @FXML
    Button btnInstallApp;
    @FXML
    Button btnScreenshot;
    @FXML
    Button btnStop;
    @FXML
    Button btnBack;
    @FXML
    Button btnHome;
    @FXML
    Button btnMenu;
    @FXML
    Button btnMonkey;
    @FXML
    Button btnRobotium;
    @FXML
    Button btnUiautomator;
    @FXML
    Button btnAppium;
    @FXML
    ImageView imageView;
    @FXML
    Pane paneImage;
    @FXML
    TabPane tabPane;
    @FXML
    VBox vBox;
    @FXML
    ListView<String> listViewDeviceInfo;
    @FXML
    ListView<String> listViewAppInfo;
    @FXML
    GridPane gridPane;
    @FXML
    TextArea textAreaClientLog;
    @FXML
    TableView tableViewLogcat;
    @FXML
    TableColumn<ClientLogCatMessage, String> tableColumnTime;
    @FXML
    TableColumn<ClientLogCatMessage, String> tableColumnLevel;
    @FXML
    TableColumn<ClientLogCatMessage, String> tableColumnTAG;
    @FXML
    TableColumn<ClientLogCatMessage, String> tableColumnPID;
    @FXML
    TableColumn<ClientLogCatMessage, String> tableColumnTID;
    @FXML
    TableColumn<ClientLogCatMessage, String> tableColumnText;

    private static String LOGCAT_TAG = "itrip-agent";
    private DeviceInfo deviceInfo;
    private LineChartHandler handlerMemory = new LineChartHandler();
    private LineChartHandler handlerCpu = new LineChartHandler();
    private LineChartHandler handlerFps = new LineChartHandler();
    private LineChartHandler handlerNetwork = new LineChartHandler();
    private ObservableList<ClientLogCatMessage> tableViewLogcatData;
    private MouseEventHandler mouseEventHandler;
    private KeyboardInputHandler keyboardInputHandler;
    private String appPath = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ========== linechart
        initLineChart();

        // tableView
        initTableView();
    }


    private void initLineChart() {
        String style = ".thick-chart .chart-series-line {    " +
                "-fx-stroke-width: 1px;" +
                "}";
        gridPane.add(handlerMemory.init("Time(s)", "Memory(MB)", "Memory", style), 0, 0);
        gridPane.add(handlerCpu.init("Time(s)", "CPU(%)", "CPU", style), 1, 0);
        gridPane.add(handlerFps.init("Time(s)", "FPS(f/s)", "FPS", style), 0, 1);
        gridPane.add(handlerNetwork.init("Time(s)", "NetFlow(MB)", "NetFlow", style), 1, 1);
    }

    private void initTableView() {
        tableViewLogcatData = FXCollections.observableArrayList();

        LogcatTableCell.setColumnCellFactory(tableColumnTime);
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());

        LogcatTableCell.setColumnCellFactory(tableColumnLevel);
        tableColumnLevel.setCellValueFactory(cellData -> cellData.getValue().logLevelProperty());

        LogcatTableCell.setColumnCellFactory(tableColumnTAG);
        tableColumnTAG.setCellValueFactory(cellData -> cellData.getValue().tagProperty());

        LogcatTableCell.setColumnCellFactory(tableColumnPID);
        tableColumnPID.setCellValueFactory(cellData -> cellData.getValue().pidProperty());

        LogcatTableCell.setColumnCellFactory(tableColumnTID);
        tableColumnTID.setCellValueFactory(cellData -> cellData.getValue().tidProperty());

        LogcatTableCell.setColumnCellFactory(tableColumnText);
        tableColumnText.setCellValueFactory(cellData -> cellData.getValue().messageProperty());

        tableViewLogcat.setItems(tableViewLogcatData);
    }

    /**
     * 启动设备端 代理服务
     */
    private void startAppAgentService() {

        // 启动输入服务
        this.deviceInfo.startInputService();
        // 设置键盘事件映射
        this.mouseEventHandler = new MouseEventHandler(this.deviceInfo, imageView.getFitWidth());
        this.keyboardInputHandler = new KeyboardInputHandler(this.deviceInfo);
        initImageViewEvents();

        // 映射屏幕
        this.deviceInfo.screenLive(imageView);
    }

    private void initImageViewEvents() {
        // mouse
        imageView.setOnMouseClicked(event -> {
            // 点击事件
            imageView.requestFocus();
            mouseEventHandler.onMouseClicked(event);
        });

//        imageView.setOnMouseDragged(mouseEventHandler::onMouseDragged);
//        imageView.setOnMouseMoved(mouseEventHandler::onMouseMoved);
        imageView.setOnMousePressed(mouseEventHandler::onMousePressed);
        imageView.setOnMouseReleased(mouseEventHandler::onMouseReleased);
//        imageView.setOnScroll(mouseEventHandler::onScroll);

        // keyboard
        imageView.setOnKeyPressed(keyboardInputHandler::onKeyPressed);

//        imageView.setOnScroll(event -> {
//            System.err.println(String.format("OnScroll(DeltaX:%s,DeltaY:%s)", event.getDeltaX(), event.getDeltaX()));
//        });
//        imageView.setInputMethodRequests(new InputMethodRequests(){
//
//        });
    }

    @FXML
    public void btnConnectClick(MouseEvent mouseEvent) {
        // 1. 显示设备列表,返回选择的单个设备
        DeviceInfo tmp = ChoiceDialogHelper.getSelectedDevice();
        if (tmp == null) {
            return;
        }

        this.deviceInfo = tmp;
        writeClientLog("选择设备：" + deviceInfo.getDeviceModel());

        // 设置设备状态监听
        this.deviceInfo.setDeviceStatusListener(this);

        // get logcat
        this.deviceInfo.getDeviceLogcat("", tableViewLogcatData, tableViewLogcat);

        // 2. 初始化设备
        this.deviceInfo.initDevice();

        // 设置显示设备信息tab
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    public void btnSelectAppClick(MouseEvent mouseEvent) {
        // 1. 选择apk
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择APK");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("APK Files", "*.apk"));
        File selectedFile = fileChooser.showOpenDialog(App.primaryStage);
        if (selectedFile == null) {
            return;
        }
        this.appPath = selectedFile.getAbsolutePath();
        writeClientLog("selected apk file : " + this.appPath);
    }

    @Override
    public void showToWindow(ApkInfo apkInfo) {
        if (apkInfo == null) {
            Logger.debug("ApkInfo is null.");
            return;
        }
        writeClientLog("解析apk完成.");
        Logger.debug(apkInfo.toString());

        // show
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add(String.format("LaunchableActivity >>> %s", apkInfo.getLaunchableActivity()));
        items.add(String.format("TargetSdkVersion >>> %s", apkInfo.getTargetSdkVersion()));
        if (apkInfo.getUsesPermissions() != null) {
            for (String per : apkInfo.getUsesPermissions()) {
                items.add(String.format("UsesPermission >>> %s", per));
            }
        }

        Platform.runLater(() -> {
            imgAppLogo.setImage(apkInfo.getIcon());
            textFieldAppLable.setText(apkInfo.getApplicationLable());
            textFieldPkgName.setText(apkInfo.getPackageName());
            textFieldVerCode.setText(apkInfo.getVersionCode());
            textFieldVerName.setText(apkInfo.getVersionName());
            textFieldMinSDK.setText(apkInfo.getMinSdkVersion());
            textFieldTargetSDK.setText(apkInfo.getTargetSdkVersion());
            listViewAppInfo.setItems(items);
        });

        // 设置tab页面显示app信息页面
        tabPane.getSelectionModel().select(1);
    }

    @FXML
    private void btnScreenshotClick(MouseEvent event) {
        if (this.deviceInfo != null && this.deviceInfo.getDevice().isOnline()) {
            Image image = this.deviceInfo.screenshot();
            // set imageVew
            if (imageView == null) {
                return;
            }
            imageView.setImage(image);
        }
    }

    @FXML
    private void btnLiveClick(MouseEvent event) throws IOException {
        if (this.deviceInfo != null && this.deviceInfo.getDevice().isOnline()) {
            /**
             // 性能数据
             this.deviceInfo.performanceInfos(apkInfo.getPackageName(), apkInfo.getAppType());
             // 映射屏幕
             this.deviceInfo.screenLive(imageView);
             **/
        }
    }

    @FXML
    private void btnStopClick(MouseEvent event) throws IOException {
        if (this.deviceInfo != null) {
            this.deviceInfo.stopPerformance();
        }
    }

    @FXML
    private void btnBackClick(MouseEvent event) {
        if (this.mouseEventHandler != null) {
            this.mouseEventHandler.BackClick();
        }
    }

    @FXML
    private void btnHomeClick(MouseEvent event) {
        if (this.mouseEventHandler != null) {
            this.mouseEventHandler.HomeClick();
        }
    }

    @FXML
    private void btnMenuClick(MouseEvent event) {
        if (this.mouseEventHandler != null) {
            this.mouseEventHandler.MenuClick();
        }
    }

    @FXML
    private void btnMonkeyClick(MouseEvent event) {
        if (this.deviceInfo == null || !this.deviceInfo.getDevice().isOnline()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null);
            alert.setContentText("请重新连接设备.");
            alert.showAndWait();
            return;
        }
        if (StringUtils.isEmpty(this.appPath)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null);
            alert.setContentText("请先选择App!");
            alert.showAndWait();
            return;
        }

        PageInfo pageInfo = PageUtils.getPageInfo("/ui/window/monkey_config.fxml");
        Stage modalStage = new Stage();
        modalStage.setScene(new Scene((Parent) pageInfo.getNode()));
        // set controller
        MonkeyConfigController configController = (MonkeyConfigController) pageInfo.getController();
        configController.setStage(modalStage);

        modalStage.setTitle("Monkey Config");
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(App.primaryStage);
        modalStage.showAndWait();

        // get result
        ModalReturnValue modalReturnValue = configController.getModalReturnValue();
        MonkeyConfig monkeyConfig = (MonkeyConfig) modalReturnValue.getValue();

        if (ButtonType.OK.equals(modalReturnValue.getButtonType())) {
            // exec command
            MonkeyTester monkeyTester = (MonkeyTester) TesterFactory.createTester(TestTypeEnum.Money
                    , this.deviceInfo
                    , this
                    , this);
            if (monkeyTester == null) {
                return;
            }
            monkeyTester.setTestConfig(monkeyConfig);
            Message message = monkeyTester.execTest(this.appPath);
        }
    }

    @FXML
    private void btnRobotiumClick(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("敬请期待!");
        alert.showAndWait();
    }

    @FXML
    private void btnUiautomatorClick(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("敬请期待!");
        alert.showAndWait();
    }

    @FXML
    private void btnAppiumClick(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("");
        alert.setContentText("敬请期待!");
        alert.showAndWait();
    }

    @Override
    public void writeClientLog(String msg) {
        final String message = textAreaClientLog.getText()
                + "\n"
                + String.format("%s >>> %s", DatetimeFormatUtils.getCurrentTime(), msg);

        Platform.runLater(() -> textAreaClientLog.setText(message));
    }

    @Override
    public void initing(String msg) {
        writeClientLog(msg);
    }

    /**
     * 完成设备初始化
     *
     * @param msg
     */
    @Override
    public void inited(String msg) {
        writeClientLog(msg);
        // start services
        startAppAgentService();
    }


    @Override
    public void getDeviceInfos(ObservableList<String> items) {
        // 3. 获取设备信息
        Platform.runLater(() -> listViewDeviceInfo.setItems(items));
    }

    @Override
    public void handPerformanceDatas(JSONObject json) {
        Platform.runLater(() ->
                PerformanceDataHandler.hanlDatas(json
                        , handlerMemory
                        , handlerCpu
                        , handlerFps
                        , handlerNetwork)
        );
    }

    @Override
    public void onAgentMainClassClosed() {

    }

}
