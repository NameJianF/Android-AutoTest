package live.itrip.client.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatetimeFormatUtils {
    /**
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(System.currentTimeMillis());
    }

    /**
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime(long timeMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(timeMillis);
    }

    /**
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
