package live.itrip.client.controller;

/**
 * @author JianF
 * <p>
 * 日志记录
 */
public interface IClientLogListener {
    /**
     * write log
     * @param msg log
     */
    void writeClientLog(String msg);
}
