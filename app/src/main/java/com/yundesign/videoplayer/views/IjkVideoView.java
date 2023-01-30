package com.yundesign.videoplayer.views;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.emp.xdcommon.android.log.LogUtils;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkVideoView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "IjkVideoView";
    private IjkMediaPlayer mediaPlayer;
    private String videoUrl;
    private AudioManager mAudioManager;
    private long mSeekTimePosition;
    private IMediaPlayer.OnPreparedListener listener;
    private IMediaPlayer.OnCompletionListener stalistener;
    private boolean needPlay;

    public IjkVideoView(Context context) {
        this(context, null);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        getHolder().addCallback(this);
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener listener) {
        this.listener = listener;
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener listener) {
        this.stalistener = listener;
    }

    private void createPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new IjkMediaPlayer();
        }
        try {
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.setOnPreparedListener(iMediaPlayer -> {
                if (listener != null)
                    listener.onPrepared(iMediaPlayer);
                if (needPlay)
                    iMediaPlayer.start();
            });
            if (stalistener != null)
                mediaPlayer.setOnCompletionListener(stalistener);
            //视频缓存好之后是否自动播放 1、允许 0、不允许
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVideoPath(String url) {
        videoUrl = url;
//        if (getVisibility() == VISIBLE) {
//            setVisibility(GONE);
//            setVisibility(VISIBLE);
//        }
        if (mediaPlayer != null) {
            release();
            createPlayer();
            mediaPlayer.setDisplay(getHolder());
        }
    }

    public void start() {
        needPlay = true;
        if (mediaPlayer != null)
            mediaPlayer.start();
    }

    public void pause() {
        needPlay = false;
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void resume() {
        needPlay = true;
        if (mediaPlayer != null) {
//            mediaPlayer.reset();
            mediaPlayer.start();
        }

    }

    public void stop() {
        needPlay = false;
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.e(TAG, "surfaceCreated");
        createPlayer();
        mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        release();
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

    public void changeProcess(int pro) {
        Log.e(TAG,"seek 位置："+pro);
        LogUtils.e(TAG,"seek 位置："+pro);
        if (pro > 100)
            pro = 100;
        if (pro < 0)
            pro = 0;
        if (mediaPlayer != null) {
            long totalTimeDuration = mediaPlayer.getDuration();
            mSeekTimePosition = (int) (pro * totalTimeDuration / 100f);
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            mediaPlayer.seekTo(mSeekTimePosition);
        }
    }

    public void changeBrightness(float percent) {
        WindowManager.LayoutParams lpa = ((Activity) (getContext())).getWindow().getAttributes();
        lpa.screenBrightness = percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
//        showBrightnessDialog(lpa.screenBrightness);
        ((Activity) (getContext())).getWindow().setAttributes(lpa);
//        removeCallbacks(disAction);
//        postDelayed(disAction,3000);
    }

    public void volumeUp(){
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume++;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    public void volumeDown(){
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume--;
        if(volume<0)
            volume=0;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    public void next(){
        long position = mediaPlayer.getCurrentPosition();
        long i = position * 100 / mediaPlayer.getDuration();
        Log.e(TAG,"position:"+position+",i:"+i+",duration:"+ mediaPlayer.getDuration());
        changeProcess((int) i+1);
    }

    public void back(){
        long position = mediaPlayer.getCurrentPosition();
        long i = position * 100 / mediaPlayer.getDuration();
        changeProcess((int) i-1);
    }
}
