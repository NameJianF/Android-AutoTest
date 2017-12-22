package live.itrip.agent;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import live.itrip.agent.util.LogUtils;

/**
 * Created by Feng on 2017/9/11.
 */

public class ExecCommands {

    public static StringBuffer execCommands(String commands) {
        StringBuffer stringBuffer = new StringBuffer();
        Process process = null;
        DataOutputStream dataOutputStream = null;
        try {
            process = Runtime.getRuntime().exec(commands);
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.flush();
            process.waitFor();
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            String separator = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append(separator);
            }
            LogUtils.v("log:" + stringBuffer.toString());
        } catch (
                Exception e)

        {
            LogUtils.e("copy fail", e);
        } finally

        {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception ignored) {
            }
        }
        LogUtils.v("finish");
        return stringBuffer;
    }


    public static List<String> execCommands2List(String commands, String strContain) {
        List<String> list = new ArrayList<>(20);
        Process process = null;
        DataOutputStream dataOutputStream = null;
        try {
            process = Runtime.getRuntime().exec(commands);
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.flush();
            process.waitFor();
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
//            String separator = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                if (strContain != null && strContain.length() > 0) {
                    if (line.contains(strContain)) {
                        list.add(line);
                    }
                } else {
                    list.add(line);
                }
            }
        } catch (Exception e) {
            LogUtils.e("copy fail", e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception ignored) {
            }
        }
//        Log.v(Main.LOGTAG, "finish");
        return list;
    }
}
