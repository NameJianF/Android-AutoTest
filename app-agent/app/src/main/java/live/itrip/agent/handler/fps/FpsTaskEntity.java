package live.itrip.agent.handler.fps;

import android.util.Log;
import android.view.Choreographer;

import live.itrip.agent.Main;
import live.itrip.agent.util.LogUtils;
import live.itrip.agent.util.ProcessUtils;

/**
 * Created by Feng on 2017/9/15.
 */
public class FpsTaskEntity {

    private FPSSampler mFPSSampler;

    public void onTaskInit() {
        this.mFPSSampler = new FPSSampler(Choreographer.getInstance());
        mFPSSampler.reset();
        mFPSSampler.start();
    }

    public Double onTaskRun() {
        if (mFPSSampler == null) {
            onTaskInit();
        }
        Double fps = mFPSSampler.getFPS();

        LogUtils.d(" >>>>>>>>>>>>>> FPS is : " + fps);
        LogUtils.d(" >>>>>>>>>>>>>> current pid : " + ProcessUtils.getCurrentPid());
        mFPSSampler.reset();
        return fps;
    }

    public void onTaskStop() {
        mFPSSampler.stop();
        mFPSSampler = null;
    }
}