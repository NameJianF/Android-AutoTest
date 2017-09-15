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

    /**
     * adb shell dumpsys SurfaceFlinger --latency packageName
     *
     * @param packageName
     * @return
     */
    public static float execCommandForFPS(String packageName) {
        int BUFFER_SIZE = 128;
        int BUFFER_NUMBER = 3;
        float fps = 0f;

        Pattern CHECK_MATCHER = Pattern.compile("^[\\d\\s]+$");
        String PENDING_FENCE_TIME = new Long(Long.MAX_VALUE).toString();

        List<List<String>> mFrameBufferData = new ArrayList<List<String>>(BUFFER_SIZE);
        String command = String.format("dumpsys SurfaceFlinger --latency %s", packageName);
        List<String> contentList = execCommands2List(command);

        try {
            for (String line : contentList) {
                Matcher matcher = CHECK_MATCHER.matcher(line);
                if (!matcher.matches()) continue;

                String[] bufferValues = line.split("\\s+");

                if (bufferValues.length == 1) {
                    if (line.trim().isEmpty())
                        continue;

                    if (mFrameBufferData.isEmpty()) {
                        //mRefreshPeriod = Long.parseLong(line.trim());
                        continue;
                    } else {
                        fps = (float) getFrameRate(mFrameBufferData);
//                        parent.setLaterFps(fps);
                        //mRefreshPeriod = Long.parseLong(line.trim());
                        mFrameBufferData.clear();
                        continue;
                    }
                }

                if (bufferValues.length != BUFFER_NUMBER)
                    return fps;

                if (bufferValues[0].trim().compareTo("0") == 0) {
                    continue;
                } else if (bufferValues[1].trim().compareTo(PENDING_FENCE_TIME) == 0) {
                    Log.d(Main.LOGTAG, "the data contains unfinished frame time");
                    continue;
                }
                List<String> delayArray = Arrays.asList(bufferValues);
                mFrameBufferData.add(delayArray);
            }


        } catch (Exception e) {
            Log.e(Main.LOGTAG, "copy fail", e);
        }
        Log.v(Main.LOGTAG, "finish");

        return fps;
    }

    /**
     * Calculate frame rate
     *
     * @return
     */
    private static double getFrameRate(List<List<String>> mFrameBufferData) {
        int mFrameLatencySampleSize = mFrameBufferData.size() - 1;
        long startTime = Long.parseLong(mFrameBufferData.get(0).get(1));
        long endTime = Long.parseLong(mFrameBufferData.get(mFrameLatencySampleSize).get(1));
        long totalDuration = endTime - startTime;
        return (double) ((mFrameLatencySampleSize - 1) * Math.pow(10, 9)) / totalDuration;
    }

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
