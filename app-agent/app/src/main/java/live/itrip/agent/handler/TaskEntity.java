package live.itrip.agent.handler;

import android.support.annotation.NonNull;

/**
 * Created by Feng on 2017/9/18.
 */

public interface TaskEntity<T> {

    void onTaskInit();

    @NonNull
    T onTaskRun();

    void onTaskStop();
}