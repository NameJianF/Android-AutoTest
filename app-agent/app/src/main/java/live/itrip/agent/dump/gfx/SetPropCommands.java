package live.itrip.agent.dump.gfx;

/**
 * Created by Feng on 2017/9/19.
 */

public class SetPropCommands {

    /**
     *  System property used to enable or disable hardware rendering profiling.
     * The default value of this property is assumed to be false.
     *
     * When profiling is enabled, the adb shell dumpsys gfxinfo command will
     * output extra information about the time taken to execute by the last
     * frames.
     *
     * Possible values:
     * "true", to enable profiling
     * "visual_bars", to enable profiling and visualize the results on screen
     * "false", to disable profiling
     */
//#define PROPERTY_PROFILE "debug.hwui.profile"
//            #define PROPERTY_PROFILE_VISUALIZE_BARS "visual_bars"
    /**
     * System property used to specify the number of frames to be used
     * when doing hardware rendering profiling.
     * The default value of this property is #PROFILE_MAX_FRAMES.
     * <p>
     * When profiling is enabled, the adb shell dumpsys gfxinfo command will
     * output extra information about the time taken to execute by the last
     * frames.
     * <p>
     * Possible values:
     * "60", to set the limit of frames to 60
     */
//            #define PROPERTY_PROFILE_MAXFRAMES "debug.hwui.profile.maxframes"


    private static final String PROPERTY_PROFILE_FALSE = "setprop debug.hwui.profile false";
    private static final String PROPERTY_PROFILE_VISUALIZE_BARS = "setprop debug.hwui.profile visual_bars";
    private static final String PROPERTY_PROFILE_TRUE = "setprop debug.hwui.profile true";

}
