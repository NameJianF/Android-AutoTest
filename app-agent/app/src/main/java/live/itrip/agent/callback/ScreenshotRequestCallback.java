package live.itrip.agent.callback;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.IWindowManager;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.ByteArrayOutputStream;

import live.itrip.agent.EncoderFeeder;
import live.itrip.agent.Main;

/**
 * Created by Feng on 2017/9/7.
 */
public class ScreenshotRequestCallback implements HttpServerRequestCallback {
    private IWindowManager iWindowManager;

    public ScreenshotRequestCallback(IWindowManager wm) {
        this.iWindowManager = wm;
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.getHeaders().set("Cache-Control", "no-cache");
        Log.i(Main.LOGTAG, "screenshot authentication success");
        try {
            Bitmap bitmap = EncoderFeeder.screenshot(iWindowManager);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
            bout.flush();
            response.send("image/jpeg", bout.toByteArray());
        } catch (Exception e) {
            response.code(500);
            response.send(e.toString());
        }
    }
}
