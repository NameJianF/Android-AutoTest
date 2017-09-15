package live.itrip.client.service.performance;

import javafx.concurrent.Task;
import live.itrip.client.bean.device.DeviceInfo;
import live.itrip.client.util.Logger;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

public class PerformanceWebsocketTask extends Task<String> {
    private PerformanceWebsocketClient clientEndpoint;

    public PerformanceWebsocketTask(String name) {
        clientEndpoint = new PerformanceWebsocketClient(name);
    }

    /**
     * Sends the requested name passed in the Task constructor to the server endpoint.
     * A new connection is established for the request.
     *
     * @return the response from the server.
     * @throws IOException      if there was an error communication with the server.
     * @throws TimeoutException if communication with the server timed out before a response was received.
     */
    @Override
    protected String call() throws IOException, TimeoutException, InterruptedException {
        return clientEndpoint.getResponse();
    }

    public void connectServer() throws IOException, DeploymentException {
        String SERVER_ENDPOINT_ADDRESS = String.format("ws://localhost:%s/infos", DeviceInfo.AGENT_HTTP_SERVER_PORT);
        Logger.error(SERVER_ENDPOINT_ADDRESS);

        ClientManager client = ClientManager.createClient();
        client.connectToServer(clientEndpoint, URI.create(SERVER_ENDPOINT_ADDRESS));
    }

    public void sendText(String text) throws IOException {
        if (this.clientEndpoint != null)
            this.clientEndpoint.sendText(text);
    }

    public void sendText(String text, boolean isLast) throws IOException {
        if (this.clientEndpoint != null)
            this.clientEndpoint.sendText(text, isLast);
    }

    public void sendBinary(ByteBuffer data) throws IOException {
        if (this.clientEndpoint != null)
            this.clientEndpoint.sendBinary(data);
    }

    public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
        if (this.clientEndpoint != null)
            this.clientEndpoint.sendBinary(partialByte, isLast);
    }

    public void sendObject(Object data) throws IOException, EncodeException {
        if (this.clientEndpoint != null)
            this.clientEndpoint.sendObject(data);
    }
}
