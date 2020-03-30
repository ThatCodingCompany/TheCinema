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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
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
import im.zego.zegoexpress.callback.IZegoMediaPlayerSeekToCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerVideoHandler;
import im.zego.zegoexpress.constants.ZegoLanguage;
import im.zego.zegoexpress.constants.ZegoMediaPlayerState;
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
    private long currentResourceTotalDuration = 100;
    private SeekBar setvolume;
    private Button buttonplay;
    private Button buttonchosefile;
    private SeekBar setprogress;
    private boolean isPlayButtonDisabled = false;

    long playingProgress = 0L;
    int mediastate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_room);
        getSupportActionBar().hide();

        //设置背景色
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.background);
        this.getWindow().setBackgroundDrawable(drawable);

        ZegoCustomVideoCaptureConfig videoCaptureConfig = new ZegoCustomVideoCaptureConfig();
        //TODO: Data type may have to be modified.
        videoCaptureConfig.bufferType = ZegoVideoBufferType.RAW_DATA;
        ZegoEngineConfig engineConfig = new ZegoEngineConfig();
        engineConfig.customVideoCaptureAuxConfig = videoCaptureConfig;
        ZegoExpressEngine.setEngineConfig(engineConfig);
        ZegoVideoConfig zegoVideoConfig = new ZegoVideoConfig();
        zegoVideoConfig.setEncodeResolution(1920, 1080);

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
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType,
                                         ArrayList<ZegoUser> userList) {
                /** 房间状态更新，在登录房间后，当用户进入或退出房间，SDK会通过该接口通知 */
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
            }


            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state,
                                            int errorCode, JSONObject extendedData) {
                /** 在调用拉流接口成功后，拉流状态变更（例如由于网络中断引起的流状态异常），SDK会通过该接口通知 */
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


        ZegoUser user = new ZegoUser(userId, userName);
        engine.loginRoom(roomId, user, null);

        camaraStreamId = "camara" + UUID.randomUUID().toString();
        filmStreamId = "film" + UUID.randomUUID().toString();
        engine.setAppOrientation(ZegoOrientation.ORIENTATION_90, ZegoPublishChannel.MAIN);
        engine.startPublishingStream(camaraStreamId, ZegoPublishChannel.MAIN);
        View local_view = findViewById(R.id.TextureViewPreview);
        engine.startPreview(new ZegoCanvas(local_view), ZegoPublishChannel.MAIN);


        //mediaplaer
        final TextureView textureView;
        //初始化控件

        textureView = findViewById(R.id.textureView);
        buttonplay = findViewById(R.id.button_play);
        buttonchosefile = findViewById(R.id.button_filechose);
        setvolume = findViewById(R.id.seekBar_volume);
        setprogress = findViewById(R.id.seekBar_progress);

        mediaplayer = ZegoMediaPlayer.createMediaPlayer();
        mediaplayer.setProgressInterval(10);
        mediaplayer.enableAux(true);
        mediaplayer.setPlayerCanvas(new ZegoCanvas(textureView));

        mediaplayer.setEventHandler(new IZegoMediaPlayerEventHandler() {
            @Override
            public void onMediaPlayerPlayingProgress(ZegoMediaPlayer mediaPlayer,
                                                     long millisecond) {
                playingProgress = millisecond;
                long total = 100 * playingProgress / currentResourceTotalDuration;
                int t = (int) total;
                setprogress.setProgress(t);
            }

            @Override
            public void onMediaPlayerStateUpdate(ZegoMediaPlayer mediaPlayer,
                                                 ZegoMediaPlayerState state, int errorCode) {
                // 本回调在UI线程被回调, 开发者可以在此进行UI的变化, 例如播放按钮的变化
                Log.d(TAG, "onMediaPlayerStateUpdate: state = " + state.value() + ", errorCode = "
                        + errorCode + ", zegoExpressMediaplayer = " + mediaPlayer);
                mediastate = state.value();
                if (mediastate == 3) {
                    //播放结束 更新按钮值
                    buttonplay.setText(getString(R.string.bt_mediaplay));
                }
            }
        });

        mediaplayer.setVideoHandler(new IZegoMediaPlayerVideoHandler() {
            @Override
            public void onVideoFrame(ZegoMediaPlayer mediaPlayer, ByteBuffer[] data,
                                     int[] dataLength, ZegoVideoFrameParam param) {
                for (int i = 0; i < data.length - 1; ++i) {

                    if (readyForPush) {
                        engine.sendCustomVideoCaptureRawData(data[i], dataLength[i], param,
                                playingProgress, ZegoPublishChannel.AUX);//TODO: fix Index 0 HERE
                    }
                }
            }
        }, ZegoVideoFrameFormat.I420);

        engine.startPublishingStream(filmStreamId, ZegoPublishChannel.AUX);

        setVolume();
        setProg();
    }

    public void setVolume() {
        setvolume.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mediaplayer.setVolume(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }

    public void setProg() {
        setprogress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long nowprogress = currentResourceTotalDuration * setprogress.getProgress() / 100;
                mediaplayer.seekTo(nowprogress, new IZegoMediaPlayerSeekToCallback() {
                    @Override
                    public void onSeekToTimeCallback(int errorCode) {
                    }
                });
            }
        });
    }

    public void buttonfilechose(View view) {
        Button button = (Button) view;
        if (button.getText().equals(getString(R.string.bt_filechose))) {
            openSystemFile();
            button.setText(getString(R.string.bt_loadfile));
            isPlayButtonDisabled = true;
            Button playButton = findViewById(R.id.button_play);
            playButton.setVisibility(View.INVISIBLE);
            setprogress.setVisibility(View.INVISIBLE);
            setvolume.setVisibility(View.INVISIBLE);

        } else {
            isPlayButtonDisabled = false;
            Button playButton = findViewById(R.id.button_play);
            playButton.setVisibility(View.VISIBLE);
            setprogress.setVisibility(View.VISIBLE);
            setvolume.setVisibility(View.VISIBLE);

            Log.d(TAG, "BeforeLoading: " + mpath);
            if (mediastate != 0) {
                //mediastate == 0  意味播放器不在播放
                playButton.setText(R.string.bt_mediaplay);
                mediaplayer.stop();
            }
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
                }
            });
            mediaplayer.setVolume(50);
            button.setText(getString(R.string.bt_filechose));
        }
    }

    public void buttonmediaplay(View view) {
        Button button = (Button) view;
        if (button.getText().equals(getString(R.string.bt_mediaplay))) {
            if (mediastate == 2) {
                //如果播放器状态为暂停播放
                mediaplayer.resume();
            } else {
                mediaplayer.start();
            }
            button.setText(getString(R.string.bt_mediapause));
        } else {
            mediaplayer.pause();
            button.setText(getString(R.string.bt_mediaplay));
        }
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //将buttonchosefile作为特征量
            if (buttonchosefile.getVisibility() == View.VISIBLE) {
                //再判断点击是否在所有控件外
                if (hidejudge(buttonchosefile, ev) && hidejudge(buttonplay, ev) &&
                        hidejudge(setvolume, ev) && hidejudge(setprogress, ev)) {
                    buttonchosefile.setVisibility(View.GONE);
                    buttonplay.setVisibility(View.GONE);
                    setvolume.setVisibility(View.GONE);
                    setprogress.setVisibility(View.GONE);
                }

            } else {
                buttonchosefile.setVisibility(View.VISIBLE);

                //对播放按钮和进度条和音量设置做特别判断
                if (isPlayButtonDisabled == false) {
                    buttonplay.setVisibility(View.VISIBLE);
                    setprogress.setVisibility(View.VISIBLE);
                    setvolume.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean hidejudge(View view, MotionEvent ev) {
        //判断点击是否发生在控件外
        int[] location = {0, 0};
        // 获取当前view在屏幕中离四边的边距
        view.getLocationInWindow(location);

        int left = location[0], top = location[1], right = view.getWidth(),
                bottom = top + view.getHeight();

        // 判断点击位置是否在view布局范围内
        if (ev.getRawX() < left || ev.getRawX() > right
                || ev.getY() < top || ev.getRawY() > bottom) {
            return true;
        } else return false;
    }

    @Override
    protected void onDestroy() {
        engine.stopPublishingStream();
        engine.stopPreview();
        engine.logoutRoom(roomId);
        mediaplayer.destroyMediaPlayer();
        mediaplayer = null;
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}
