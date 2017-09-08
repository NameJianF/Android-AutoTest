// IWindowManager.aidl
package android.view;

import android.view.IRotationWatcher;
import android.graphics.Point;

// Declare any non-default types here with import statements

interface IWindowManager {

       void getInitialDisplaySize(int displayId, out Point size);

       void getBaseDisplaySize(int displayId, out Point size);

       void getRealDisplaySize(out Point paramPoint);

       int getRotation();

       int watchRotation(IRotationWatcher watcher, int displayId);
}
