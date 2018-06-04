package live.itrip.agent.callback;

import android.graphics.Bitmap;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.ByteArrayOutputStream;

import live.itrip.agent.EncoderFeeder;
import live.itrip.agent.common.HttpErrorCode;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;

/**
 * Created on 2017/9/7.
 * @author JianF
 */
public class ScreenshotRequestCallback implements HttpServerRequestCallback {
    public ScreenshotRequestCallback() {
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.getHeaders().set("Cache-Control", "no-cache");
        LogUtils.i("screenshot authentication success");
        try {
            Bitmap bitmap = EncoderFeeder.screenshot(InternalApi.getWindowManager());
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
            bout.flush();
            response.send("image/jpeg", bout.toByteArray());
        } catch (Exception e) {
            response.code(HttpErrorCode.ERROR_CODE_500);
            response.send(e.toString());
        }
    }
}
