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
            Log.v(Main.LOGTAG, "log:" + stringBuffer.toString());
        } catch (Exception e) {
            Log.e(Main.LOGTAG, "copy fail", e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception ignored) {
            }
        }
        Log.v(Main.LOGTAG, "finish");
        return stringBuffer;
    }


    public static List<String> execCommands2List(String commands) {
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
                list.add(line);
            }
        } catch (Exception e) {
            Log.e(Main.LOGTAG, "copy fail", e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception ignored) {
            }
        }
        Log.v(Main.LOGTAG, "finish");
        return list;
    }
}
