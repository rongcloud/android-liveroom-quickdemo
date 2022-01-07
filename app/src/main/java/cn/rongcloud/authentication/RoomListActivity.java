package cn.rongcloud.authentication;


import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.bcq.refresh.IRefresh;
import com.bcq.refresh.XRecyclerView;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.authentication.bean.VoiceRoom;
import cn.rongcloud.live.ApiLiveDialogHelper;
import cn.rongcloud.live.AudienceActivity;
import cn.rongcloud.live.BroadcastActivity;
import cn.rongcloud.oklib.LoadTag;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.oklib.wrapper.interfaces.ILoadTag;
import cn.rongcloud.quickdemo.R;

/**
 * 房间列表
 */
public class RoomListActivity extends AbsPermissionActivity {
    public final static String TAG = "RoomListActivity";
    private final static String ACTION_CREATE = "创建房间";

    @Override
    protected String[] onCheckPermission() {
        return VOICE_PERMISSIONS;
    }

    private XRecyclerView rl_rooms;
    private RcySAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(ACTION_CREATE).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(ACTION_CREATE)) {
            createLiveRoom();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPermissionAccept(boolean accept) {
        if (accept) {
            initView();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_room_list);
        setTitle("视频直播");
        rl_rooms = findViewById(R.id.rl_rooms);
        rl_rooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RcySAdapter<VoiceRoom, RcyHolder>(this, R.layout.layout_room_item) {
            @Override
            public void convert(RcyHolder holder, VoiceRoom room, int position) {
                holder.setText(R.id.room_name, room.getRoomName());
                holder.setText(R.id.user_id, room.getCreateUser().getUserName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean owner = TextUtils.equals(room.getUserId(), AccoutManager.getCurrentId());
                        jumpToLiveRoom(room.getRoomId(), owner);
                    }
                });
            }
        };
        rl_rooms.setAdapter(adapter);
        // refresh load
        rl_rooms.setLoadListener(new IRefresh.LoadListener() {
            @Override
            public void onRefresh() {
                getRoomListFromService(true);
            }

            @Override
            public void onLoad() {
                getRoomListFromService(false);
            }
        });
        getRoomListFromService(true);
    }

    void createLiveRoom() {
        ApiLiveDialogHelper.helper().showEditorDialog(this, "房间名称", new IResultBack<String>() {
            @Override
            public void onResult(String result) {
                if (TextUtils.isEmpty(result)) {
                    KToast.show("请输入房间名称");
                    return;
                }
                createRoomByService(result);
            }
        });
    }

    private int page = 1;
    private final static int PAGE_SIZE = 10;

    void getRoomListFromService(boolean refresh) {
        if (refresh) {
            page = 1;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", PAGE_SIZE);
        params.put("type", Api.ROOM_TYPE);
        OkApi.get(Api.ROOM_LIST, params, new WrapperCallBack() {
            @Override
            public void onError(int code, String msg) {
                KToast.show("拉取房间列表失败");
            }

            @Override
            public void onResult(Wrapper wrapper) {
                List<VoiceRoom> rooms = wrapper.getList("rooms", VoiceRoom.class);
                Logger.e(TAG, "provideFromService: size = " + (null == rooms ? 0 : rooms.size()));
                if (rooms != null && !rooms.isEmpty()) {
                    page++;
                }
                for (VoiceRoom room : rooms) {
                    if (null != room.getCreateUser()) {
                        AccoutManager.setAcctount(room.getCreateUser(), false);
                    }
                }
                if (null != adapter) adapter.setData(rooms, true);
            }

            @Override
            public void onAfter() {
                if (null != rl_rooms) {
                    rl_rooms.refreshComplete();
                    rl_rooms.loadComplete();
                }
            }
        });
    }

    private void createRoomByService(String roomName) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", roomName);
        params.put("themePictureUrl", "");
        params.put("isPrivate", 0);
        params.put("password", "");
        params.put("kv", new ArrayList());
        params.put("roomType", Api.ROOM_TYPE);// 1：语聊房  2：电台房 3：直播房
        LoadTag tag = new LoadTag(activity, "创建中...");
        tag.show();
        OkApi.post(Api.ROOM_CREATE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (null == result) {
                    KToast.show("创建房间失败");
                    if (null != tag) tag.dismiss();
                    return;
                }
                VoiceRoom voiceRoom = result.get(VoiceRoom.class);
                if (null == voiceRoom) {
                    KToast.show("创建房间失败");
                    if (null != tag) tag.dismiss();
                    return;
                }
                if (result.getCode() == 10000) {
                    if (null != tag) tag.dismiss();
                    createRoomBySDK(tag, voiceRoom.getRoomId(), voiceRoom.getRoomName());
                } else {
                    if (null != tag) tag.dismiss();
                    if (30016 == result.getCode()) {
                        KToast.show("您已经创建过房间，不能重复创建");
                        jumpToLiveRoom(voiceRoom.getRoomId(), true);
                    } else {
                        KToast.show("创建房间失败");
                    }
                }
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                if (null != tag) tag.dismiss();
                KToast.show("创建房间失败");
            }
        });
    }

    public static void destoryRoomByService(Activity activity, String roomId) {
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
                } else {
                    KToast.show("关闭房间失败");
                }
            }
        });
    }

    void createRoomBySDK(ILoadTag tag, String roomId, String roomName) {
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(roomName)) {
            KToast.show("房间ID或房间名称不能为空");
            if (null != tag) tag.dismiss();
            return;
        }
        jumpToLiveRoom(roomId, true);
    }

    /**
     * 跳转到视频直播界面
     *
     * @param roomId 房间Id
     * @param owner  是不是房主
     */
    private void jumpToLiveRoom(String roomId, boolean owner) {
        if (owner){
            //如果是房主的话
            Intent intent=new Intent(this, BroadcastActivity.class);
            intent.putExtra("roomId",roomId);
            startActivity(intent);
        }else {
            //如果不是房主的话
            Intent intent=new Intent(this, AudienceActivity.class);
            intent.putExtra("roomId",roomId);
            startActivity(intent);
        }
    }
}
