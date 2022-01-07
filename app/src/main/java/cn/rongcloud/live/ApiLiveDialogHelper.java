package cn.rongcloud.live;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.rongcloud.authentication.AccoutManager;
import cn.rongcloud.authentication.Api;
import cn.rongcloud.authentication.bean.User;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.callback.RCLiveCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.liveroom.api.model.RCLiveSeatInfo;
import cn.rongcloud.liveroom.manager.SeatManager;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.quickdemo.R;
import io.rong.imlib.RongIMClient;


public class ApiLiveDialogHelper {
//    public final static LiveFun[] Live_API = new LiveFun[]{
//            LiveFun.seat_enter,
//            LiveFun.seat_left,
//            LiveFun.live_seat_lock,
//            LiveFun.live_seat_unlock,
//            LiveFun.live_seat_mute,
//            LiveFun.live_seat_unmute,
//            LiveFun.live_seat_audio_enable,
//            LiveFun.live_seat_audio_unenable,
//            LiveFun.live_seat_video_enable,
//            LiveFun.live_seat_video_unenable,
//            LiveFun.live_seat_video_unenable,
//            LiveFun.kict_out_seat,
//    };
    //主播点击他人麦位，可操作的API
    public final static LiveFun[] Broadcast_Click_OtherSeat_API = new LiveFun[]{
            LiveFun.live_seat_lock,
            LiveFun.live_seat_unlock,
            LiveFun.live_seat_mute,
            LiveFun.live_seat_unmute,
            LiveFun.kict_out_seat,
            LiveFun.live_kick_out_room,
            LiveFun.live_switch_seat
    };
    //主播点击自己的麦位，可操作的API
    public final static LiveFun[] Broadcast_Click_OwnerSeat_API = new LiveFun[]{
            LiveFun.live_seat_mute,
            LiveFun.live_seat_unmute,
            LiveFun.live_seat_audio_enable,
            LiveFun.live_seat_audio_unenable,
            LiveFun.live_seat_video_enable,
            LiveFun.live_seat_video_unenable,
    };
    //观众 点击自己的麦位，可操作的API
    public final static LiveFun[] Audience_Click_OwnerSeat_API = new LiveFun[]{
            LiveFun.seat_enter,
            LiveFun.seat_left,
            LiveFun.live_seat_mute,
            LiveFun.live_seat_unmute,
            LiveFun.live_seat_audio_enable,
            LiveFun.live_seat_audio_unenable,
            LiveFun.live_seat_video_enable,
            LiveFun.live_seat_video_unenable,
    };

    private final static String TAG = "ApiLiveDialogHelper";
    private final static ApiLiveDialogHelper seatApi = new ApiLiveDialogHelper();
    private QDialog dialog;

    public static ApiLiveDialogHelper helper() {
        return seatApi;
    }

    public void showLiveSeatApiDialog(Activity activity, RCLiveSeatInfo seatInfo,boolean isBroadcast) {
        if (null == seatInfo) {
            return;
        }
        //点击的麦位，根据当前用户是否为主播 ，点击的是否是自己的麦位
        LiveFun[] Live_API;
        if (isBroadcast){
            if (TextUtils.equals(seatInfo.getUserId(), RongIMClient.getInstance().getCurrentUserId())) {
                Live_API=Broadcast_Click_OwnerSeat_API;
            }else {
                Live_API=Broadcast_Click_OtherSeat_API;
            }
        }else {
            Live_API=Audience_Click_OwnerSeat_API;
        }
        showApiDialog(activity, "直播间麦位Api功能演示", Live_API, new OnApiClickListener() {
            @Override
            public void onApiClick(View v, LiveFun fun) {
                handleApi(fun, seatInfo);
                dismissDialog();
            }
        });
    }


    /**
     * 显示api演示弹框
     *
     * @param activity
     * @param title    标题
     * @param apis     api名称
     * @param listener 监听
     */
    private void showApiDialog(Activity activity, String title, LiveFun[] apis, OnApiClickListener listener) {
        if (null == dialog || !dialog.enable()) {
            dialog = new QDialog(activity,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ApiLiveDialogHelper.this.dialog = null;
                        }
                    });
        }
        dialog.replaceContent(title,
                "取消",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                },
                "",
                null,
                initApiFunView(apis, listener));
        dialog.show();
    }

    private View initApiFunView(LiveFun[] apiNames, OnApiClickListener listener) {
        RecyclerView refresh = new RecyclerView(dialog.getContext());
        refresh.setLayoutManager(new GridLayoutManager(dialog.getContext(), 2));
        IAdapte adapter = new ApiAdapter(dialog.getContext(), listener);
        adapter.setRefreshView(refresh);
        adapter.setData(Arrays.asList(apiNames), true);
        return refresh;
    }


    void handleApi(LiveFun fun, RCLiveSeatInfo seatInfo) {
        int seatIndex = seatInfo.getIndex();
        if (LiveFun.live_seat_mute == fun) {
            RCLiveEngine.getInstance().getSeatManager().mute(seatIndex, true, new DefalutCallback("静麦"));
        } else if (LiveFun.live_seat_unmute == fun) {
            RCLiveEngine.getInstance().getSeatManager().mute(seatIndex, false, new DefalutCallback("取消静麦"));
        } else if (LiveFun.live_seat_lock == fun) {
            RCLiveEngine.getInstance().getSeatManager().lock(seatIndex, true, new DefalutCallback("锁麦"));
        } else if (LiveFun.live_seat_unlock == fun) {
            RCLiveEngine.getInstance().getSeatManager().lock(seatIndex, false, new DefalutCallback("解锁"));
        } else if (LiveFun.live_seat_audio_enable == fun) {
            RCLiveEngine.getInstance().getSeatManager().enableAudio(seatIndex, true, new DefalutCallback("启用音频"));
        } else if (LiveFun.live_seat_audio_unenable == fun) {
            RCLiveEngine.getInstance().getSeatManager().enableAudio(seatIndex, false, new DefalutCallback("禁用音频"));
        } else if (LiveFun.live_seat_video_enable == fun) {
            RCLiveEngine.getInstance().getSeatManager().enableVideo(seatIndex, true, new DefalutCallback("启用视频"));
        } else if (LiveFun.live_seat_video_unenable == fun) {
            RCLiveEngine.getInstance().getSeatManager().enableVideo(seatIndex, false, new DefalutCallback("禁用视频"));
        } else if (LiveFun.seat_enter == fun) {
            RCLiveEngine.getInstance().enterSeat(seatIndex, new DefalutCallback("上麦"));
        } else if (LiveFun.seat_left == fun) {
            RCLiveEngine.getInstance().leaveSeat(new DefalutCallback("下麦"));
        } else if (LiveFun.kict_out_seat == fun) {
            if (TextUtils.isEmpty(seatInfo.getUserId())) {
                KToast.show("该麦位没有主播");
                return;
            }
            RCLiveEngine.getInstance().kickOutSeat(seatInfo.getUserId(), new DefalutCallback("抱下麦"));
        } else if (LiveFun.live_kick_out_room ==fun){
            if (TextUtils.isEmpty(seatInfo.getUserId())) {
                KToast.show("该麦位没有主播");
                return;
            }
            RCLiveEngine.getInstance().kickOutRoom(seatInfo.getUserId(), new DefalutCallback("踢出房间"));
        } else if (LiveFun.live_switch_seat ==fun){
            if (!TextUtils.isEmpty(seatInfo.getUserId())) {
                KToast.show("该麦位已经有主播");
                return;
            }
            RCLiveEngine.getInstance().switchTo(seatInfo.getIndex(), new DefalutCallback("切换麦位"));
        }
    }

    public interface OnApiClickListener {
        void onApiClick(View v, LiveFun fun);
    }

    /**
     * api功能适配器
     */
    private static class ApiAdapter extends RcySAdapter<LiveFun, RcyHolder> {
        private OnApiClickListener listener;

        private ApiAdapter(Context context, OnApiClickListener listener) {
            super(context, R.layout.layout_item_api);
            this.listener = listener;
        }

        @Override
        public void convert(RcyHolder holder, LiveFun s, int position) {
            holder.setText(R.id.api_fun, s.getValue());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != listener) listener.onApiClick(view, s);
                }
            });
        }
    }

    public static class DefalutCallback implements RCLiveCallback {
        String action;

        public DefalutCallback(String action) {
            this.action = action;
        }

        @Override
        public void onSuccess() {
            KToast.show(action + "成功");
        }

        @Override
        public void onError(int code, RCLiveError error) {
            KToast.show(action + "失败 :" + GsonUtil.obj2Json(error));
        }
    }

    /**
     * 显示编辑框
     *
     * @param activity
     * @param title    标题
     */
    public void showEditorDialog(Activity activity, String title, IResultBack<String> resultBack) {
        showEditorDialog(activity, title, "", resultBack);
    }

    public void dismissDialog() {
        if (null != dialog) {
            dialog.dismiss();
        }
        dialog = null;
    }

    public void showEditorDialog(Activity activity, String title, String cofirm, IResultBack<String> resultBack) {
        if (null == dialog || !dialog.enable()) {
            dialog = new QDialog(activity,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ApiLiveDialogHelper.this.dialog = null;
                        }
                    });
        }
        EditText editText = new EditText(dialog.getContext());
        editText.setHint(title);
        dialog.replaceContent(title,
                "",
                null,
                TextUtils.isEmpty(cofirm) ? "新建/加入" : cofirm,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                        String roomId = editText.getText().toString().trim();
                        if (null != resultBack) resultBack.onResult(roomId);
                    }
                },
                editText);
        dialog.show();
    }

    public void showTipDialog(Activity activity, String title, String message, IResultBack<Boolean> resultBack) {
        if (null == dialog || !dialog.enable()) {
            dialog = new QDialog(activity,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ApiLiveDialogHelper.this.dialog = null;
                        }
                    });
        }
        TextView textView = new TextView(dialog.getContext());
        textView.setText(message);
        textView.setTextSize(18);
        textView.setTextColor(Color.parseColor("#343434"));
        dialog.replaceContent(title,
                "拒绝",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                        if (null != resultBack) resultBack.onResult(false);
                    }
                },
                "同意",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                        if (null != resultBack) resultBack.onResult(true);
                    }
                },
                textView);
        dialog.show();
    }

    /**
     * 显示选择邀请观众的弹框
     *
     * @param activity
     * @param title
     * @param resultBack
     */
    public void showSelectDialog(Activity activity,
                                 String roomId,String title, IResultBack<AccoutManager.Accout> resultBack, boolean all,SeatListFun seatListFun) {
        if (null == dialog || !dialog.enable()) {
            dialog = new QDialog(activity,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ApiLiveDialogHelper.this.dialog = null;
                        }
                    });
        }
        RecyclerView refresh = new RecyclerView(dialog.getContext());
        IAdapte adapter = new RcySAdapter<AccoutManager.Accout, RcyHolder>(dialog.getContext(), R.layout.layout_item_selector) {
            @Override
            public void convert(RcyHolder holder, AccoutManager.Accout accout, int position) {
                holder.setText(R.id.selector_name, accout.getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (null != resultBack) resultBack.onResult(accout);
                    }
                });
            }
        };
        refresh.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
        adapter.setRefreshView(refresh);
        if (all) {
            adapter.setData(AccoutManager.getAccounts(), true);
        } else {
            switch (seatListFun){
                case kick_out_room:
                    List<AccoutManager.Accout> inRooms = new ArrayList<>();
                    OkApi.get(getMembers(roomId), null, new WrapperCallBack() {
                        @Override
                        public void onResult(Wrapper result) {
                            if (result.ok()) {
                                List<User> list = result.getList(User.class);
                                for (User user : list) {
                                    String userId = user.getUserId();
                                    if (!userId.equals(AccoutManager.getCurrentId())){
                                        AccoutManager.Accout accout = new AccoutManager.Accout(userId, user.getUserName());
                                        inRooms.add(accout);
                                    }
                                }
                                adapter.setData(inRooms, true);
                            }
                        }
                    });
                    break;
                case invite_enter_seat:
                    List<AccoutManager.Accout> onlines = new ArrayList<>();
                    OkApi.get(getMembers(roomId), null, new WrapperCallBack() {
                        @Override
                        public void onResult(Wrapper result) {
                            if (result.ok()) {
                                List<User> list = result.getList(User.class);
                                for (User user : list) {
                                    String userId = user.getUserId();
                                    if (!userId.equals(AccoutManager.getCurrentId())&&!SeatManager.get().getInSeatUserIds().contains(userId)){
                                        AccoutManager.Accout accout = new AccoutManager.Accout(userId, user.getUserName());
                                        onlines.add(accout);
                                    }
                                }
                                adapter.setData(onlines, true);
                            }
                        }
                    });
                    break;
            }

        }
        dialog.replaceContent(title,
                "取消",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                },
                "",
                null,
                refresh);
        dialog.show();
    }

    public static String getMembers(String roomId) {
        return Api.HOST + "mic/room/" + roomId + "/members";
    }
}
