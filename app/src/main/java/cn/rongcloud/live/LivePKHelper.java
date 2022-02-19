package cn.rongcloud.live;

/**
 * @author lihao
 * @project LiveRoomQuickDemo
 * @date 2022/1/13
 * @time 5:50 下午
 */
public class LivePKHelper {

    private static LivePKHelper livePKHelper;

    public static LivePKHelper getLivePKHelper(){
        if (livePKHelper==null){
            livePKHelper=new LivePKHelper();
        }
        return livePKHelper;
    }

    public static void releasePK(){
        livePKHelper=null;
    }
    public String inviterRoomId;
    public String inviterId;

    public String getInviterRoomId() {
        return inviterRoomId;
    }

    public void setInviterRoomId(String inviterRoomId) {
        this.inviterRoomId = inviterRoomId;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }


    /**
     * pk中 被邀请者信息
     * 1.发起邀请api是保存
     * 2.取消邀请时释放（手动）
     * 3.接收到邀请响应释放
     */
    public String inviteeRoomId;
    public String inviteeId;

    public String getInviteeRoomId() {
        return inviteeRoomId;
    }

    public void setInviteeRoomId(String inviteeRoomId) {
        this.inviteeRoomId = inviteeRoomId;
    }

    public String getInviteeId() {
        return inviteeId;
    }

    public void setInviteeId(String inviteeId) {
        this.inviteeId = inviteeId;
    }

}
