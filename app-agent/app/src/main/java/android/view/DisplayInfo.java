package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DisplayInfo implements Parcelable {
    public static final Creator<DisplayInfo> CREATOR = new Creator<DisplayInfo>() {
        public DisplayInfo createFromParcel(Parcel source) {
            throw new AssertionError("should be a framework class?");
        }

        public DisplayInfo[] newArray(int size) {
            throw new AssertionError("should be a framework class?");
        }
    };

    public int describeContents() {
        throw new AssertionError("should be a framework class?");
    }

    public void writeToParcel(Parcel dest, int flags) {
        throw new AssertionError("should be a framework class?");
    }
}
