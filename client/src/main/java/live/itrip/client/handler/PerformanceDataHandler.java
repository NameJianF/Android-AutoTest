package live.itrip.client.handler;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import org.json.JSONObject;

import java.time.LocalDateTime;

public class PerformanceDataHandler {

    public static void hanlDatas(JSONObject json
            , LineChartHandler handlerMemory
            , LineChartHandler handlerCpu
            , LineChartHandler handlerFps
            , LineChartHandler handlerNetwork) {

        LocalDateTime time = LocalDateTime.now();
        if (json.has("DeviceMemoryInfo")) {
            int value = 0;
            /**
            if (json.has("AppMemoryInfo")) {
                JSONObject objMem = json.getJSONObject("AppMemoryInfo");
                if (objMem.has("memPids")) {
                    int length = objMem.getInt("memPids");
                    for (int i = 0; i < length; i++) {
                        value += objMem.getInt("dalvikPrivateDirty[" + i + "]");
                    }
                }
            } else
            **/
            {
                JSONObject objMem = json.getJSONObject("DeviceMemoryInfo");
                int total = objMem.getInt("totalMem");
                int avail = objMem.getInt("availMem");
                value = total - avail;
                value = value / 1024 / 1024;
            }
            final int tmp = value;
            Platform.runLater(() -> {
                handlerMemory.addData(new XYChart.Data<>(time, tmp));
            });
        }

        if (json.has("cpu")) {
            try {
                JSONObject jsoncpu = json.getJSONObject("cpu");
                int cpu = jsoncpu.getInt("user");
                Platform.runLater(() -> {
                    handlerCpu.addData(new XYChart.Data<>(time, cpu));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (json.has("fps")) {
            JSONObject jsonfps = json.getJSONObject("fps");
            int total = jsonfps.getInt("totalFrames");
            int janky = jsonfps.getInt("jankyFrames");
            int fps = jsonfps.getInt("fps");
            Platform.runLater(() -> {
                handlerFps.addData(new XYChart.Data<>(time, fps));
            });
        }
        if (json.has("network")) {
            JSONObject jsonnetwork = json.getJSONObject("network");
            long mobileRxBytes = jsonnetwork.getLong("MobileRxBytes");
            long mobileTxBytes = jsonnetwork.getLong("MobileTxBytes");
            long totalRxBytes = jsonnetwork.getLong("TotalRxBytes");
            long totalTxBytes = jsonnetwork.getLong("TotalTxBytes");
            long uidRxBytes = 1;
            long uidTxBytes = 1;
            if (jsonnetwork.has("UidRxBytes")) {
                uidRxBytes = jsonnetwork.getLong("UidRxBytes");
            }
            if (jsonnetwork.has("UidRxBytes")) {
                uidTxBytes = jsonnetwork.getLong("UidTxBytes");
            }
            long tmp = uidRxBytes / 1024 / 1024;
            Platform.runLater(() -> {
                handlerNetwork.addData(new XYChart.Data<>(time, tmp));
            });
        }
    }
}
