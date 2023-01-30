package com.yundesign.videoplayer.ui;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yundesign.videoplayer.R;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.common.AppConfig;
import com.yundesign.videoplayer.common.ConfigManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

public class AndroidVideoActivity extends BaseActivity {

    private List<String> videoList;
    private int currentPos;
    private VideoView detailPlayer;
    private ConfigManager configManager;
    private ImageView iv;
    private String currentUrl;
    private AudioManager mAudioManager;

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
                stopVideo();
                break;
            case 4:             //音量
                changeVolume(command.getValue());
                break;
            case 5:             //亮度
                changeBrightness(command.getValue() / 100f);
                break;
            case 6:
                detailPlayer.seekTo(command.getValue());
                break;
            case 7:             //切换视频源
                String content = command.getContent();
                if (content.startsWith("video")) {
                    try {
                        int index = Integer.parseInt(content.substring(5)) - 1;
                        if (index < 0)
                            index = 0;
                        if (index > videoList.size() - 1)
                            index = videoList.size() - 1;
                        currentPos = index;
                        String url = videoList.get(currentPos);
                        if (new File(url).exists()) {
                            initVideo(url);
                            playVideo();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    initVideo(content);
                    playVideo();
                }
                break;
            case 8:
                finish();
        }
    }

    private void initVideo(String url) {
        currentUrl = url;
        detailPlayer.setVideoPath(url);
    }

    private void stopVideo() {
        iv.setVisibility(View.VISIBLE);
        detailPlayer.pause();
    }

    private void resumeVideo() {
        detailPlayer.resume();
    }

    private void pauseVideo() {
        detailPlayer.pause();
    }

    private void playVideo() {
        iv.setVisibility(View.GONE);
        detailPlayer.start();
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected void initView() {
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        configManager = ConfigManager.getInstance();
        detailPlayer = findViewById(R.id.videoView);
        iv = findViewById(R.id.iv);
        if (new File(AppConfig.BACK_IMG + ".png").exists()) {
            Glide.with(this).load(AppConfig.BACK_IMG + ".png").diskCacheStrategy(DiskCacheStrategy.NONE).into(iv);
        }

        if (new File(AppConfig.BACK_IMG + ".jpg").exists()) {
            Glide.with(this).load(AppConfig.BACK_IMG + ".jpg").diskCacheStrategy(DiskCacheStrategy.NONE).into(iv);
        }
        detailPlayer.setOnCompletionListener(iMediaPlayer -> {
            Log.e(TAG, "OnCompletionListener");
            if (configManager.getLoopMode() == 1) {
                iv.setVisibility(View.VISIBLE);
            } else if (configManager.getLoopMode() == 2) {
                currentPos++;
                if (currentPos > videoList.size() - 1) {
                    currentPos = 0;
                }
                initVideo(videoList.get(currentPos));
                playVideo();
            } else if (configManager.getLoopMode() == 3) {
                if (!TextUtils.isEmpty(currentUrl)) {
                    initVideo(currentUrl);
                } else
                    initVideo(videoList.get(currentPos));
                playVideo();
            }
        });
        detailPlayer.setOnPreparedListener(iMediaPlayer -> {
            Log.e(TAG, "OnPreparedListener");
        });
        videoList = configManager.getVideoList();
        if (videoList.size() > 0) {
            initVideo(videoList.get(0));
        }
        Log.e(TAG, "autoPlay:" + configManager.isAutoPlay());
        if (configManager.isAutoPlay())
            playVideo();

    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_android_video;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void changeVolume(int volume) {
        if (volume > 100)
            volume = 100;
        if (volume < 0)
            volume = 0;
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int deltaV = (int) (max * volume / 100f);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, deltaV, 0);
//        showVolumeDialog(0, volume);
//        removeCallbacks(disAction);
//        postDelayed(disAction,3000);
    }

    public void changeBrightness(float percent) {
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
//        showBrightnessDialog(lpa.screenBrightness);
        getWindow().setAttributes(lpa);
//        removeCallbacks(disAction);
//        postDelayed(disAction,3000);
    }

}
