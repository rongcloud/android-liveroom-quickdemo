package cn.rongcloud.live;

import cn.rongcloud.liveroom.api.interfaces.RCLiveEventListener;
import cn.rongcloud.liveroom.api.interfaces.RCLiveLinkListener;
import cn.rongcloud.liveroom.api.interfaces.RCLivePKListener;
import cn.rongcloud.liveroom.api.interfaces.RCLiveSeatListener;

public abstract class LiveListener implements RCLiveEventListener, RCLiveLinkListener, RCLiveSeatListener , RCLivePKListener {
    abstract void set();

    abstract void release();
}
