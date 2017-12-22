package live.itrip.client.service.performance;

import live.itrip.client.util.Logger;

import javax.websocket.*;
import java.io.IOException;

/**
 * @author
 */

@ClientEndpoint(subprotocols = "infos-protocol")
public class PerformanceWebsocketClient {
    private final String name;
    private Session session;
    private IMessageCallback messageCallback;

    public PerformanceWebsocketClient(String name) {
        this.name = name;
    }

    public void setMessageCallback(IMessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    @OnOpen
    public void onOpen(Session session) {
        Logger.debug("Sending request: '" + name + "' with session " + session.getId());
        this.session = session;
    }

    @OnMessage
    public void OnMessage(Session session, String message) {
        Logger.debug("Received response: '" + message + "' for request: '" + name + "' with session " + session.getId());
        messageCallback.OnMessage(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.error("Communication error, saying hello to '" + name + "' with session " + session.getId(), throwable);
    }

    @OnClose
    public void onClose() {
//        connectToWebSocket();
    }

    public void sendText(String text) throws IOException {
        if (this.session != null) {
            this.session.getBasicRemote().sendText(text);
        }
    }

    /*
    public void sendText(String text, boolean isLast) throws IOException {
        if (this.session != null)
            this.session.getBasicRemote().sendText(text, isLast);
    }

    public void sendBinary(ByteBuffer data) throws IOException {
        if (this.session != null)
            this.session.getBasicRemote().sendBinary(data);
    }

    public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
        if (this.session != null)
            this.session.getBasicRemote().sendBinary(partialByte, isLast);
    }

    public void sendObject(Object data) throws IOException, EncodeException {
        if (this.session != null)
            this.session.getBasicRemote().sendObject(data);
    }
    */

    public interface IMessageCallback {
        void OnMessage(String message);
    }
}
