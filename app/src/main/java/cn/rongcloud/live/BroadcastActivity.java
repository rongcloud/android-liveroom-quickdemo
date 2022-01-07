package cn.rongcloud.live;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

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
import cn.rongcloud.oklib.LoadTag;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.OkParams;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.quickdemo.R;;

public class BroadcastActivity extends Activity implements View.OnClickListener {
    String TAG = "BroadcastActivity";
    FrameLayout contain;
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
    //便于测试 统一房间Id
    private RecyclerView rl_accout;
    private RCLiveView liveView;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        roomId = getIntent().getStringExtra("roomId");
        init();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.preview == id) {
            preview();
        } else if (R.id.start == id) {
            start();
        } else if (R.id.stop == id) {
            stop();
        } else if (R.id.invetate == id) {
            invetate();
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
        }else if (R.id.kickoutRoom == id){
            kickoutRoom();
        }
    }

    private void kickoutRoom() {
        ApiLiveDialogHelper.helper().showSelectDialog(this, roomId,"选择踢出房间用户", new IResultBack<AccoutManager.Accout>() {
            @Override
            public void onResult(AccoutManager.Accout result) {
                RCLiveEngine.getInstance().kickOutRoom(result.getUserId(),null);
                ApiLiveDialogHelper.helper().dismissDialog();
            }
        }, false,SeatListFun.kick_out_room);
    }

    void init() {
        contain = findViewById(R.id.container);
        findViewById(R.id.preview).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.invetate).setOnClickListener(this);
        findViewById(R.id.mix).setOnClickListener(this);
        findViewById(R.id.customer).setOnClickListener(this);
        findViewById(R.id.kickoutRoom).setOnClickListener(this);
        RCLiveEngine.getInstance().setCustomerLayout(new QuickCustomerLayout());
        RCLiveEngine.getInstance().setSeatViewProvider(new QuickProvider() {
            @Override
            public void onSeatClick(RCLiveSeatInfo seatInfo) {
                ApiLiveDialogHelper.helper().showLiveSeatApiDialog(BroadcastActivity.this, seatInfo, true);
            }
        });
        QuickListener.get().resister(this, true);
        preview();
    }

    void preview() {
        RCLiveEngine.getInstance().prepare(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                liveView = RCLiveEngine.getInstance().preview();
                FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                fl.gravity = Gravity.CENTER;
                if (null != liveView) {
                    liveView.attachParent(contain, fl);
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
                        KToast.show("开播失败"+rcLiveError.getMessage());
                        destoryRoomByService(BroadcastActivity.this,roomId);
                    }
                });
            }

            @Override
            public void onError(int code, RCLiveError error) {
                KToast.show("预览失败：" + GsonUtil.obj2Json(error));
            }
        });
    }

    void start() {
        ApiLiveDialogHelper.helper().showEditorDialog(this, "直播间ID", "开播", new IResultBack<String>() {
            @Override
            public void onResult(String result) {
                if (TextUtils.isEmpty(result)) {
                    KToast.show("房间ID不能为空");
                    return;
                }
                RCLiveEngine.getInstance().begin(result, new ApiLiveDialogHelper.DefalutCallback("开播"));
            }
        });
    }

    void stop() {
        RCLiveEngine.getInstance().finish(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                KToast.show("关播直播间成功");
                destoryRoomByService(BroadcastActivity.this,roomId);
            }

            @Override
            public void onError(int code, RCLiveError error) {
                KToast.show("关播直播间失败:" + GsonUtil.obj2Json(error));
            }
        });
    }

    void invetate() {
        ApiLiveDialogHelper.helper().showSelectDialog(this, roomId,"选择直播上麦", new IResultBack<AccoutManager.Accout>() {
            @Override
            public void onResult(AccoutManager.Accout result) {
                RCLiveEngine.getInstance().getLinkManager().inviteLiveVideo(result.getUserId(), -1, new ApiLiveDialogHelper.DefalutCallback("邀请上麦"));
                ApiLiveDialogHelper.helper().dismissDialog();
            }
        }, false,SeatListFun.invite_enter_seat);
    }


    public void destoryRoomByService(Activity activity, String roomId) {
        String url = Api.DELETE_ROOM.replace(Api.KEY_ROOM_ID, roomId);
        LoadTag tag = new LoadTag(activity, "关闭房间...");
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
                    changeUserRoom("");
                } else {
                    KToast.show("关闭房间失败");
                }
            }
        });
    }

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