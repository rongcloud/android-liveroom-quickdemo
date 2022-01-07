package cn.rongcloud.live;

public enum SeatListFun {
    kick_out_room("踢出房间"),
    invite_enter_seat("邀请上麦");

    private String value;

    SeatListFun(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
