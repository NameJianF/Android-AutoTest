package live.itrip.client.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * Description:
 *
 * @author JianF
 * Date:  2017/10/23
 * Time:  18:46
 * Modify:
 */
public class ThreadExecutor {
    private static ThreadPoolExecutor cachedThreadPool;

    /**
     * 需要设置 Runnable Name
     * Thread.currentThread().setName("threadName");
     *
     * @param runnable Runnable
     */
    public static void execute(Runnable runnable) {
        if (cachedThreadPool == null) {
            synchronized (ThreadPoolExecutor.class) {
                if (cachedThreadPool == null) {
                    cachedThreadPool = new ThreadPoolExecutor(
                            5,
                            50,
                            10L,
                            TimeUnit.SECONDS,
                            new SynchronousQueue<>(),
                            new ThreadFactoryBuilder().build());
                }
            }
        }
        cachedThreadPool.execute(runnable);
    }
}