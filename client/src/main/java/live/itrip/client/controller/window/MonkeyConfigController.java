package live.itrip.client.controller.window;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import live.itrip.client.common.App;
import live.itrip.client.common.ModalReturnValue;
import live.itrip.client.device.test.config.MonkeyConfig;
import live.itrip.client.util.Logger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * Description: Monkey 命令配置界面
 *
 * @author JianF
 * Date:  2017/11/20
 * Time:  15:54
 * Modify:
 */
public class MonkeyConfigController implements Initializable {
    @FXML
    TabPane tabPane;
    @FXML
    TextField textFieldEventCount;
    @FXML
    TextField textFieldSeed;
    @FXML
    TextField textFieldThrottle;
    @FXML
    TextField textFieldPctTouch;
    @FXML
    TextField textFieldPctMotion;
    @FXML
    TextField textFieldPctNav;
    @FXML
    TextField textFieldPctMajorNav;
    @FXML
    TextField textFieldPctSysKeys;
    @FXML
    TextField textFieldPctAppSwitch;
    @FXML
    TextField textFieldPctAnyEvent;
    @FXML
    CheckBox checkBoxDbgNoEvents;
    @FXML
    CheckBox checkBoxHprof;
    @FXML
    CheckBox checkBoxIgnoreCrashes;
    @FXML
    CheckBox checkBoxIgnoreNavtiveCrashes;
    @FXML
    CheckBox checkBoxIgnoreTimeouts;
    @FXML
    CheckBox checkBoxIgnoreSecurityExceptions;
    @FXML
    CheckBox checkBoxKillProcessAfterError;
    @FXML
    CheckBox checkBoxMonitorNativeCrashes;
    @FXML
    CheckBox checkBoxWaitDbg;
    @FXML
    TextField textFieldScriptFile;
    @FXML
    Button btnScriptFile;
    @FXML
    Button btnOK;
    @FXML
    Button btnCancel;

    private static final String REGEX_D = "\\d*";
    private static final String REGEX_D_2 = "[^\\d]";
    private Stage stage;
    private MonkeyConfig monkeyConfig = new MonkeyConfig();
    private ModalReturnValue modalReturnValue = new ModalReturnValue();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set tab selected
        this.tabPane.getSelectionModel().select(0);
        // init return value
        modalReturnValue.setValue(monkeyConfig);

        // force the field to be numeric only
        textFieldEventCount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldEventCount.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldSeed.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldSeed.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldThrottle.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldThrottle.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctTouch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctTouch.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctMotion.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctMotion.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctNav.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctNav.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctMajorNav.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctMajorNav.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctSysKeys.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctSysKeys.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctAppSwitch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctAppSwitch.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });
        textFieldPctAnyEvent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(REGEX_D)) {
                textFieldPctAnyEvent.setText(newValue.replaceAll(REGEX_D_2, ""));
            }
        });

        // set textfield default value
        textFieldEventCount.setText(monkeyConfig.getEventCount().toString());
        textFieldSeed.setText(monkeyConfig.getSeed().toString());
        textFieldThrottle.setText(monkeyConfig.getThrottle().toString());
        textFieldPctTouch.setText(monkeyConfig.getPctTouch().toString());
        textFieldPctMotion.setText(monkeyConfig.getPctMotion().toString());
        textFieldPctNav.setText(monkeyConfig.getPctNav().toString());
        textFieldPctMajorNav.setText(monkeyConfig.getPctMajorNav().toString());
        textFieldPctSysKeys.setText(monkeyConfig.getPctSysKeys().toString());
        textFieldPctAnyEvent.setText(monkeyConfig.getPctAnyEvent().toString());
    }

    /**
     * 选择脚本文件
     *
     * @param event
     */
    @FXML
    private void buttonScriptFileClick(MouseEvent event) {
        // choice script file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Monkey 脚本文件");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Monkey Script Files", "*.script"));
        File selectedFile = fileChooser.showOpenDialog(App.primaryStage);
        if (selectedFile == null) {
            return;
        }
        Logger.debug(selectedFile.getAbsolutePath());
        // set text
        textFieldScriptFile.setText(selectedFile.getAbsolutePath());
    }

    /**
     * 执行测试
     *
     * @param event
     */
    @FXML
    private void buttonOKClicked(MouseEvent event) {
        MonkeyConfig monkeyConfig = new MonkeyConfig();
        int tabIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (tabIndex == 0) {
            // command
            monkeyConfig.setExecType(MonkeyConfig.EXEC_TYPE_COMMAND);
            // textfield
            if (StringUtils.isNotEmpty(textFieldSeed.getText())) {
                monkeyConfig.setSeed(Integer.valueOf(textFieldSeed.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldThrottle.getText())) {
                monkeyConfig.setThrottle(Integer.valueOf(textFieldThrottle.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctTouch.getText())) {
                monkeyConfig.setPctTouch(Integer.valueOf(textFieldPctTouch.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctMotion.getText())) {
                monkeyConfig.setPctMotion(Integer.valueOf(textFieldPctMotion.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctNav.getText())) {
                monkeyConfig.setPctNav(Integer.valueOf(textFieldPctNav.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctMajorNav.getText())) {
                monkeyConfig.setPctMajorNav(Integer.valueOf(textFieldPctMajorNav.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctSysKeys.getText())) {
                monkeyConfig.setPctSysKeys(Integer.valueOf(textFieldPctSysKeys.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctAppSwitch.getText())) {
                monkeyConfig.setPctAppSwitch(Integer.valueOf(textFieldPctAppSwitch.getText()));
            }
            if (StringUtils.isNotEmpty(textFieldPctAnyEvent.getText())) {
                monkeyConfig.setPctAnyEvent(Integer.valueOf(textFieldPctAnyEvent.getText()));
            }

            // checkbox
            monkeyConfig.setDbgNoEvents(checkBoxDbgNoEvents.isSelected());
            monkeyConfig.setHprof(checkBoxHprof.isSelected());
            monkeyConfig.setIgnoreCrashes(checkBoxIgnoreCrashes.isSelected());
            monkeyConfig.setIgnoreNavtiveCrashes(checkBoxIgnoreNavtiveCrashes.isSelected());
            monkeyConfig.setIgnoreTimeouts(checkBoxIgnoreTimeouts.isSelected());
            monkeyConfig.setIgnoreSecurityExceptions(checkBoxIgnoreSecurityExceptions.isSelected());
            monkeyConfig.setKillProcessAfterError(checkBoxKillProcessAfterError.isSelected());
            monkeyConfig.setMonitorNativeCrashes(checkBoxMonitorNativeCrashes.isSelected());
            monkeyConfig.setWaitDbg(checkBoxWaitDbg.isSelected());
        } else if (tabIndex == 1) {
            // script file
            monkeyConfig.setExecType(MonkeyConfig.EXEC_TYPE_SCRIPT);
            if (StringUtils.isEmpty(this.textFieldScriptFile.getText())) {
                return;
            }
            ArrayList<String> scripts = new ArrayList<>();
            scripts.add(this.textFieldScriptFile.getText());
            monkeyConfig.setScriptfile(scripts);
        }
        modalReturnValue.setButtonType(ButtonType.OK);
        modalReturnValue.setValue(monkeyConfig);
        this.close();
    }

    /**
     * 取消
     *
     * @param event
     */
    @FXML
    private void buttonCancelClicked(MouseEvent event) {
        modalReturnValue.setButtonType(ButtonType.CANCEL);
        this.close();
    }

    /**
     * close this window
     */
    private void close() {
        if (this.stage != null) {
            this.stage.close();
        }
    }

    public ModalReturnValue getModalReturnValue() {
        return modalReturnValue;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(we -> {
            modalReturnValue.setButtonType(ButtonType.CLOSE);
        });
    }
}
