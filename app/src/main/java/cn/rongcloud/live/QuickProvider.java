package cn.rongcloud.live;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.kit.UIKit;
import com.kit.utils.Logger;

import cn.rongcloud.authentication.AccoutManager;
import cn.rongcloud.liveroom.api.RCHolder;
import cn.rongcloud.liveroom.api.RCParamter;
import cn.rongcloud.liveroom.api.SeatViewProvider;
import cn.rongcloud.liveroom.api.model.RCLiveSeatInfo;
import cn.rongcloud.quickdemo.R;

public class QuickProvider implements SeatViewProvider {
    String TAG = "QuickProvider";

    @Override
    public View provideSeatView(RCLiveSeatInfo seatInfo, RCParamter rcParamter) {
        if (!TextUtils.isEmpty(seatInfo.getUserId())){
            return null;
        }
        View view = inflate(seatInfo, rcParamter);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSeatClick(seatInfo);
            }
        });
        conver(new RCHolder(view), seatInfo, rcParamter);
        return view;
    }

    public void conver(RCHolder holder, RCLiveSeatInfo seat, RCParamter paramter) {
        if (holder.rootView()==null){
            return;
        }
        Logger.e(TAG, "conver:" + seat.getIndex());
        String name = AccoutManager.getAccoutName(seat.getUserId());
        holder.setText(R.id.member_name,
                "index:" + seat.getIndex()
                        + "\n name:" + name
                        + "\n 静音:" + seat.isMute()
                        + "\n 锁定:" + seat.isLock()
                        + "\n 音频:" + seat.isEnableAudio()
                        + "\n 图像:" + seat.isEnableVideo()
        );
        holder.rootView().setBackgroundColor(!TextUtils.isEmpty(seat.getUserId())
                ? Color.parseColor("#00343434")
                : Color.parseColor("#779999"));
    }

    public View inflate(RCLiveSeatInfo seat, RCParamter paramter) {
        Logger.e(TAG, "inflate");
        return UIKit.inflate(R.layout.layout_live_item);
    }

    public void onSeatClick(RCLiveSeatInfo seat) {

    }
}
