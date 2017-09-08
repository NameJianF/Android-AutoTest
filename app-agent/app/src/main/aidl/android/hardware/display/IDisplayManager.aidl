// IDisplayManager.aidl
package android.hardware.display;


import android.view.DisplayInfo;


// Declare any non-default types here with import statements

interface IDisplayManager {
    DisplayInfo getDisplayInfo(int displayId);
}
