package cn.rongcloud.live;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.authentication.AccoutManager;
import cn.rongcloud.authentication.Api;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.RCLiveMixType;
import cn.rongcloud.liveroom.api.callback.RCLiveCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.liveroom.api.model.RCLiveSeatInfo;
import cn.rongcloud.liveroom.weight.RCLiveView;
import cn.rongcloud.liveroom.weight.wrapper.RCLiveVideoWrapperView;
import cn.rongcloud.oklib.LoadTag;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.OkParams;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.pk.RoomOwnerDialog;
import cn.rongcloud.quickdemo.R;


/**
 * QuickDemo 视频直播 主播端
 * TODO QuickDemo只做SDK API的用法演示，详细的业务流程可参考 RC_RTC开源项目，开源地址为 ：
 * https://github.com/rongcloud/rongcloud-scene-android-demo
 */
public class BroadcastActivity extends Activity implements View.OnClickListener {

    String TAG = "BroadcastActivity";
    List<RCLiveMixType> mixTypes = Arrays.asList(RCLiveMixType.RCMixTypeOneToOne,
            RCLiveMixType.RCMixTypeOneToSix,
            RCLiveMixType.RCMixTypeGridTwo,
            RCLiveMixType.RCMixTypeGridThree,
            RCLiveMixType.RCMixTypeGridFour,
            RCLiveMixType.RCMixTypeGridSeven,
            RCLiveMixType.RCMixTypeGridNine);
    int index;
    int customer;
    int[] customerTypes = new int[]{QuickCustomerLayout.CUSTOMER_1V3, QuickCustomerLayout.CUSTOMER_1V4};
    private RCLiveView liveView;
    private String roomId; //当前房间ID
    private RoomOwnerDialog dialog;
    private TextView tvMutePk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        roomId = getIntent().getStringExtra("roomId");
        initView();
        initData();
    }


    /**
     * 初始化布局
     */
    private void initView() {
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.invite).setOnClickListener(this);
        findViewById(R.id.mix).setOnClickListener(this);
        findViewById(R.id.customer).setOnClickListener(this);
        findViewById(R.id.kickoutRoom).setOnClickListener(this);
        findViewById(R.id.beginPk).setOnClickListener(this::onClick);
        findViewById(R.id.quitPk).setOnClickListener(this::onClick);
        findViewById(R.id.cancelPk).setOnClickListener(this::onClick);
        tvMutePk = findViewById(R.id.mutePk);
        tvMutePk.setOnClickListener(this::onClick);
    }

    /**
     * 初始化信息 打开相机等
     */
    private void initData() {
        //设置覆盖布局提供者
        RCLiveEngine.getInstance().setSeatViewProvider(new QuickProvider() {
            @Override
            public void onSeatClick(RCLiveSeatInfo seatInfo) {
                ApiLiveDialogHelper.helper().showLiveSeatApiDialog(BroadcastActivity.this, seatInfo, true);
            }
        });
        //设置直播房监听事件，具体的可以进去QuickListener看具体的监听
        QuickListener.get().resister(this, true);
        //打开相机，渲染到视图上
        preview();
        //设置自定义布局
        RCLiveEngine.getInstance().setCustomerLayout(new QuickCustomerLayout());
    }

    /**
     * 开始预览
     */
    private void preview() {
        FrameLayout contain = findViewById(R.id.container);
        RCLiveEngine.getInstance().prepare(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                liveView = RCLiveEngine.getInstance().preview();
                FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                fl.gravity = Gravity.CENTER;
                if (null != liveView) {
                    liveView.attachParent(contain, fl);
                }
                for (int i = 0; i < liveView.getChildCount(); i++) {
                    View childAt = liveView.getChildAt(i);
                    if (childAt instanceof RCLiveVideoWrapperView) {
                        ((RCLiveVideoWrapperView) childAt).setOnTop();
                    }
                }
                KToast.show("预览成功");
                RCLiveEngine.getInstance().begin(roomId, new RCLiveCallback() {
                    @Override
                    public void onSuccess() {
                        KToast.show("开播成功");
                        changeUserRoom(roomId);
                    }

                    @Override
                    public void onError(int i, RCLiveError rcLiveError) {
                        KToast.show("开播失败" + rcLiveError.getMessage());
                        finishLiveRoom();
                    }
                });
            }

            @Override
            public void onError(int code, RCLiveError error) {
                KToast.show("预览失败：" + GsonUtil.obj2Json(error));
            }
        });
    }

    /**
     * 点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.stop == id) {
            finishLiveRoom();
        } else if (R.id.invite == id) {
            invite();
        } else if (R.id.mix == id) {
            index++;
            int i = index % 7;
            RCLiveEngine.getInstance().setMixType(mixTypes.get(i), null);
        } else if (R.id.customer == id) {
            customer++;
            int i = customer % 2;
            RCLiveEngine.getInstance().setCustomerMixType(customerTypes[i], new RCLiveCallback() {
                @Override
                public void onSuccess() {
                    Logger.e(TAG, "setCustomerMixType onSuccess:");
                }

                @Override
                public void onError(int code, RCLiveError error) {
                    Logger.e(TAG, "setCustomerMixType:" + GsonUtil.obj2Json(error));
                }
            });
        } else if (R.id.kickoutRoom == id) {
            kickOutRoom();
        } else if (R.id.beginPk == id) {
            beginPk();
        } else if (R.id.cancelPk == id) {
            cancelPk();
        } else if (R.id.quitPk == id) {
            quitPk();
        } else if (R.id.mutePk == id) {
            mutePk();
        }
    }

    boolean isMute = true;

    /**
     * 对正在连麦的房间进行声音操作
     */
    private void mutePk() {
        RCLiveEngine.getInstance().mutePKUser(isMute, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                EToast.showToast(isMute ? "静音成功" : "取消静音成功");
                tvMutePk.setText(isMute ? "取消静音PK" : "静音Pk用户");
                isMute = !isMute;
            }

            @Override
            public void onError(int code, RCLiveError error) {
                EToast.showToast(isMute ? "静音失败" : "取消静音失败");
            }
        });
    }

    /**
     * 退出PK
     */
    private void quitPk() {
        RCLiveEngine.getInstance().quitPK(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                EToast.showToast("退出PK成功");
                LivePKHelper.releasePK();
            }

            @Override
            public void onError(int code, RCLiveError error) {
                EToast.showToast("退出PK失败");
            }
        });
    }

    /**
     * 取消PK
     */
    private void cancelPk() {
        LivePKHelper livePKHelper = LivePKHelper.getLivePKHelper();
        if (livePKHelper != null && livePKHelper.inviteeRoomId != null && livePKHelper.inviteeId != null) {
            RCLiveEngine.getInstance().cancelPKInvitation(livePKHelper.inviteeRoomId, livePKHelper.inviteeId, new RCLiveCallback() {
                @Override
                public void onSuccess() {
                    EToast.showToast("撤销PK申请成功");
                    LivePKHelper.releasePK();
                }

                @Override
                public void onError(int code, RCLiveError error) {
                    EToast.showToast("撤销PK申请失败");
                }
            });
        }

    }

    /**
     * 开始PK
     */
    private void beginPk() {
        //获取可PK的人
        dialog = new RoomOwnerDialog(this, new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean aBoolean) {
                dialog.dismiss();
            }
        }).setOnCancelListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        dialog.show();
    }

    /**
     * 踢出房间
     */
    private void kickOutRoom() {
        ApiLiveDialogHelper.helper().showSelectDialog(this, roomId, "选择踢出房间用户", new IResultBack<AccoutManager.Accout>() {
            @Override
            public void onResult(AccoutManager.Accout result) {
                RCLiveEngine.getInstance().kickOutRoom(result.getUserId(), null);
                ApiLiveDialogHelper.helper().dismissDialog();
            }
        }, false, SeatListFun.kick_out_room);
    }

    /**
     * 邀请上麦
     */
    void invite() {
        ApiLiveDialogHelper.helper().showSelectDialog(this, roomId, "选择直播上麦", new IResultBack<AccoutManager.Accout>() {
            @Override
            public void onResult(AccoutManager.Accout result) {
                RCLiveEngine.getInstance().getLinkManager().inviteLiveVideo(result.getUserId(), -1, new ApiLiveDialogHelper.DefalutCallback("邀请上麦"));
                ApiLiveDialogHelper.helper().dismissDialog();
            }
        }, false, SeatListFun.invite_enter_seat);
    }

    /**
     * 销毁直播间
     * TODO 因为SDK不具备真正意义的关闭房间，finish方法为过时方法，可以不调用，而直接调用API接口方法关闭房间
     */
    public void finishLiveRoom() {
        String url = Api.DELETE_ROOM.replace(Api.KEY_ROOM_ID, roomId);
        LoadTag tag = new LoadTag(this, "关闭房间...");
        tag.show();
        OkApi.get(url, null, new WrapperCallBack() {
            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                if (null != tag) tag.dismiss();
                KToast.show("关闭房间失败");
            }

            @Override
            public void onResult(Wrapper wrapper) {
                if (null != tag) tag.dismiss();
                if (wrapper.ok()) {
                    KToast.show("关闭房间成功");
                    BroadcastActivity.this.finish();
                    //TODO 释放掉资源 一定记得在结束直播释放掉所有资源
                    RCLiveEngine.getInstance().unPrepare(null);
                    changeUserRoom("");
                } else {
                    KToast.show("关闭房间失败");
                }
            }
        });
    }

    /**
     * 通过APi接口设置当前用户所在的房间，方便执行邀请上麦，PK等操作
     * @param roomId
     */
    public void changeUserRoom(String roomId) {
        HashMap<String, Object> params = new OkParams()
                .add("roomId", roomId)
                .build();
        OkApi.get(Api.USER_ROOM_CHANGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    Log.e(TAG, "onResult: " + result.getMessage());
                }
            }
        });
    }
}