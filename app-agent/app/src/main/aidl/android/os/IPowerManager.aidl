// IPowerManager.aidl
package android.os;

// Declare any non-default types here with import statements

interface IPowerManager {

       void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, in WorkSource ws,
                   String historyTag);

       boolean isInteractive();

       boolean isScreenOn();

       void releaseWakeLock(IBinder lock, int flags);

       void reboot(String reason);
}
