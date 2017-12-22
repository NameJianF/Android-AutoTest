package live.itrip.agent.responder;

import android.content.Context;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Created on 2017/12/5.
 *
 * @author Feng
 *         Description :
 *         Update :
 */

abstract public class AbstractResponder {
    Context context;

    protected AbstractResponder(Context context) {
        this.context = context;
    }

//    abstract public GeneratedMessageLite respond(Wire.Envelope envelope) throws InvalidProtocolBufferException;

    abstract public void cleanup();
}
