## 前置条件

1. 为了方便您快速运行quickdemo，我们为您预置了融云 appkey 和 对应的测试服务器url，您不需要自己部署测试服务器即可运行。
2. 申请  `BusinessToken`
    1. BusinessToken 主要是防止滥用 quickdemo 里的测试appKey，我们为接口做了限制，一个 BusinessToken 最多可以支持10个用户注册，20天使用时长。点击此处 [获取BusinessToken](https://rcrtc-api.rongcloud.net/code)
    2. 过期后您的注册用户会自动移除，想继续使用 quickdemo 需要您重新申请 BusinessToken
    3. 成功获取到 BusinessToken 后，替换 LVSDefine.h 中定义的 BusinessToken

## 运行demo

#### 登录

1. 安装APP启动登录页面 
2. 填写一个符合11位进制的手机号，然后点击登录

<img src="./assets/README-1641550807942.jpg" alt="图片替换文本" width="300"/>

#### 首页

1. 点击列表进入直播间以观众身份观看直播
2. 点击 创建房间 开始直播

<img src="./assets/README-1641550863555.jpg" alt="图片替换文本" width="300"/>

#### 观众端

1. 申请上麦
2. 取消上麦申请
3. 离开直播间 
4. 结束连麦
5. 静音  取消静音
6. 关闭视频 取消视频

<img src="./assets/README-1641551010676.jpg" alt="图片替换文本" width="300"/>

#### 主播端

1. 关闭直播间
2. 踢出房间
3. 邀请上麦
4. 设置布局 
5. 设置自定义布局 
6. 点击自己麦位 
   1. 静音
   2. 取消静音
   3. 开启音频
   4. 关闭音频
   5. 开启视频
   6. 关闭视频

<img src="./assets/README-1641551329324.jpg" alt="图片替换文本" width="300"/>

7. 点击其他麦位
   1. 锁定麦位 
   2. 解锁麦位
   3. 静音
   4. 取消静音 
   5. 抱下麦
   6. 踢出房间
   7. 切换麦位

<img src="./assets/README-1641551688948.jpg" alt="图片替换文本" width="300"/>




具体的使用可参照示例代码。

示例代码展示了更详细的api调用
