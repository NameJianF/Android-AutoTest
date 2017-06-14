package vysor.renderscript;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.Matrix4f;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.Short4;
import android.renderscript.Type;

public class ScriptC_uchar4ToX extends ScriptC_Base {
    private static final int mExportForEachIdx_root = 0;
    private static final int mExportVarIdx_gTransformMatrix = 0;
    private static final int mExportVarIdx_gTranslate = 1;
    private Element __U8_4;
    private FieldPacker __rs_fp_U8_4;
    private Element __to;
    private Matrix4f mExportVar_gTransformMatrix;
    private Short4 mExportVar_gTranslate;

    public ScriptC_uchar4ToX(RenderScript rs, String scriptName, Element to, byte[] bitcode, String cacheDir) {
        super(rs, scriptName, bitcode, cacheDir);
        this.__to = to;
        this.__U8_4 = Element.U8_4(rs);
    }

    public Element getTo() {
        return this.__to;
    }

    public synchronized void set_gTransformMatrix(Matrix4f v) {
        this.mExportVar_gTransformMatrix = v;
        FieldPacker fp = new FieldPacker(64);
        fp.addMatrix(v);
        setVar(0, fp);
    }

    public Matrix4f get_gTransformMatrix() {
        return this.mExportVar_gTransformMatrix;
    }

    public synchronized void set_gTranslate(Short4 v) {
        this.mExportVar_gTranslate = v;
        FieldPacker fp = new FieldPacker(4);
        fp.addU8(v);
        setVar(1, fp, this.__U8_4, new int[]{1});
    }

    public Short4 get_gTranslate() {
        return this.mExportVar_gTranslate;
    }

    public void forEach_root(Allocation ain, Allocation aout) {
        if (!ain.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else if (aout.getType().getElement().isCompatible(this.__to)) {
            Type t0 = ain.getType();
            Type t1 = aout.getType();
            if (t0.getCount() == t1.getCount() && t0.getX() == t1.getX() && t0.getY() == t1.getY() && t0.getZ() == t1.getZ() && t0.hasFaces() == t1.hasFaces() && t0.hasMipmaps() == t1.hasMipmaps()) {
                forEach(0, ain, aout, null);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException("Type mismatch with target!");
        }
    }
}
