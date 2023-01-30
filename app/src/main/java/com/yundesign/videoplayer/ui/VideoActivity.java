package com.yundesign.videoplayer.ui;

import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;
import com.yundesign.videoplayer.R;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.common.AppConfig;
import com.yundesign.videoplayer.common.ConfigManager;
import com.yundesign.videoplayer.views.MyVideoView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

public class VideoActivity extends BaseActivity {


    private MyVideoView detailPlayer;
    private OrientationUtils orientationUtils;
    private boolean isPlay;
    private boolean isPause;
    private ConfigManager configManager;
    private List<String> videoList;
    private int currentPos;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(Command command) {
        switch (command.getCmd()) {
            case 0:             //播放
                playVideo();
                break;
            case 1:             //暂停
                pauseVideo();
                break;
            case 2:              //resume
                resumeVideo();
                break;
            case 3:             //停止
                releaseVideo();
                detailPlayer.getCurrentPlayer().release();
                break;
            case 4:             //音量
                detailPlayer.changeVolume(command.getValue());
                break;
            case 5:             //亮度
                detailPlayer.changeBrightness(command.getValue() / 100f);
                break;
            case 6:
                detailPlayer.changeProcess(command.getValue());
                break;
            case 7:             //切换视频源
                releaseVideo();
                String content = command.getContent();
                if (content.startsWith("video")) {
                    try {
                        int index = Integer.parseInt(content.substring(5)) - 1;
                        if (index < 0)
                            index = 0;
                        if (index > videoList.size() - 1)
                            index = videoList.size() - 1;
                        String url = videoList.get(index);
                        if(new File(url).exists()){
                            initVideo(url);
                            playVideo();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }else {
                    initVideo(content);
                    playVideo();
                }
                break;
            case 8:
                finish();
                break;
            case 9:
                detailPlayer.volumeUp();
                break;
            case 10:
                detailPlayer.volumeDown();
                break;
            case 11:
                detailPlayer.next();
                break;
            case 12:
                detailPlayer.back();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            detailPlayer.changeProcess(detailPlayer.getCurrentPositionWhenPlaying()+10);
        }else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            detailPlayer.changeProcess(detailPlayer.getCurrentPositionWhenPlaying()-10);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected void initView() {
        detailPlayer = findViewById(R.id.videoPlayer);
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
        configManager = ConfigManager.getInstance();
        videoList = configManager.getVideoList();
        if (videoList.size() > 0) {
            initVideo(videoList.get(0));
        }
        if (configManager.isAutoPlay())
            playVideo();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_video;
    }

    private void initVideo(String url) {
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        ImageView imageView = new ImageView(this);
        if (new File(AppConfig.BACK_IMG + ".png").exists()) {
            Glide.with(this).load(AppConfig.BACK_IMG + ".png").into(imageView);
        }

        if (new File(AppConfig.BACK_IMG + ".jpg").exists()) {
            Glide.with(this).load(AppConfig.BACK_IMG + ".jpg").into(imageView);
        }
        if (configManager.getLoopMode() == 3) {
            gsyVideoOption.setLooping(true);
        }
        gsyVideoOption.setThumbImageView(imageView)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setThumbPlay(false)
                .setIsTouchWigetFull(false)
                .setIsTouchWiget(false)
                .setShowPauseCover(false)
                .setShowDragProgressTextOnSeekBar(false)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        orientationUtils.setEnable(true);
                        isPlay = true;
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                        Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//当前非全屏player
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                    }
                }).setLockClickListener((view, lock) -> {
            if (orientationUtils != null) {
                //配合下方的onConfigurationChanged
                orientationUtils.setEnable(!lock);
            }
        }).build(detailPlayer);

        detailPlayer.getFullscreenButton().setOnClickListener(v -> {
            //直接横屏
            orientationUtils.resolveByClick();

            //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
            detailPlayer.startWindowFullscreen(VideoActivity.this, true, true);
        });

        detailPlayer.setGSYStateUiListener(state -> {
            if (state == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE) {
                if (configManager.getLoopMode() == 2) {
                    currentPos++;
                    if (currentPos > videoList.size() - 1) {
                        currentPos = 0;
                    }
                    initVideo(videoList.get(currentPos));
                    playVideo();
                }
            }
        });
        detailPlayer.getBackButton().setVisibility(View.GONE);

    }

    private void playVideo() {
        detailPlayer.startPlayLogic();
    }

    @Override
    public void onBackPressed() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        pauseVideo();
        super.onPause();
    }

    private void pauseVideo() {
        detailPlayer.getCurrentPlayer().onVideoPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        resumeVideo();
        super.onResume();
    }

    private void resumeVideo() {
        detailPlayer.getCurrentPlayer().onVideoResume(false);
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseVideo();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    private void releaseVideo() {
        if (isPlay) {
            detailPlayer.release();
//            detailPlayer.getCurrentPlayer().release();
            isPlay = false;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }
}
