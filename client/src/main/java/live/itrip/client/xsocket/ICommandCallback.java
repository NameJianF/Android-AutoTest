package live.itrip.client.xsocket;

import java.io.IOException;

/**
 * Created by Feng on 2017/6/14.
 */
public interface ICommandCallback {

    void handleScreenshot(byte[] result) throws IOException;

    void handleStringMessage(String message);
}
