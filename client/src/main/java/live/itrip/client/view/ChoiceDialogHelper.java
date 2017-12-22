package live.itrip.client.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.device.DeviceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChoiceDialogHelper {

    public static DeviceInfo getSelectedDevice() {
        List<DeviceInfo> deviceList = DeviceManager.getInstance().getDeviceList();

        if (deviceList.size() <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR Dialog");
            alert.setHeaderText(null);
            alert.setContentText("no devices online!");

            alert.showAndWait();
            return null;
        }

        List<String> choices = new ArrayList<>();
        for (DeviceInfo info : deviceList) {
            choices.add(String.format("%s : %s", info.getDeviceModel(), info.getSerialNumber()));
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Choice Dialog");
        dialog.setHeaderText("选择一个已连接的设备.");
        dialog.setContentText("设备:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            int index = choices.indexOf(result.get());
            return deviceList.get(index);
        }

        return null;
    }
}
