package live.itrip.agent.handler.network;

import android.net.TrafficStats;

import org.json.JSONException;
import org.json.JSONObject;

import live.itrip.agent.util.ProcessUtils;

/**
 * Created by Feng on 2017/9/20.
 */

public class NetWorkSampler {

    /*
    * static long  getMobileRxBytes()  //获取通过移动数据网络收到的字节总数
    * static long  getMobileTxBytes()  //通过移动数据网发送的总字节数
    * static long  getTotalRxBytes()  //获取设备总的接收字节数
    * static long  getTotalTxBytes()  //获取设备总的发送字节数
    * static long  getUidRxBytes(int uid)  //获取指定uid的接收字节数
    * static long  getUidTxBytes(int uid) //获取指定uid的发送字节数
    */


    /**
     * @return
     */
    public static JSONObject getAppNetFlow(Object activityManager, String packageName) throws JSONException {
        JSONObject netflow = new JSONObject();

        int uid = ProcessUtils.getUidByPackageName(activityManager, packageName);
        netflow.put("MobileRxBytes", TrafficStats.getMobileRxBytes());
        netflow.put("MobileTxBytes", TrafficStats.getMobileTxBytes());
        netflow.put("TotalRxBytes", TrafficStats.getTotalRxBytes());
        netflow.put("TotalTxBytes", TrafficStats.getTotalTxBytes());
        if (uid > 0) {
            netflow.put("UidRxBytes", TrafficStats.getUidRxBytes(uid));
            netflow.put("UidTxBytes", TrafficStats.getUidTxBytes(uid));
        }
        return netflow;
    }
}
