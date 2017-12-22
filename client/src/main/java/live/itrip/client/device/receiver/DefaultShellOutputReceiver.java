package live.itrip.client.device.receiver;

import com.android.ddmlib.MultiLineReceiver;

public class DefaultShellOutputReceiver extends MultiLineReceiver {
    private static final String EOF = "\r\n";
    private boolean isCancel = false;
    private StringBuffer output = new StringBuffer();

    @Override
    public void processNewLines(String[] arg0) {
        for (String line : arg0) {
            if (line.length() > 0) {
                output.append(line + EOF);
            }
        }
    }

    /***
     * 获取所有输出结果
     *
     * @return
     */
    public String getOutput() {
        return output.toString();
    }

    /**
     * 取消执行
     */
    public void cancel() {
        isCancel = true;
    }

    @Override
    public boolean isCancelled() {
        return isCancel;
    }

}