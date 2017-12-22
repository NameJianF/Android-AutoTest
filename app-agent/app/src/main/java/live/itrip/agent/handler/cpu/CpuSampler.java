package live.itrip.agent.handler.cpu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import live.itrip.agent.util.LogUtils;

/**
 * Created on 2017/9/15.
 *
 * @author JianF
 */

public class CpuSampler {
    private static final Object lock = new Object();
    private static final int BUFFER_SIZE = 1000;
    private long mUserLast = 0;
    private long mSystemLast = 0;
    private long mIdleLast = 0;
    private long mIoWaitLast = 0;
    private long mTotalLast = 0;
    private long mAppCpuTimeLast = 0;

    private static CpuSampler instance;

    public static CpuSampler getInstance() {
        if (instance == null) {
            synchronized (lock) {
                instance = new CpuSampler();
            }
        }
        return instance;
    }

    public CpuSampler() {

    }


    public JSONObject getCpuRateInfo(int mPid) {
        BufferedReader cpuReader = null;
        BufferedReader pidReader = null;

        try {
            cpuReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), BUFFER_SIZE);
            String cpuRate = cpuReader.readLine();
            if (cpuRate == null) {
                cpuRate = "";
            }

            // TODO
            if (mPid == 0) {
                mPid = android.os.Process.myPid();
            }
            String pidCpuRate = "";
            if (mPid > 0) {
                pidReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + mPid + "/stat")), BUFFER_SIZE);
                pidCpuRate = pidReader.readLine();
                if (pidCpuRate == null) {
                    pidCpuRate = "";
                }
            } else {
                pidCpuRate = "";
            }
            return parse(cpuRate, pidCpuRate);
        } catch (Throwable throwable) {
            LogUtils.e("getCpuRateInfo: ", throwable);
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException exception) {
                LogUtils.e("getCpuRateInfo: ", exception);
            }
        }
        return null;
    }

    private void reset() {
        mUserLast = 0;
        mSystemLast = 0;
        mIdleLast = 0;
        mIoWaitLast = 0;
        mTotalLast = 0;
        mAppCpuTimeLast = 0;
    }

    private JSONObject parse(String cpuRate, String pidCpuRate) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        String[] cpuInfoArray = cpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            return jsonObject;
        }

        long user = Long.parseLong(cpuInfoArray[2]);
        long nice = Long.parseLong(cpuInfoArray[3]);
        long system = Long.parseLong(cpuInfoArray[4]);
        long idle = Long.parseLong(cpuInfoArray[5]);
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        long total = user + nice + system + idle + ioWait
                + Long.parseLong(cpuInfoArray[7])
                + Long.parseLong(cpuInfoArray[8]);

        String[] pidCpuInfoList = pidCpuRate.split(" ");
        if (pidCpuInfoList.length < 17) {
            return jsonObject;
        }

        long appCpuTime = Long.parseLong(pidCpuInfoList[13])
                + Long.parseLong(pidCpuInfoList[14])
                + Long.parseLong(pidCpuInfoList[15])
                + Long.parseLong(pidCpuInfoList[16]);

        if (mTotalLast != 0) {
            long idleTime = idle - mIdleLast;
            long totalTime = total - mTotalLast;

            // cpu:36% app:-22907% [user:14% system:18% ioWait:0% ]

            jsonObject.put("cpu", (totalTime - idleTime) * 100L / totalTime);
            jsonObject.put("app", (appCpuTime - mAppCpuTimeLast) * 100L / totalTime);
            long tmp = (user - mUserLast) * 100L / totalTime;
            if (tmp > 100 || tmp < 0) {
                tmp = 0;
            }
            jsonObject.put("user", tmp);
            jsonObject.put("system", (system - mSystemLast) * 100L / totalTime);
            jsonObject.put("ioWait", (ioWait - mIoWaitLast) * 100L / totalTime);
        }
        mUserLast = user;
        mSystemLast = system;
        mIdleLast = idle;
        mIoWaitLast = ioWait;
        mTotalLast = total;
        mAppCpuTimeLast = appCpuTime;

        return jsonObject;
    }
}