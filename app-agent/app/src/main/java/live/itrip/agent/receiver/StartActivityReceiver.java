package live.itrip.agent.receiver;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import live.itrip.agent.util.LogUtils;

/**
 * Created on 2017/9/19.
 * @author JianF
 */

public class StartActivityReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(intent);
        String pkg = intent.getStringExtra("pkg");
        String activity = intent.getStringExtra("activity");
        ComponentName componentName = new ComponentName(pkg, activity);
        i.setComponent(componentName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        try {
//            attachContext();
//        } catch (Exception ex) {
//            LogUtils.e(ex.getMessage(), ex);
//        }

        LogUtils.e("start activity:" + activity);

        context.startActivity(i);
    }


    /**
     * hook Instrumentation  execStartActivity
     *
     * @throws Exception
     */
    public static void attachContext() throws Exception {
        // 先获取到当前的ActivityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        // 拿到原始的 mInstrumentation字段
        Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        mInstrumentationField.setAccessible(true);
        Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);

//        LogUtils.d("Instrumentation PackageName[" + mInstrumentation.getTargetContext()+ "]");


        // 创建代理对象
        Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);

        // 偷梁换柱
        mInstrumentationField.set(currentActivityThread, evilInstrumentation);
    }

    public static class EvilInstrumentation extends Instrumentation {
        // ActivityThread中原始的对象, 保存起来
        Instrumentation mBase;

        public EvilInstrumentation(Instrumentation base) {
            mBase = base;
        }

        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {

            // Hook之前, XXX到此一游!
            LogUtils.d("\n执行了startActivity, 参数如下: \n" + "who = [" + who.getPackageName() + "], " +
                    "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                    "\ntarget = [" + target + "], \nintent = [" + intent +
                    "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");

            // 开始调用原始的方法, 调不调用随你,但是不调用的话, 所有的startActivity都失效了.
            // 由于这个方法是隐藏的,因此需要使用反射调用;首先找到这个方法
            try {
                Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                        "execStartActivity",
                        Context.class,
                        IBinder.class,
                        IBinder.class,
                        Activity.class,
                        Intent.class,
                        int.class,
                        Bundle.class);
                execStartActivity.setAccessible(true);
                return (ActivityResult) execStartActivity.invoke(mBase, who,
                        contextThread, token, target, intent, requestCode, options);

//                Method execStartActivity = Instrumentation.class.getDeclaredMethod(
//                        "startActivitySync",
//                        Intent.class);
//                execStartActivity.setAccessible(true);
//                Activity activity = (Activity) execStartActivity.invoke(mBase, intent);
//
//                LogUtils.d("startActivitySync result activity[" + activity + "]");
//                LogUtils.d("startActivitySync result activity PackageName [" + activity.getPackageName() + "]");

            } catch (Exception e) {
                // 某该死的rom修改了  需要手动适配
                throw new RuntimeException(e);
            }
        }
    }
}
