package live.itrip.agent.renderscript;

import android.renderscript.RenderScript;
import android.renderscript.ScriptC;

import java.lang.reflect.Method;

public class ScriptC_Base extends ScriptC {
    public ScriptC_Base(RenderScript rs, String scriptName, byte[] bitcode, String cacheDir) {
        super(getScript(rs, scriptName, bitcode, cacheDir), rs);
    }

    private static int getScript(RenderScript rs, String scriptName, byte[] bitcode, String cacheDir) {
        try {
            Method nScriptCCreate = rs.getClass().getDeclaredMethod("nScriptCCreate", new Class[]{String.class, String.class, byte[].class, Integer.TYPE});
            nScriptCCreate.setAccessible(true);
            Object ret = nScriptCCreate.invoke(rs, new Object[]{scriptName, cacheDir, bitcode, Integer.valueOf(bitcode.length)});
            if (ret instanceof Integer) {
                return ((Integer) ret).intValue();
            }
            return (int) ((Long) ret).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("rs fail");
        }
    }
}
