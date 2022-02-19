package cn.rongcloud.authentication;

public interface Api {
    String KEY_ROOM_ID = "roomId";
    int VOICE_ROOM_TYPE = 1;
    int RADIO_ROOM_TYPE = 2;
    int LIVE_ROOM_TYPE = 3;

    int ROOM_TYPE = LIVE_ROOM_TYPE;
    // 融云测试服务器
    String HOST = "https://rcrtc-api.rongcloud.net/";
    String LOGIN = HOST + "user/login";
    String ROOM_CREATE = HOST + "mic/room/create";
    String ROOM_LIST = HOST + "mic/room/list";
    String FILE_URL = HOST + "file/show?path=";
    String DEFAULT_PORTRAIT = "https://cdn.ronghub.com/demo/default/rce_default_avatar.png";

    String DELETE_ROOM = HOST + "mic/room/" + KEY_ROOM_ID + "/delete";

    String USER_ROOM_CHANGE = HOST + "user/change";

    /**
     * pk状态上报
     */
    String PK_STATE = HOST + "mic/room/pk";

   String PK_INFO = HOST + "mic/room/pk/detail/";

    // pk/{roomId}/isPk
   String isPkState = HOST + "mic/room/pk/" + KEY_ROOM_ID + "/isPk";
    String ONLINE_CREATE =HOST + "mic/room/online/created/list";
}
