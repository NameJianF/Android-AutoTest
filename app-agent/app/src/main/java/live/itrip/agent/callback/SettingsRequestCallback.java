package live.itrip.agent.callback;

import android.util.Log;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONObject;

import live.itrip.agent.ExecCommands;
import live.itrip.agent.Main;
import live.itrip.agent.dump.gfx.GfxInfoReader;
import live.itrip.agent.service.AccessibilityServiceHelper;
import live.itrip.agent.util.LogUtils;

/**
 * Created on 2017/9/7.
 * @author JianF
 */
public class SettingsRequestCallback implements HttpServerRequestCallback {
    private SettingsType settingsType;

    public SettingsRequestCallback(SettingsType settingsType) {
        this.settingsType = settingsType;
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.getHeaders().set("Cache-Control", "no-cache");
        LogUtils.i("settings success");
        try {
            response.setContentType("application/json;charset=utf-8");
            JSONObject object = new JSONObject();

            StringBuffer buffer = new StringBuffer();
            if (SettingsType.OPEN_ACCESSIBILITY_SERVICE.equals(this.settingsType)) {
                AccessibilityServiceHelper.setAccessibilityService(1);
            } else if (SettingsType.CLOSE_ACCESSIBILITY_SERVICE.equals(this.settingsType)) {
                AccessibilityServiceHelper.setAccessibilityService(0);
            } else if (SettingsType.PROPERTY_PROFILE_FALSE.equals(this.settingsType)) {
                buffer = ExecCommands.execCommands(GfxInfoReader.SetPropCommands.PROPERTY_PROFILE_FALSE);
            } else if (SettingsType.PROPERTY_PROFILE_VISUALIZE_BARS.equals(this.settingsType)) {
                buffer = ExecCommands.execCommands(GfxInfoReader.SetPropCommands.PROPERTY_PROFILE_VISUALIZE_BARS);
            } else if (SettingsType.PROPERTY_PROFILE_TRUE.equals(this.settingsType)) {
                buffer = ExecCommands.execCommands(GfxInfoReader.SetPropCommands.PROPERTY_PROFILE_TRUE);
            }

            object.put("code", 0);
            object.put("msg", buffer.toString());
            response.send(object.toString());
        } catch (Exception e) {
            response.code(500);
            response.send(e.toString());
        }
    }


    public enum SettingsType {
        // AccessibilityService
        OPEN_ACCESSIBILITY_SERVICE,
        CLOSE_ACCESSIBILITY_SERVICE,

        // gfxinfo
        PROPERTY_PROFILE_FALSE,
        PROPERTY_PROFILE_VISUALIZE_BARS,
        PROPERTY_PROFILE_TRUE;
    }
}
