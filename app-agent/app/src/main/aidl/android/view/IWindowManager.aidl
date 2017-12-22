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

        /**
            * Watch the rotation of the specified screen.  Returns the current rotation,
            * calls back when it changes.
            */
           int watchRotation(IRotationWatcher watcher, int displayId);
}
