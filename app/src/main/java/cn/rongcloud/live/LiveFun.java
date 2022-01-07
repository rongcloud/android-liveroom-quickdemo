package cn.rongcloud.live;

public enum LiveFun {
    seat_enter("上麦"),
    seat_left("下麦"),
    kict_out_seat("抱下麦"),
    // live
    live_seat_mute("静音"),
    live_seat_unmute("取消静音"),
    live_seat_audio_enable("开启音频"),
    live_seat_audio_unenable("关闭音频"),
    live_seat_video_enable("开启视频"),
    live_seat_video_unenable("关闭视频"),
    live_seat_lock("锁定麦位"),
    live_seat_unlock("解锁麦位"),
    live_kick_out_room("踢出房间"),
    live_switch_seat("切换麦位");
    private String value;

    LiveFun(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
