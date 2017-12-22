package live.itrip.agent.service;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import live.itrip.agent.BuildConfig;
import live.itrip.agent.ExecCommands;
import live.itrip.agent.Main;
import live.itrip.agent.handler.BroadcastHandler;
import live.itrip.agent.util.InternalApi;
import live.itrip.agent.util.LogUtils;

/**
 * Created by Feng on 2017/9/26.
 *
 * @author JianF
 */

public class AccessibilityServiceHelper {
    private static final String CMD_1 = "settings put secure enabled_accessibility_services live.itrip.agent/live.itrip.agent.service.AgentAccessibilityService";
    private static final String CMD_2 = "settings put secure accessibility_enabled ";
    private static final String PACKAGE_NAME = "live.itrip.agent";
    private static final String SERVICE_NAME = "live.itrip.agent.service.AgentAccessibilityService";
    private static final String START_SERVICE_RECEIVER = "live.itrip.agent.receiver.StartServiceReceiver";

    /**
     * 设置辅助服务
     * 1：开启
     * 0：关闭
     */
    public static void setAccessibilityService(int flag) {
        LogUtils.d("Hello, I am started by app_process! ");

        StringBuffer buffer = ExecCommands.execCommands(CMD_1);
        LogUtils.d("openAccessibilityService cmd1 result : " + buffer.toString());

        buffer = ExecCommands.execCommands(CMD_2 + flag);
        LogUtils.d("openAccessibilityService cmd2 result : " + buffer.toString());
    }

    /**
     * 启动 agent 辅助服务
     * 1. 打开服务权限 AccessibilityServiceHelper.setAccessibilityService(1);
     * 2. 启动服务
     * 3. 关闭服务权限
     */
    public static void startAccessibilityService() {
        // 打开服务权限
        AccessibilityServiceHelper.setAccessibilityService(1);

        Intent intent = new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, START_SERVICE_RECEIVER));
        intent.putExtra("pkg", PACKAGE_NAME);
        intent.putExtra("serviceName", SERVICE_NAME);
        try {
            LogUtils.d("broadcastIntent >>>>>>>>>>>>>> " + InternalApi.getBroadcastIntent().getParameterTypes().length);
            BroadcastHandler.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
