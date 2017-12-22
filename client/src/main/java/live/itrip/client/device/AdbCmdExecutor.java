package live.itrip.client.device;

import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import live.itrip.client.device.receiver.DefaultShellOutputReceiver;
import live.itrip.client.util.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2017/6/13.
 *
 * @author JianF
 */
public class AdbCmdExecutor {
    private static final String REGEX = "([\\\"'](\\S+)[\\\"']|[\\\"']([^\\\"^']+)[\\\"']|\\S+)(\\s+|$)";

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
        final int waitTime = 5;// spin-wait sleep, in ms
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
                        int wait = waitTime * 5;
                        timeToResponseCount += wait;
                        if (maxTimeToOutputResponse > 0 && timeToResponseCount > maxTimeToOutputResponse) {
                            throw new ShellCommandUnresponsiveException();
                        }
                        Thread.sleep(wait);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
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

    public static StringBuffer executeCommandStartMain(String command) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static StringBuffer executeCommandByProcess(String command) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Process process = Runtime.getRuntime().exec(command);
            // 读取进程标准输出流
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
            // 关闭输入流
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stringBuffer;
    }


    private static List<String> parseCmds(String cmd) {
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(cmd);
        List<String> ss = new ArrayList<String>();
        while (m.find()) {
            if (null != m.group(3)) {
                ss.add(m.group(3));
            } else if (null != m.group(2)) {
                ss.add(m.group(2));
            } else {
                ss.add(m.group(1));
            }
        }
        return ss;
    }

}
