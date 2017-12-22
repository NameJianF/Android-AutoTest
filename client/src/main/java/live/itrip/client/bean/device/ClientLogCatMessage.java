package live.itrip.client.bean.device;

import com.android.ddmlib.logcat.LogCatMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClientLogCatMessage {
    private StringProperty logLevel;
    private StringProperty pid;
    private StringProperty tid;
    private StringProperty appName;
    private StringProperty tag;
    private StringProperty timestamp;
    private StringProperty message;

    public ClientLogCatMessage(LogCatMessage logCatMessage) {
        logLevel = new SimpleStringProperty(logCatMessage.getLogLevel().toString());
        pid = new SimpleStringProperty(String.valueOf(logCatMessage.getPid()));
        tid = new SimpleStringProperty(String.valueOf(logCatMessage.getTid()));
        appName = new SimpleStringProperty(logCatMessage.getAppName());
        tag = new SimpleStringProperty(logCatMessage.getTag());
        timestamp = new SimpleStringProperty(logCatMessage.getTimestamp().toString());
        message = new SimpleStringProperty(logCatMessage.getMessage());
    }

    public StringProperty logLevelProperty() {
        return logLevel;
    }

    public StringProperty pidProperty() {
        return pid;
    }

    public StringProperty tidProperty() {
        return tid;
    }

    public StringProperty appNameProperty() {
        return appName;
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }

    public StringProperty messageProperty() {
        return message;
    }
}
