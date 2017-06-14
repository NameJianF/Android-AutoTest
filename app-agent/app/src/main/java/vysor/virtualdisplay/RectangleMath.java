package vysor.virtualdisplay;

import android.graphics.Rect;

public class RectangleMath {
    public static Rect centerInside(int sourceWidth, int sourceHeight, int resizeWidth, int resizeHeight) {
        float ratio = Math.min(((float) resizeWidth) / ((float) sourceWidth), ((float) resizeHeight) / ((float) sourceHeight));
        if (ratio == 0.0f) {
            return null;
        }
        float transx = (((float) resizeWidth) - (((float) sourceWidth) * ratio)) / 2.0f;
        float transy = (((float) resizeHeight) - (((float) sourceHeight) * ratio)) / 2.0f;
        return new Rect((int) transx, (int) transy, (int) (((float) resizeWidth) - transx), (int) (((float) resizeHeight) - transy));
    }
}
