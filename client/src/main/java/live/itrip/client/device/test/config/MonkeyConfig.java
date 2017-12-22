package live.itrip.client.device.test.config;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Description:
 *
 * @author JianF
 * Date:  2017/11/10
 * Time:  10:55
 * Modify:
 */
public class MonkeyConfig implements ITestConfig {
    public final static int EXEC_TYPE_COMMAND = 1;
    public final static int EXEC_TYPE_SCRIPT = 2;

    /**
     * 脚本文件
     */
    private List<String> scriptfile;
    /**
     * 执行方式：1，命令； 2，脚本文件
     */
    private int execType = EXEC_TYPE_COMMAND;

    // 事件参数
    /**
     * 随机事件总数
     */
    private Integer eventCount = 1000;
    /**
     * 随机数生成器种子
     */
    private Integer seed = 500;
    /**
     * 固定延迟时间：milliseconds, <=0 为不设置延迟
     */
    private Integer throttle = 500;
    /**
     * 触屏事件百分比
     */
    private Integer pctTouch = 40;
    /**
     * 滑动事件百分比
     */
    private Integer pctMotion = 25;
    /**
     * 轨迹球事件百分比
     */
    private Integer pctTrackBall;
    /**
     * 基本导航事件百分比：上下左右
     */
    private Integer pctNav = 5;
    /**
     * 主要导航事件百分比：Back Menu
     */
    private Integer pctMajorNav = 10;
    /**
     * 系统按键事件百分比：如：Home、Back、拨号、挂断、音量键等
     */
    private Integer pctSysKeys = 10;
    /**
     * 启动活动事件百分比：执行startActivity()函数，覆盖包中的Activity
     */
    private Integer pctAppSwitch = 10;
    /**
     * 其他事件百分比：如普通的按键消息、不常用的设备按钮等
     */
    private Integer pctAnyEvent = 0;

    // 调试选项
    /**
     * 是否执行初始启动，测试启动的Activity并不再生成事件
     */
    private boolean dbgNoEvents = false;
    /**
     * 是否在发送时间序列前、后生成性能分析报告
     */
    private boolean hprof = false;
    /**
     * 是否忽略 待测app崩溃
     */
    private boolean ignoreCrashes = false;
    /**
     * 是否忽略 native crash
     */
    private boolean ignoreNavtiveCrashes = false;
    /**
     * 是否忽略 超时错误
     */
    private boolean ignoreTimeouts = false;
    /**
     * 是否忽略 权限错误
     */
    private boolean ignoreSecurityExceptions = false;
    /**
     * 是否 通知系统停止发生错误的进程
     */
    private boolean killProcessAfterError = true;
    /**
     * 是否 执行由NDK等底层代码引起的崩溃时关机
     */
    private boolean monitorNativeCrashes = false;
    /**
     * 是否 等待调试器连接
     */
    private boolean waitDbg = false;

    public Integer getEventCount() {
        return eventCount;
    }

    public void setEventCount(Integer eventCount) {
        this.eventCount = eventCount;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getThrottle() {
        return throttle;
    }

    public void setThrottle(Integer throttle) {
        this.throttle = throttle;
    }

    public Integer getPctTouch() {
        return pctTouch;
    }

    public void setPctTouch(Integer pctTouch) {
        this.pctTouch = pctTouch;
    }

    public Integer getPctMotion() {
        return pctMotion;
    }

    public void setPctMotion(Integer pctMotion) {
        this.pctMotion = pctMotion;
    }

    public Integer getPctTrackBall() {
        return pctTrackBall;
    }

    public void setPctTrackBall(Integer pctTrackBall) {
        this.pctTrackBall = pctTrackBall;
    }

    public Integer getPctNav() {
        return pctNav;
    }

    public void setPctNav(Integer pctNav) {
        this.pctNav = pctNav;
    }

    public Integer getPctMajorNav() {
        return pctMajorNav;
    }

    public void setPctMajorNav(Integer pctMajorNav) {
        this.pctMajorNav = pctMajorNav;
    }

    public Integer getPctSysKeys() {
        return pctSysKeys;
    }

    public void setPctSysKeys(Integer pctSysKeys) {
        this.pctSysKeys = pctSysKeys;
    }

    public Integer getPctAppSwitch() {
        return pctAppSwitch;
    }

    public void setPctAppSwitch(Integer pctAppSwitch) {
        this.pctAppSwitch = pctAppSwitch;
    }

    public Integer getPctAnyEvent() {
        return pctAnyEvent;
    }

    public void setPctAnyEvent(Integer pctAnyEvent) {
        this.pctAnyEvent = pctAnyEvent;
    }

    public boolean isDbgNoEvents() {
        return dbgNoEvents;
    }

    public void setDbgNoEvents(boolean dbgNoEvents) {
        this.dbgNoEvents = dbgNoEvents;
    }

    public boolean isHprof() {
        return hprof;
    }

    public void setHprof(boolean hprof) {
        this.hprof = hprof;
    }

    public boolean isIgnoreCrashes() {
        return ignoreCrashes;
    }

    public void setIgnoreCrashes(boolean ignoreCrashes) {
        this.ignoreCrashes = ignoreCrashes;
    }

    public boolean isIgnoreTimeouts() {
        return ignoreTimeouts;
    }

    public void setIgnoreTimeouts(boolean ignoreTimeouts) {
        this.ignoreTimeouts = ignoreTimeouts;
    }

    public boolean isIgnoreSecurityExceptions() {
        return ignoreSecurityExceptions;
    }

    public void setIgnoreSecurityExceptions(boolean ignoreSecurityExceptions) {
        this.ignoreSecurityExceptions = ignoreSecurityExceptions;
    }

    public boolean isKillProcessAfterError() {
        return killProcessAfterError;
    }

    public void setKillProcessAfterError(boolean killProcessAfterError) {
        this.killProcessAfterError = killProcessAfterError;
    }

    public boolean isMonitorNativeCrashes() {
        return monitorNativeCrashes;
    }

    public void setMonitorNativeCrashes(boolean monitorNativeCrashes) {
        this.monitorNativeCrashes = monitorNativeCrashes;
    }

    public boolean isWaitDbg() {
        return waitDbg;
    }

    public void setWaitDbg(boolean waitDbg) {
        this.waitDbg = waitDbg;
    }

    public boolean isIgnoreNavtiveCrashes() {
        return ignoreNavtiveCrashes;
    }

    public void setIgnoreNavtiveCrashes(boolean ignoreNavtiveCrashes) {
        this.ignoreNavtiveCrashes = ignoreNavtiveCrashes;
    }

    public List<String> getScriptfile() {
        return scriptfile;
    }

    public void setScriptfile(List<String> scriptfile) {
        this.scriptfile = scriptfile;
    }

    public int getExecType() {
        return execType;
    }

    public void setExecType(int execType) {
        this.execType = execType;
    }

}
