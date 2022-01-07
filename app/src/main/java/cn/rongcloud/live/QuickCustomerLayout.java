package cn.rongcloud.live;

import java.util.List;
import java.util.Map;

import cn.rongcloud.liveroom.api.RCRect;
import cn.rongcloud.liveroom.api.model.RCLiveSeatInfo;
import cn.rongcloud.liveroom.weight.RCLiveCanvas;
import cn.rongcloud.liveroom.weight.interfaces.IRCLiveLayout;
import cn.rongcloud.liveroom.weight.wrapper.RCLiveVideoWrapperView;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;

public class QuickCustomerLayout implements IRCLiveLayout {
    public final static int CUSTOMER_1V3 = 8;
    public final static int CUSTOMER_1V4 = 9;
    private final static int width = 720;
    private final static int height = 720;

    @Override
    public int createrIndex(int mixType) {
        return 0;
    }

    @Override
    public RCLiveCanvas configCanvas(int mixType) {
        return new RCLiveCanvas(width, height);
    }

    @Override
    public RCRect[] seatFrameRects(int mixType) {
        float size = height / 6.0f;
        float w = 1.0f * width + size;
        float x = width / w;
        if (CUSTOMER_1V3 == mixType) {
            return new RCRect[]{
                    new RCRect(0, 0, 1 - size / w, 1),
                    new RCRect(x, 0, size / w, 2.0f / 6),
                    new RCRect(x, 2.0f / 6, size / w, 2.0f / 6),
                    new RCRect(x, 4.0f / 6, size / w, 2.0f / 6),
            };
        } else if (CUSTOMER_1V4 == mixType) {
            return new RCRect[]{
                    new RCRect(0, 1.0f / 6, 1 - size / w, 1 - 1.0f / 6),
                    new RCRect(0, 0, 1, 1.0f / 6),
                    new RCRect(x, 1.0f / 6, size / w, 2.5f / 6),
                    new RCRect(x, 3.5f / 6, size / w, 2.5f / 6),
            };
        }
        return new RCRect[0];
    }

    @Override
    public void onBindVideoView(int mixType,
                                String roomOwnerUserId,
                                List<RCLiveSeatInfo> seatInfos,
                                List<RCLiveVideoWrapperView> videoList,
                                RCRTCVideoOutputStream localStram,
                                Map<String, RCRTCVideoInputStream> remoteVideoStream) {

    }
}
