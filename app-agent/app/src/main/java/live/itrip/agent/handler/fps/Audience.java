package live.itrip.agent.handler.fps;

/**
 * Created by Feng on 2017/9/15.
 */

public interface Audience {
    void heartbeat(double fps);
}