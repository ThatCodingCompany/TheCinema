package com.thatcodingcompany.theCinema;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoCustomVideoCaptureHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoLanguage;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class CreateRoomActivity extends AppCompatActivity {
    public static ZegoExpressEngine engine = null;
    public static final String TAG = "CreateRoom";
    private ArrayList<String> streamForPulling;
    private String userId;
    private String userName;
    private String roomId;
    private String camaraStreamId;
    private String filmStreamId;
    private ArrayList<String> remoteStreamIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_room);

        ZegoCustomVideoCaptureConfig videoCaptureConfig = new ZegoCustomVideoCaptureConfig();
        //TODO: Data type may have to be modified.
        videoCaptureConfig.bufferType = ZegoVideoBufferType.RAW_DATA;
        ZegoEngineConfig engineConfig = new ZegoEngineConfig();
        engineConfig.customVideoCaptureAuxConfig = videoCaptureConfig;
        ZegoExpressEngine.setEngineConfig(engineConfig);

        engine = ZegoExpressEngine.createEngine(idConfig.appid, idConfig.appsign,
                true, ZegoScenario.GENERAL, getApplication(), null);
        engine.setDebugVerbose(true, ZegoLanguage.CHINESE);

        engine.setCustomVideoCaptureHandler(new IZegoCustomVideoCaptureHandler() {
            @Override
            public void onStart(ZegoPublishChannel channel) {
                // 收到回调后，开发者需要执行启动视频采集相关的业务逻辑，例如开启摄像头等

            }

            @Override
            public void onStop(ZegoPublishChannel channel) {
                // 收到回调后，开发者需要执行停止视频采集相关的业务逻辑，例如关闭摄像头等

            }
        });

        engine.setEventHandler(new IZegoEventHandler() {
            /** 常用回调 */
            /** The following are callbacks frequently used */
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode,
                                          JSONObject extendedData) {
                /** 房间状态回调，在登录房间后，当房间状态发生变化（例如房间断开，认证失败等），SDK会通过该接口通知 */
                /** Room status update callback: after logging into the room, when the room
                 * connection status changes
                 * (such as room disconnection, login authentication failure, etc.), the SDK
                 * will notify through the callback
                 */
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType,
                                         ArrayList<ZegoUser> userList) {
                /** 房间状态更新，在登录房间后，当用户进入或退出房间，SDK会通过该接口通知 */
                /** User status is updated. After logging into the room, when a user is added
                 *  or deleted in the room,
                 * the SDK will notify through this callback
                 */
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType,
                                           ArrayList<ZegoStream> streamList) {
                /** 流状态更新，在登录房间后，当房间内有新增或删除音视频流，SDK会通过该接口通知 */
                /** The stream status is updated. After logging into the room, when there is
                 * a new publish or delete of audio and video stream,
                 * the SDK will notify through this callback */

            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state,
                                               int errorCode, JSONObject extendedData) {
                /** 在调用推流接口成功后，推流状态变更（例如由于网络中断引起的流状态异常），SDK会通过该接口通知 */
                /** After calling the stream publishing interface successfully, when the
                 * status of the stream changes,
                 * such as the exception of streaming caused by network interruption, the SDK
                 * will notify through this callback
                 */
            }


            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state,
                                            int errorCode, JSONObject extendedData) {
                /** 在调用拉流接口成功后，拉流状态变更（例如由于网络中断引起的流状态异常），SDK会通过该接口通知 */
                /** After calling the streaming interface successfully, when the status of
                 * the stream changes,
                 * such as network interruption leading to abnormal situation, the SDK will
                 * notify through
                 * this callback */
            }
        });
        userId = UUID.randomUUID().toString();
        userName = userId;
        roomId = UUID.randomUUID().toString();
        //Log.d(TAG, "onCreate: \n" + userId);
        ZegoUser user = new ZegoUser(userId, userName);
        engine.loginRoom(roomId, user, null);
        Log.d(TAG, "onCreate: Login room.");
        camaraStreamId = "camara" + UUID.randomUUID().toString();
        filmStreamId = "film" + UUID.randomUUID().toString();
        engine.setAppOrientation(ZegoOrientation.ORIENTATION_90);
        engine.startPublishingStream(camaraStreamId, ZegoPublishChannel.MAIN);
        View local_view = findViewById(R.id.TextureViewPreview);
        engine.startPreview(new ZegoCanvas(local_view), ZegoPublishChannel.MAIN);
    }

    @Override
    protected void onDestroy() {
        engine.stopPublishingStream();
        engine.stopPreview();
        engine.logoutRoom(roomId);
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}
