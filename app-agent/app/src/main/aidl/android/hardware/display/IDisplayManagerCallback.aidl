// IDisplayManagerCallback.aidl
package android.hardware.display;

// Declare any non-default types here with import statements

interface IDisplayManagerCallback {
   oneway void onDisplayEvent(int displayId, int event);
}
