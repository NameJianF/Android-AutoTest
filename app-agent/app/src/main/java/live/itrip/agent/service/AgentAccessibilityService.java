package live.itrip.agent.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import live.itrip.agent.Main;
import live.itrip.agent.util.LogUtils;


/**
 * Created by Feng on 2017/9/26.
 * 1. 监控activity变化
 * 2. 获取点击控件信息
 */

public class AgentAccessibilityService extends AccessibilityService {

//    private String currentPakageName;
//    private String currentActivity;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

//        if (Build.VERSION.SDK_INT >= 16)
        //Just in case this helps
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            LogUtils.i("CurrentPackageName:" + componentName.getPackageName());
//            currentPakageName = componentName.getPackageName();

            ActivityInfo activityInfo = tryGetActivity(componentName);
            boolean isActivity = activityInfo != null;
            if (isActivity) {
//                currentActivity = componentName.flattenToShortString();
                LogUtils.i("CurrentActivity:" + componentName.flattenToShortString());

                // dump view nodes
                AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
                // get listview
                showAllText(accessibilityNodeInfo);
//                nodeInfoActivity = accessibilityNodeInfo;
            }
        }
    }

    private void showAllText(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo.getChildCount() == 0) {
            if (accessibilityNodeInfo.getText() != null) {
                LogUtils.v("node text :" + accessibilityNodeInfo.getText());
            }
            LogUtils.v("node id :" + accessibilityNodeInfo.toString());
            LogUtils.v("node class name: " + accessibilityNodeInfo.getClassName());
            LogUtils.v("node : " + accessibilityNodeInfo.toString());
        } else {
            if (accessibilityNodeInfo.getClassName().equals("android.widget.ListView")) {
                LogUtils.v("node : " + accessibilityNodeInfo.toString());
//                nodeInfoListView = accessibilityNodeInfo;
                return;
            }

            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                showAllText(accessibilityNodeInfo.getChild(i));
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }
}