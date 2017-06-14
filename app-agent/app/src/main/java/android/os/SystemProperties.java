package android.os;

public class SystemProperties {
    public static final int PROP_NAME_MAX = 31;
    public static final int PROP_VALUE_MAX = 91;

    private static native String native_get(String str);

    private static native String native_get(String str, String str2);

    private static native boolean native_get_boolean(String str, boolean z);

    private static native int native_get_int(String str, int i);

    private static native long native_get_long(String str, long j);

    private static native void native_set(String str, String str2);

    public static String get(String key) {
        if (key.length() <= 31) {
            return native_get(key);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static String get(String key, String def) {
        if (key.length() <= 31) {
            return native_get(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static int getInt(String key, int def) {
        if (key.length() <= 31) {
            return native_get_int(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static long getLong(String key, long def) {
        if (key.length() <= 31) {
            return native_get_long(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static boolean getBoolean(String key, boolean def) {
        if (key.length() <= 31) {
            return native_get_boolean(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static void set(String key, String val) {
        if (key.length() > 31) {
            throw new IllegalArgumentException("key.length > 31");
        } else if (val == null || val.length() <= 91) {
            native_set(key, val);
        } else {
            throw new IllegalArgumentException("val.length > 91");
        }
    }
}
