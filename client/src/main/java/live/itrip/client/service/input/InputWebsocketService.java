package live.itrip.client.service.input;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import live.itrip.client.util.Logger;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InputWebsocketService extends Service<String> {
    private final StringProperty name = new SimpleStringProperty(this, "name");

    /**
     * The name property is set as an input parameter for a service execution.
     *
     * @return the name property.
     */
    public final StringProperty nameProperty() {
        return name;
    }

    public final void setName(String value) {
        name.set(value);
    }

    public final String getName() {
        return name.get();
    }

    private InputWebsocketTask clientTask;

    public void sendText(String text) throws IOException {
        if (this.clientTask != null)
            this.clientTask.sendText(text);
    }

    public void sendText(String text, boolean isLast) throws IOException {
        if (this.clientTask != null)
            this.clientTask.sendText(text, isLast);
    }

    public void sendBinary(ByteBuffer data) throws IOException {
        if (this.clientTask != null)
            this.clientTask.sendBinary(data);
    }

    public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
        if (this.clientTask != null)
            this.clientTask.sendBinary(partialByte, isLast);
    }

    public void sendObject(Object data) throws IOException, EncodeException {
        if (this.clientTask != null)
            this.clientTask.sendObject(data);

    }

    @Override
    protected Task<String> createTask() {
        clientTask = new InputWebsocketTask(getName());
        try {
            clientTask.connectServer();
        } catch (IOException | DeploymentException e) {
            Logger.error(e.getMessage(), e);
        }
        return clientTask;
    }
}
