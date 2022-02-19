package cn.rongcloud.live;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.utils.Logger;

import java.util.HashMap;

import cn.rongcloud.authentication.Api;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.callback.RCLiveCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.liveroom.api.model.RCLiveSeatInfo;
import cn.rongcloud.liveroom.weight.RCLiveView;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.OkParams;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.quickdemo.R;
import io.rong.imlib.RongIMClient;

/**
 * QuickDemo 视频直播 观众端
 * TODO QuickDemo只做SDK API的用法演示，详细的业务流程可参考 RC_RTC开源项目，开源地址为 ：
 * https://github.com/rongcloud/rongcloud-scene-android-demo
 */
public class AudienceActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "AudienceActivity";
    FrameLayout contain;
    private String roomId;
    private TextView tvRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_audience);
        init();
        roomId = getIntent().getStringExtra("roomId");
        joinRoom();
    }

    private void init() {
        contain = findViewById(R.id.container);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.cancelRequest).setOnClickListener(this);
        tvRequest = findViewById(R.id.request);
        tvRequest.setOnClickListener(this);
        RCLiveEngine.getInstance().setSeatViewProvider(new QuickProvider() {
            @Override
            public void onSeatClick(RCLiveSeatInfo seatInfo) {
                if (TextUtils.equals(seatInfo.getUserId(), RongIMClient.getInstance().getCurrentUserId())) {
                    ApiLiveDialogHelper.helper().showLiveSeatApiDialog(AudienceActivity.this, seatInfo, false);
                }
            }
        });
        RCLiveEngine.getInstance().setCustomerLayout(new QuickCustomerLayout());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.stop == id) {
            leaveRoom();
        } else if (R.id.request == id) {
            request();
        } else if (R.id.cancelRequest == id) {
            revokeRequest();
        }
    }

    /**
     * 加入直播间
     */
    private void joinRoom() {
        QuickListener.get().resister(this, false);
        //这里演示订阅CDN房间，可以根据自己的业务需求用决定
        RCLiveEngine.getInstance().joinCDNRoom(roomId, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                Logger.e(TAG, "joinRoom:onSuccess");
                RCLiveView rcLiveView = RCLiveEngine.getInstance().preview();
                rcLiveView.attachParent(contain, null);
                KToast.show("加入直播间成功");
                changeUserRoom(roomId);
            }

            @Override
            public void onError(int code, RCLiveError error) {
                KToast.show("加入直播间失败:" + GsonUtil.obj2Json(error));
            }
        });
    }

    /**
     * 离开直播间
     */
    private void leaveRoom() {
        RCLiveEngine.getInstance().leaveRoom(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                KToast.show("离开直播间成功");
                finish();
                changeUserRoom("");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                KToast.show("离开直播间失败:" + GsonUtil.obj2Json(error));
            }
        });
    }

    /**
     * 申请上麦
     */
    private void request() {
        RCLiveEngine.getInstance().getLinkManager().requestLiveVideo(-1, new ApiLiveDialogHelper.DefalutCallback("申请上麦") {
            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });
    }

    /**
     * 撤回申请上麦
     */
    private void revokeRequest() {
        RCLiveEngine.getInstance().getLinkManager().cancelRequest(new ApiLiveDialogHelper.DefalutCallback("撤销申请"));
    }

    /**
     * 修改在哪个房间里面
     *
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