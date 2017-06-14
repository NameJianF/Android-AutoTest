package xsocket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Feng on 2017/6/14.
 */

public class CommandExecResult {
    private int code = ErrorCode.UNKNOWN;
    private String data = "";

    public String toJsonString() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("code", this.code);
        object.put("data", this.data);
        return object.toString();
    }

    /**
     * error code
     *
     * @author Feng
     */
    public class ErrorCode {
        public final static int SUCESS = 0;
        public final static int UNKNOWN = -1;// 未知错误
        public final static int EXCEPTION = -2; // 执行异常
        public final static int MISSING_INFORMATION = 1;// 缺少信息
    }

    public static enum DataType {
        String, Image
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
