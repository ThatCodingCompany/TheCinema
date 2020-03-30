package com.thatcodingcompany.theCinema;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoLanguage;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class JoinRoomActivity extends AppCompatActivity {
    public static ZegoExpressEngine engine = null;
    public static final String TAG = "JoinRoom";
    private String userId;
    private String userName;
    private String roomId = "";
    private String camaraStreamId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_join_room);
        getSupportActionBar().hide();

        //设置背景色
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.background);
        this.getWindow().setBackgroundDrawable(drawable);

        engine = ZegoExpressEngine.createEngine(idConfig.appid, idConfig.appsign,
                true, ZegoScenario.GENERAL, getApplication(), null);
        engine.setDebugVerbose(true, ZegoLanguage.CHINESE);
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
                if (updateType.equals(ZegoUpdateType.ADD)) {
                    for (int i = 0; i < streamList.size(); ++i) {
                        ZegoStream currentStream = streamList.get(i);
                        if (currentStream.streamID.equals(camaraStreamId)) {
                            continue;
                        } else {
                            if (currentStream.streamID.substring(0, 6).equals("camara")) {
                                View remoteCamara = findViewById(R.id.TextureViewRemote);
                                engine.startPlayingStream(currentStream.streamID,
                                        new ZegoCanvas(remoteCamara));
                            } else if (currentStream.streamID.substring(0, 4).equals("film")) {
                                View remoteFilm = findViewById(R.id.textureView);
                                engine.startPlayingStream(currentStream.streamID,
                                        new ZegoCanvas(remoteFilm));
                            }
                        }
                    }
                }
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
        final ZegoUser user = new ZegoUser(userId, userName);
        final View local_view = findViewById(R.id.TextureViewPreview);
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this).setTitle("请输入房间口令")
                .setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        roomId = editText.getText().toString();
                        Log.i(TAG, "onClick: " + roomId);
                        engine.loginRoom(roomId, user, null);
                        Log.d(TAG, "onCreate: Login room.");
                        camaraStreamId = "camara" + UUID.randomUUID().toString();
                        engine.setAppOrientation(ZegoOrientation.ORIENTATION_90,
                                ZegoPublishChannel.MAIN);
                        engine.startPublishingStream(camaraStreamId, ZegoPublishChannel.MAIN);
                        engine.startPreview(new ZegoCanvas(local_view), ZegoPublishChannel.MAIN);
                    }
                }).setNegativeButton("取消", null).show();

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
