package live.itrip.client.common;

/**
 * Created by Feng on 2017/5/12.
 */
public class Config {

    public static String CLIENT_VERSION;

    public static void printValues() {
        System.err.println(String.format("CLIENT_VERSION=%s", CLIENT_VERSION));
    }
}
