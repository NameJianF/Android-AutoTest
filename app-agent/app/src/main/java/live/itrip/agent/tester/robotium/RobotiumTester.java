package live.itrip.agent.tester.robotium;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.robotium.solo.Solo;

import live.itrip.agent.Main;
import live.itrip.agent.tester.TestRunnerParam;
import live.itrip.agent.util.LogUtils;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * Description:
 *
 * @author JianF
 *         Date:  2017/11/6
 *         Time:  17:21
 *         Modify:
 */
public class RobotiumTester extends android.test.InstrumentationTestRunner {

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        TestRunnerParam inputDataStore = TestRunnerParam.getInstance();
        inputDataStore.init(getTargetContext());
        String activity = "";
        int retrytime = 0;
        try {
            ApplicationInfo applicationInfo = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            activity = applicationInfo.metaData.getString("activity");
            retrytime = applicationInfo.metaData.getInt("retrytime");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(activity)) {
            LogUtils.e("初始化失败，无法获取启动activity");
        } else {
            inputDataStore.setTargetActivity(activity);
        }
        if (retrytime > 1) {
            inputDataStore.setRetryTime(retrytime);
        }


    }

    @Override
    public void finish(int resultCode, Bundle results) {
        super.finish(resultCode, results);
    }
}
