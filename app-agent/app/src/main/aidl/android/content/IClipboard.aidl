// IClipboard.aidl
package android.content;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.IOnPrimaryClipChangedListener;

// Declare any non-default types here with import statements

interface IClipboard {
       void setPrimaryClip(in ClipData clip, String callingPackage);
       ClipData getPrimaryClip(String pkg);
       ClipDescription getPrimaryClipDescription(String callingPackage);
       boolean hasPrimaryClip(String callingPackage);
       void addPrimaryClipChangedListener(in IOnPrimaryClipChangedListener listener,
               String callingPackage);
       void removePrimaryClipChangedListener(in IOnPrimaryClipChangedListener listener);
       /**
        * Returns true if the clipboard contains text; false otherwise.
        */
       boolean hasClipboardText(String callingPackage);
}
