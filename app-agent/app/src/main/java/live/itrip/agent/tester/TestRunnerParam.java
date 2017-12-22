package live.itrip.agent.tester;

import android.content.Context;
import android.util.Log;

import live.itrip.agent.Main;
import live.itrip.agent.util.LogUtils;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * Description:
 *
 * @author JianF
 *         Date:  2017/11/6
 *         Time:  18:57
 *         Modify:
 */
public class TestRunnerParam {
    private String targetPackage;
    private String targetActivity;
    private int retryTime = 1;

    public void init(Context context) {
        if (context != null) {
            setTargetPackage(context.getPackageName());
        } else {
            LogUtils.e("初始化失败：" + "无法获取目标apk");
        }
    }


    private static TestRunnerParam testRunnerParam = null;

    public static TestRunnerParam getInstance() {
        if (testRunnerParam == null) {
            testRunnerParam = new TestRunnerParam();
        }
        return testRunnerParam;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(String targetActivity) {
        this.targetActivity = targetActivity;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }
}
