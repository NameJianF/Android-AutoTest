package live.itrip.client.device;

import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import live.itrip.client.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Feng on 2017/6/13.
 */
public class AdbCmdExecutor {
    /**
     * 执行外部指令并返回输出结果
     *
     * @param command
     * @param maxTimeToOutputResponse
     * @return
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static String executeCommand(String workDir, String command, int maxTimeToOutputResponse)
            throws ShellCommandUnresponsiveException, IOException {
        DefaultShellOutputReceiver rcvr = new DefaultShellOutputReceiver();
        executeCommand(workDir, command, rcvr, maxTimeToOutputResponse);
        return rcvr.getOutput();
    }

    private static void executeCommand(String workDir, String command,
                                       IShellOutputReceiver rcvr, int maxTimeToOutputResponse)
            throws ShellCommandUnresponsiveException, IOException {
        final int WAIT_TIME = 5;// spin-wait sleep, in ms
        Process proc = null;
        InputStream input = null;// BufferedReader
        try {
            List<String> cmds = parseCmds(command);
            ProcessBuilder processBuilder = new ProcessBuilder(cmds);
            processBuilder.directory(new File(workDir));
            /*
             * 设置此进程生成器的 redirectErrorStream 属性 如果此属性为 true，则任何由通过此对象的 start()
			 * 方法启动的后续子进程生成的错误输出都将与标准输出合并， 因此两者均可使用 Process.getInputStream()
			 * 方法读取。这使得关联错误消息和相应的输出变得更容易。 初始值为false。
			 */
            processBuilder.redirectErrorStream(true);
            proc = processBuilder.start();
            input = proc.getInputStream();
            byte[] buffer = new byte[2048];
            int timeToResponseCount = 0;
            while (true) {
                if (rcvr != null && rcvr.isCancelled()) {
                    break;
                }
                int count = 0;
                // 如果不判断会阻塞
                if (input.available() > 0) {
                    count = input.read(buffer);
                }
                if (count < 0) {
                    System.out.println("execute '" + command
                            + " on device : EOF hit. Read: " + count);
                } else if (count == 0) {
                    try {
                        int wait = WAIT_TIME * 5;
                        timeToResponseCount += wait;
                        if (maxTimeToOutputResponse > 0
                                && timeToResponseCount > maxTimeToOutputResponse) {
                            throw new ShellCommandUnresponsiveException();
                        }
                        Thread.sleep(wait);
                    } catch (InterruptedException ie) {
                    }
                } else {
                    // reset timeout
                    timeToResponseCount = 0;
                    // send data to receiver if present
                    if (rcvr != null) {
                        rcvr.addOutput(buffer, 0, count);
                    }
                }
                // 内容写出完毕后检查进程是否结束
                try {

                    proc.exitValue();

                    //Zipalign 执行在进程退出后才会输出相应的日志，所以增加相应的处理
                    count = 0;
                    if (input.available() > 0) {
                        count = input.read(buffer);
                    }
                    if (count < 0) {
                        break;
                    } else if (count == 0) {
                        break;
                    } else {
                        // reset timeout
                        timeToResponseCount = 0;
                        // send data to receiver if present
                        if (rcvr != null) {
                            rcvr.addOutput(buffer, 0, count);
                        }
                    }
                } catch (IllegalThreadStateException itse) {
                    Logger.error(itse.getMessage(), itse);
                }
            }
        } finally {
            if (proc != null) {
                proc.destroy();
            }
        }
    }

    private static List<String> parseCmds(String cmd) {
        Pattern p = Pattern.compile("([\\\"'](\\S+)[\\\"']|[\\\"']([^\\\"^']+)[\\\"']|\\S+)(\\s+|$)");
        Matcher m = p.matcher(cmd);
        List<String> ss = new ArrayList<String>();
        while (m.find()) {
            if (null != m.group(3))
                ss.add(m.group(3));
            else if (null != m.group(2))
                ss.add(m.group(2));
            else
                ss.add(m.group(1));
        }
        return ss;
    }

    public static final class DefaultShellOutputReceiver extends
            MultiLineReceiver {
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
}
