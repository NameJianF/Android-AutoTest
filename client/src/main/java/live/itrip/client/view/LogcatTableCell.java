package live.itrip.client.view;

import com.android.ddmlib.Log;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import live.itrip.client.bean.device.ClientLogCatMessage;

public class LogcatTableCell {

    public static void setColumnCellFactory(TableColumn column) {
        column.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<ClientLogCatMessage, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            ClientLogCatMessage message = getTableView().getItems().get(getIndex());
                            if (Log.LogLevel.WARN.toString().equals(message.logLevelProperty().get())) {
                                this.setTextFill(Color.ORANGE);
                            } else if (Log.LogLevel.ERROR.toString().equals(message.logLevelProperty().get())) {
                                this.setTextFill(Color.RED);
                            } else if (Log.LogLevel.ASSERT.toString().equals(message.logLevelProperty().get())) {
                                this.setTextFill(Color.DARKRED);
                            } else if (Log.LogLevel.DEBUG.toString().equals(message.logLevelProperty().get())) {
                                this.setTextFill(Color.BLUE);
                            } else {
                                this.setTextFill(Color.BLACK);
                            }
                            setText(item);
                        }
                    }
                };
            }
        });
    }


}
