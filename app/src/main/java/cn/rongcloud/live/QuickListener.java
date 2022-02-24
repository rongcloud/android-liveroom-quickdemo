package cn.rongcloud.live;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.rongcloud.authentication.AccoutManager;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.RCLiveMixType;
import cn.rongcloud.liveroom.api.callback.RCLiveCallback;
import cn.rongcloud.liveroom.api.callback.RCLiveResultCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.liveroom.api.model.RCLiveSeatInfo;
import cn.rongcloud.liveroom.api.model.RCLiveVideoPK;
import cn.rongcloud.liveroom.api.model.RCLivevideoFinishReason;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.base.RCRTCVideoFrame;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

public class QuickListener extends LiveListener {
    private final static String TAG = "QuickLiveEventListener";
    private final static QuickListener instance = new QuickListener();
    private WeakReference<Activity> reference;

    private QuickListener() {
        RongIMClient.getInstance().setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus connectionStatus) {
                if (connectionStatus == ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                    KToast.show("当前账号已在其他设备登录，请重新连接");
                    if (reference != null && null != reference.get()) {
                        reference.get().finish();
                    }
                }
            }
        });
    }

    public static QuickListener get() {
        return instance;
    }

    private boolean braodcast = false;

    public void resister(Activity activity, boolean braodcast) {
        this.braodcast = braodcast;
        reference = new WeakReference(activity);
        RCLiveEngine.getInstance().setLiveEventListener(this);
        RCLiveEngine.getInstance().setLivePKEventListener(this);
        RCLiveEngine.getInstance().getLinkManager().setLiveLinkListener(this);
        RCLiveEngine.getInstance().getSeatManager().setLiveSeatListener(this);
    }

    @Override
    public void onRoomInfoReady() {
        Logger.e(TAG, "onRoomInfoReady:");
    }

    @Override
    public void onRoomInfoUpdate(String key, String value) {
        Logger.e(TAG, "onRoomInfoUpdate: key " + key + " value = " + value);
    }

    @Override
    public void onUserEnter(String userId, int onlineCount) {

    }

    @Override
    public void onUserExit(String userId, int onlineCount) {

    }

    @Override
    public void onUserKickOut(String userId, String operatorId) {
        EToast.showToast("您已被踢出直播间");
        RCLiveEngine.getInstance().leaveRoom(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                reference.get().finish();
            }

            @Override
            public void onError(int i, RCLiveError rcLiveError) {

            }
        });

        Logger.e(TAG, "onUserKitOut: userId " + userId + " operatorId  = " + operatorId);
    }

    @Override
    public void onLiveVideoUpdate(List<String> lineMicUserIds) {
        Logger.e(TAG, "onLiveVideoUpdate:");
    }


    @Override
    public void onLiveVideoRequestChange() {
        Logger.e(TAG, "onLiveVideoRequestChanage:");
        if (!braodcast) return;
        if (null != reference) {
            ApiLiveDialogHelper.helper().dismissDialog();
            RCLiveEngine.getInstance().getLinkManager().getRequestLiveVideoIds(new RCLiveResultCallback<List<String>>() {
                @Override
                public void onResult(List<String> result) {
                    int count = null == result ? 0 : result.size();
                    if (count < 1) {
                        return;
                    }
                    // todo 这里只是演示 获取index = 0的申请者，实际是展示在申请列表中
                    String userId = result.get(0);
                    String name = AccoutManager.getAccoutName(userId);
                    ApiLiveDialogHelper.helper().showTipDialog(reference.get(), "提示", "'" + name + "' 申请上麦，是否接收", new IResultBack<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            if (result) {
                                RCLiveEngine.getInstance().getLinkManager().acceptRequest(userId, new RCLiveCallback() {
                                    @Override
                                    public void onSuccess() {
                                        EToast.showToast("接收申请成功");
                                    }

                                    @Override
                                    public void onError(int code, RCLiveError error) {
                                        EToast.showToast("接收申请失败");
                                        Logger.e(TAG, error);
                                    }
                                });
                            } else {
                                RCLiveEngine.getInstance().getLinkManager().rejectRequest(userId, new RCLiveCallback() {
                                    @Override
                                    public void onSuccess() {
                                        EToast.showToast("拒绝成功");
                                    }

                                    @Override
                                    public void onError(int code, RCLiveError error) {
                                        EToast.showToast("拒绝失败");
                                        Logger.e(TAG, error);
                                    }
                                });
                            }
                        }
                    });
                }

                @Override
                public void onError(int code, RCLiveError error) {
                    Logger.e(TAG, error.toString());
                }
            });
        }
    }

    @Override
    public void onLiveVideoRequestAccepted() {
        Logger.e(TAG, "onLiveVideoRequestAccepted:");
    }

    @Override
    public void onLiveVideoRequestRejected() {
        EToast.showToast("申请上麦被拒绝");
        Logger.e(TAG, "onLiveVideoRequestRejected:");
    }

    @Override
    public void onLiveVideoRequestCanceled() {
        Logger.e(TAG, "onLiveVideoRequestCanceled:");
    }

    @Override
    public void onLiveVideoInvitationReceived(String userId, int index) {
        Logger.e(TAG, "onliveVideoInvitationReceived:");
        if (null != reference) {
            String name = AccoutManager.getAccoutName(userId);
            ApiLiveDialogHelper.helper().showTipDialog(reference.get(), "邀请", "'" + name + "'邀请您上麦，是否接收", new IResultBack<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    if (result) {
                        RCLiveEngine.getInstance().getLinkManager().acceptInvitation(userId, index, new RCLiveCallback() {
                            @Override
                            public void onSuccess() {
                                EToast.showToast("接收邀请成功");
                            }

                            @Override
                            public void onError(int code, RCLiveError error) {
                                EToast.showToast("接收邀请失败");
                                Logger.e(TAG, error);
                            }
                        });
                    } else {
                        RCLiveEngine.getInstance().getLinkManager().rejectInvitation(userId, new RCLiveCallback() {
                            @Override
                            public void onSuccess() {
                                EToast.showToast("拒绝成功");
                            }

                            @Override
                            public void onError(int code, RCLiveError error) {
                                EToast.showToast("拒绝失败");
                                Logger.e(TAG, error);
                            }
                        });
                    }
                }
            });

        }
    }


    @Override
    public void onLiveVideoInvitationCanceled() {
        Logger.e(TAG, "onliveVideoInvitationCanceled:");
    }

    @Override
    public void onLiveVideoInvitationAccepted(String s) {
        Logger.e(TAG, "onliveVideoInvitationAccepted:");
    }

    @Override
    public void onLiveVideoInvitationRejected(String s) {
        Logger.e(TAG, "onliveVideoInvitationRejected:");
    }

    @Override
    public void onLiveVideoStarted() {
        Logger.e(TAG, "onLiveVideoStarted:");
    }

    @Override
    public void onLiveVideoStopped(RCLivevideoFinishReason reason) {
        Logger.e(TAG, "onLiveVideoStoped:");
    }

    @Override
    public void onReceiveMessage(Message message) {
        Logger.e(TAG, "onReceiveMessage:");
    }

    @Override
    public void onNetworkStatus(long delayMs) {
//        Logger.e(TAG, "onNetworkStatus:");
    }

    @Override
    public void onOutputSampleBuffer(RCRTCVideoFrame frame) {
//        Logger.e(TAG, "onOutputSampleBuffer:");
    }

    @Override
    public RCRTCConfig.Builder onInitRCRTCConfig(RCRTCConfig.Builder builder) {
        return null;
    }

    @Override
    public void onRoomMixTypeChange(RCLiveMixType mixType, int customerType) {
        Logger.e(TAG, "onRoomMixTypeChange: mixType = " + (customerType == -1 ? mixType.getValue() : customerType));
    }

    @Override
    public void onRoomDestroy() {
        QDialog dialog = new QDialog(reference.get(),
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                    }
                });
        dialog.replaceContent("当前直播已结束", "", null, "确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.get().finish();
                RCLiveEngine.getInstance().leaveRoom(null);
            }
        }, null);
        dialog.show();
        Logger.e(TAG, "onRoomDestory:");
    }


    @Override
    public void onSeatLocked(RCLiveSeatInfo seatInfo, boolean locked) {

    }

    @Override
    public void onSeatMute(RCLiveSeatInfo seatInfo, boolean mute) {

    }

    @Override
    public void onSeatAudioEnable(RCLiveSeatInfo seatInfo, boolean enable) {

    }

    @Override
    public void onSeatVideoEnable(RCLiveSeatInfo seatInfo, boolean enable) {

    }

    @Override
    public void onSeatSpeak(RCLiveSeatInfo seatInfo, int audioLevel) {

    }

    @Override
    public void set() {
    }

    @Override
    public void release() {
    }

    @Override
    public void onPKBegin(RCLiveVideoPK rcLiveVideoPK) {
        Log.e(TAG, "onPKBegin: ");
        KToast.show("PK开始");
    }

    @Override
    public void onPKFinish() {
        Log.e(TAG, "onPKFinish: ");
        KToast.show("PK结束");
    }

    @Override
    public void onReceivePKInvitation(String inviterRoomId, String inviterUserId) {
        Log.e(TAG, "onReceivePKInvitation: ");
        //收到PK邀请
        ApiLiveDialogHelper.helper().showResponsePK(reference.get(), new ApiLiveDialogHelper.OnApiClickListener() {
            @Override
            public void onApiClick(View v, LiveFun fun) {
                switch (fun) {
                    case agree_pk:
                        RCLiveEngine.getInstance().acceptPKInvitation(inviterRoomId, inviterUserId, null);
                        break;
                    default:
                        RCLiveEngine.getInstance().rejectPKInvitation(inviterRoomId, inviterUserId, fun.getValue(), null);
                        break;
                }
                ApiLiveDialogHelper.helper().dismissDialog();
            }
        });
    }

    @Override
    public void onPKInvitationCanceled(String inviterRoomId, String inviterUserId) {
        Log.e(TAG, "onPKInvitationCanceled: ");
        EToast.showToast("PK邀请被撤销");
        ApiLiveDialogHelper.helper().dismissDialog();
    }

    @Override
    public void onAcceptPKInvitationFromRoom(String inviteeRoomId, String inviteeUserId) {
        Log.e(TAG,"onAcceptPKInvitationFromRoom");
    }

    @Override
    public void onRejectPKInvitationFromRoom(String inviteeRoomId, String inviteeUserId, String reason) {
        KToast.show("对方拒绝PK，理由是:"+reason);
    }
}
