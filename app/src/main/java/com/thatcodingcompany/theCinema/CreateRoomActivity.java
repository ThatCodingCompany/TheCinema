package com.thatcodingcompany.theCinema;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoCustomVideoCaptureHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerVideoHandler;
import im.zego.zegoexpress.constants.ZegoLanguage;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.constants.ZegoVideoFrameFormat;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zegoexpress.entity.ZegoVideoFrameParam;

public class CreateRoomActivity extends AppCompatActivity {
    public static ZegoExpressEngine engine = null;
    public static final String TAG = "CreateRoom";

    private String userId;
    private String userName;
    private String roomId;
    private String camaraStreamId;
    private String filmStreamId;
    private boolean readyForPush = false;

    private String mpath;
    private ZegoMediaPlayer mediaplayer = null;
    private long currentResourceTotalDuration;
    long playingProgress = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_room);
        getSupportActionBar().hide();

        ZegoCustomVideoCaptureConfig videoCaptureConfig = new ZegoCustomVideoCaptureConfig();
        //TODO: Data type may have to be modified.
        videoCaptureConfig.bufferType = ZegoVideoBufferType.RAW_DATA;
        ZegoEngineConfig engineConfig = new ZegoEngineConfig();
        engineConfig.customVideoCaptureAuxConfig = videoCaptureConfig;
        ZegoExpressEngine.setEngineConfig(engineConfig);
        ZegoVideoConfig zegoVideoConfig = new ZegoVideoConfig();
        zegoVideoConfig.setCaptureResolution(592, 370);

        engine = ZegoExpressEngine.createEngine(idConfig.appid, idConfig.appsign,
                true, ZegoScenario.GENERAL, getApplication(), null);
        engine.setDebugVerbose(true, ZegoLanguage.CHINESE);

        engine.setVideoConfig(zegoVideoConfig, ZegoPublishChannel.AUX);

        engine.setCustomVideoCaptureHandler(new IZegoCustomVideoCaptureHandler() {
            @Override
            public void onStart(ZegoPublishChannel channel) {
                // 收到回调后，开发者需要执行启动视频采集相关的业务逻辑，例如开启摄像头等
                readyForPush = true;
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
                if (updateType.equals(ZegoUpdateType.ADD)) {
                    for (int i = 0; i < streamList.size(); ++i) {
                        ZegoStream currentStream = streamList.get(i);
                        if (currentStream.streamID.equals(camaraStreamId)
                                || currentStream.streamID.equals(filmStreamId)) {
                            continue;
                        } else {
                            if (currentStream.streamID.substring(0, 6).equals("camara")) {
                                View remoteCamara = findViewById(R.id.TextureViewRemote);
                                engine.startPlayingStream(currentStream.streamID,
                                        new ZegoCanvas(remoteCamara));
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
        roomId = UUID.randomUUID().toString();

        new AlertDialog.Builder(this).setTitle("把房间口令发给朋友吧！").setMessage(roomId)
                .setPositiveButton("复制到剪切板", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ClipboardManager clipboardManager =
                                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("TheCinemaLabel", roomId);
                        clipboardManager.setPrimaryClip(clipData);
                    }
                }).setNegativeButton("取消", null).show();

        //Log.d(TAG, "onCreate: \n" + userId);
        ZegoUser user = new ZegoUser(userId, userName);
        engine.loginRoom(roomId, user, null);
        Log.d(TAG, "onCreate: Login room.");
        camaraStreamId = "camara" + UUID.randomUUID().toString();
        filmStreamId = "film" + UUID.randomUUID().toString();
        engine.setAppOrientation(ZegoOrientation.ORIENTATION_90, ZegoPublishChannel.MAIN);
        engine.startPublishingStream(camaraStreamId, ZegoPublishChannel.MAIN);
        View local_view = findViewById(R.id.TextureViewPreview);
        engine.startPreview(new ZegoCanvas(local_view), ZegoPublishChannel.MAIN);


        //mediaplaer
        ImageButton buttonplay;
        ImageButton loadresource;
        final TextureView textureView;
        //初始化控件
        buttonplay = findViewById(R.id.Button_play);
        loadresource = findViewById(R.id.Button_loadresource);
        textureView = findViewById(R.id.textureView);
        mediaplayer = ZegoMediaPlayer.createMediaPlayer();
        mediaplayer.setProgressInterval(30);

        mediaplayer.setEventHandler(new IZegoMediaPlayerEventHandler() {
            @Override
            public void onMediaPlayerPlayingProgress(ZegoMediaPlayer mediaPlayer,
                                                     long millisecond) {
                playingProgress = millisecond;
            }
        });

        mediaplayer.setVideoHandler(new IZegoMediaPlayerVideoHandler() {
            @Override
            public void onVideoFrame(ZegoMediaPlayer mediaPlayer, ByteBuffer[] data,
                                     int[] dataLength, ZegoVideoFrameParam param) {
                for (int i = 0; i < data.length - 1; ++i) {
                    Log.d(TAG, "IsReadyForPush " + readyForPush);
                    Log.d(TAG, "onFrameLength1: " + data.length);
                    Log.d(TAG, "onFrameLength2: " + dataLength.length);
                    if (readyForPush) {
                        engine.sendCustomVideoCaptureRawData(data[i], dataLength[i], param,
                                playingProgress, ZegoPublishChannel.AUX);//TODO: fix Index 0 HERE
                    }
                }
            }
        }, ZegoVideoFrameFormat.Unknown);

        loadresource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaplayer != null) {
                    openSystemFile();
                }
            }
        });

        buttonplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "BeforeLoading: " + mpath);
                mediaplayer.loadResource(mpath, new IZegoMediaPlayerLoadResourceCallback() {
                    @Override
                    public void onLoadResourceCallback(int i) {
                        if (i != 0) {
                            Log.e(TAG, "onLoadResourceCallback:" + i);
                            Toast.makeText(CreateRoomActivity.this, "加载本地资源异常:" + i,
                                    Toast.LENGTH_LONG).show();
                        }
                        // 只有在加载成功之后 getTotalDuration 才会返回正常的数值
                        currentResourceTotalDuration = mediaplayer.getTotalDuration();
                        Log.d(TAG, "currentResourceTotalDuration: " +
                                currentResourceTotalDuration);
                        Toast.makeText(CreateRoomActivity.this,
                                "currentResourceTotalDuration: " + currentResourceTotalDuration,
                                Toast.LENGTH_LONG).show();
                        //Toast.makeText(CreateRoomActivity.this, "加载:", Toast.LENGTH_LONG).show();
                    }
                });
                mediaplayer.setPlayerCanvas(new ZegoCanvas(textureView));
                mediaplayer.start();
            }
        });
        engine.startPublishingStream(filmStreamId, ZegoPublishChannel.AUX);
    }

    public void openSystemFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 所有类型
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择文件"), 1);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(CreateRoomActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            //Get the Uri of the selected file
            Uri uri = data.getData();
            if (uri != null) {
                String path = Filechoose.getPath(this, uri);
                Log.i("filepath", " = " + path);
                mpath = path;
                Log.i(TAG, "onActivityResult: mpath" + mpath);
            }
        }
    }

    @Override
    protected void onDestroy() {
        engine.stopPublishingStream();
        engine.stopPreview();
        engine.logoutRoom(roomId);
        ZegoExpressEngine.destroyEngine(null);
        mediaplayer.destroyMediaPlayer();
        mediaplayer = null;
        super.onDestroy();
    }
}
