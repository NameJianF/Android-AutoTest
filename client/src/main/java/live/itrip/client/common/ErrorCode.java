package live.itrip.client.common;

/**
 * Created on 2017/6/13.
 *
 * @author JianF
 */
public class ErrorCode {
    /***
     * 成功
     */
    public static final int SUCCESS = 0;

    /**
     * 执行指令失败，用于没有找到关键标记如安装的"Sucess"资源
     */
    public static final int EXEC_FAILED = 1;

    /**
     * 执行指令时，时间内没有找到关机标记--或进程未退出(如：ShellCommandUnresponsiveException)
     */
    public static final int EXEC_TIMEOUT = 10;

    /**
     * 执行指令时，发送异常（IOException）
     */
    public static final int EXEC_ERROR = 20;

    /**
     * app 解析失败
     */
    public static final int ERROR_ANALYZE_APP = 21;
}
