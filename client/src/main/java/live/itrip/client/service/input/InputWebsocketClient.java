package live.itrip.client.service.input;

import live.itrip.client.util.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ClientEndpoint(subprotocols = "mirror-protocol")
public class InputWebsocketClient {
    private final String name;
    private String response;
    private Throwable exception;
    private static final int REQUEST_TIMEOUT_SECS = 10;
    private final CountDownLatch messageLatch = new CountDownLatch(1);
    private Session session;

    public InputWebsocketClient(String name) {
        this.name = name;
    }

    @OnOpen
    public void onOpen(Session session) {
        Logger.debug("Sending request: '" + name + "' with session " + session.getId());
        this.session = session;
    }

    @OnMessage
    public void OnMessage(Session session, String message) {
        Logger.debug("Received response: '" + message + "' for request: '" + name + "' with session " + session.getId());
        response = message;
        messageLatch.countDown();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.error("Communication error, saying hello to '" + name + "' with session " + session.getId(), throwable);
        exception = throwable;
        messageLatch.countDown();
    }

    @OnClose
    public void onClose() {
//        connectToWebSocket();
    }

    public void sendText(String text) throws IOException {
        if (this.session != null)
            this.session.getBasicRemote().sendText(text);
    }

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

    /**
     * Blocks until either the server sends a response to the request, an communication error occurs
     * or the communication request times out.
     *
     * @return the server response message.
     * @throws TimeoutException     if the server does not respond before the timeout value is reached.
     * @throws InterruptedException if the communication thread is interrupted (e.g. thread.interrupt() is invoked on it for cancellation purposes).
     * @throws IOException          if a communication error occurs.
     */
    public String getResponse() throws TimeoutException, InterruptedException, IOException {
        if (messageLatch.await(REQUEST_TIMEOUT_SECS, TimeUnit.SECONDS)) {
            if (exception != null) {
                throw new IOException("Unable to say hello", exception);
            }
            return response;
        } else {
            throw new TimeoutException("Timed out awaiting server hello response for " + name);
        }
    }
}
