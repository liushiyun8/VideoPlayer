package com.yundesign.videoplayer.views;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public class MyVideoView extends StandardGSYVideoPlayer {

    private Runnable disAction;

    public MyVideoView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
        initView();
    }

    public MyVideoView(Context context) {
        super(context);
        initView();
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        disAction = this::touchSurfaceUp;
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
        int position = getCurrentPositionWhenPlaying();
        int i = position * 100 / getDuration();
        changeProcess(i+1);
    }

    public void back(){
        int position = getCurrentPositionWhenPlaying();
        int i = position * 100 / getDuration();
        changeProcess(i-1);
    }

    public void changeProcess(int pro) {
        if (pro > 100)
            pro = 100;
        if (pro < 0)
            pro = 0;
        int totalTimeDuration = getDuration();
        mSeekTimePosition = (int) (pro * totalTimeDuration / 100f);
        if (mSeekTimePosition > totalTimeDuration)
            mSeekTimePosition = totalTimeDuration;
//        String seekTime = CommonUtil.stringForTime(mSeekTimePosition);
//        String totalTime = CommonUtil.stringForTime(totalTimeDuration);
//        showProgressDialog(pro, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        if (getGSYVideoManager() != null && (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE)) {
            try {
                getGSYVideoManager().seekTo(mSeekTimePosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            int duration = getDuration();
//            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
//            if (mProgressBar != null) {
//                mProgressBar.setProgress(progress);
//            }
//            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
//                Debuger.printfLog("onTouchScreenSeekPosition");
//                mVideoAllCallBack.onTouchScreenSeekPosition(mOriginUrl, mTitle, this);
//            }
        }
    }

    public void changeBrightness(float percent) {
        WindowManager.LayoutParams lpa = ((Activity) (mContext)).getWindow().getAttributes();
        lpa.screenBrightness = percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
//        showBrightnessDialog(lpa.screenBrightness);
        ((Activity) (mContext)).getWindow().setAttributes(lpa);
//        removeCallbacks(disAction);
//        postDelayed(disAction,3000);
    }

    @Override
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime, boolean forceChange) {

    }

    protected void setViewShowState(View view, int visibility) {
        if (view != null) {
            view.setVisibility(GONE);
        }
    }
}
