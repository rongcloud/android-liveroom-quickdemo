package cn.rongcloud.pk;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.bcq.refresh.XRecyclerView;
import com.kit.UIKit;
import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;

import java.util.List;

import cn.rongcloud.authentication.Api;
import cn.rongcloud.authentication.bean.VoiceRoomBean;
import cn.rongcloud.live.EToast;
import cn.rongcloud.live.LivePKHelper;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.callback.RCLiveCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.oklib.BottomDialog;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.quickdemo.R;


/**
 * pk在线房主弹框
 */
public class RoomOwnerDialog extends BottomDialog {
    public RoomOwnerDialog(Activity activity, IResultBack<Boolean> resultBack) {
        super(activity);
        this.resultBack = resultBack;
        setContentView(R.layout.layout_owner_dialog, 60);
        initView();
        requestOwners();
    }

    private XRecyclerView rcyOwner;
    private IAdapte adapter;
    private IResultBack resultBack;

    private void initView() {
        rcyOwner = UIKit.getView(getContentView(), R.id.rcy_owner);
        rcyOwner.setLayoutManager(new LinearLayoutManager(mActivity));

        adapter = new RcySAdapter<VoiceRoomBean, RcyHolder>(mActivity, R.layout.layout_owner_item) {

            @Override
            public void convert(RcyHolder holder, VoiceRoomBean item, int position) {
                holder.setText(R.id.tv_name, item.getCreateUser().getUserName());
                holder.rootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RCLiveEngine.getInstance().sendPKInvitation(item.getRoomId(), item.getCreateUserId(), new RCLiveCallback() {
                            @Override
                            public void onSuccess() {
                                LivePKHelper.getLivePKHelper().setInviteeId(item.getCreateUserId());
                                LivePKHelper.getLivePKHelper().setInviteeRoomId(item.getRoomId());
                                KToast.show("已邀请PK,等待对方接受");
                                if (null != resultBack)
                                    resultBack.onResult(true);
                            }

                            @Override
                            public void onError(int code, RCLiveError error) {
                                EToast.showToast("邀请PK失败");
                                resultBack.onResult(false);
                            }
                        });
                    }
                });
            }
        };
        adapter.setRefreshView(rcyOwner);
        rcyOwner.enableRefresh(false);
    }

    /**
     * 判断是否正在pk
     *
     * @param roomId     房间id
     * @param resultBack 回调
     */
    void isInPk(String roomId, IResultBack<Boolean> resultBack) {
//        PKApi.getPKInfo(roomId, new IResultBack<PKResult>() {
//            @Override
//            public void onResult(PKResult pkResult) {
//                if (null == pkResult || pkResult.getStatusMsg() == -1 || pkResult.getStatusMsg() == 2) {
//                    resultBack.onResult(false);
//                } else {
//                    resultBack.onResult(true);
//                }
//            }
//        });
        resultBack.onResult(false);
    }

    private void requestOwners() {

        OkApi.get(Api.ONLINE_CREATE, null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                List<VoiceRoomBean> rooms = result.getList(VoiceRoomBean.class);
                adapter.setData(rooms, true);
            }

            @Override
            public void onAfter() {
                if (null != rcyOwner) {
                    rcyOwner.loadComplete();
                    rcyOwner.refreshComplete();
                }
            }

        });
    }

}
