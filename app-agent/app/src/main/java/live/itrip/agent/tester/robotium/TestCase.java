package live.itrip.agent.tester.robotium;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * Description:
 *
 * @author JianF
 *         Date:  2017/11/6
 *         Time:  18:00
 *         Modify:
 */
public class TestCase extends ActivityInstrumentationTestCase2<Activity> {
    private Solo solo;

    public TestCase(String pkg, Class<Activity> activityClass) {
        super(pkg, activityClass);
    }

    /**
     * 运行测试用例之前做一些准备工作
     *
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    /**
     * 测试用例运行完成后做收尾工作
     *
     * @throws Exception
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        solo.finishOpenedActivities();
    }
}
