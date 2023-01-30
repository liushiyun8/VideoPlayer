package com.yundesign.videoplayer.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.emp.xdcommon.common.utils.ToastUtil;
import com.yundesign.videoplayer.R;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.common.ConfigManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class PhotoActivity extends BaseActivity {

    //    private ViewPager vp;
//    private MyVPAdater myVPAdater;
    private ConfigManager configManager;
    private Runnable action;
    private ImageView iv;
    private int currentPos;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(Command command) {
        if (command.getCmd() == 11) {
            String content = command.getContent();
            if (content == null)
                return;
            switch (content) {
                case "next":             //下一张图片
                    nextPhoto();
                    break;
                case "prev":             //上一张图片
                    prevPhoto();
                    break;
                case "first":             //第一张照片
                    firstPhoto();
                    break;
                case "last":             //最后一张照片
                    lastPhoto();
                    break;
                case "autostart":             //自动播放
                    autoStart();
                    break;
                case "autostop":             //停止
                    autoStop();
                    break;
                default:
                    if (content.startsWith("image")) {
                        try {
                            int index = Integer.parseInt(content.substring(5)) - 1;
                            if (index < 0)
                                index = 0;
                            if (index > ConfigManager.getInstance().getImgList().size() - 1)
                                index = ConfigManager.getInstance().getImgList().size() - 1;
                            currentPos = index;
                            setImage();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        } else if (command.getCmd() == 8) {
            finish();
        }

    }

    private void autoStop() {
        configManager.setImgAutoPlay(false);
        autoPlay();
    }

    private void autoStart() {
        configManager.setImgAutoPlay(true);
        autoPlay();
    }

    private void lastPhoto() {
        currentPos = ConfigManager.getInstance().getImgList().size() - 1;
        setImage();
    }

    private void setImage() {
        String path = ConfigManager.getInstance().getImgList().get(currentPos);
        Glide.with(this).load(path).placeholder(iv.getDrawable())
                .diskCacheStrategy(DiskCacheStrategy.NONE).into(iv);
    }

    private void firstPhoto() {
        currentPos = 0;
        setImage();
    }

    private void prevPhoto() {
        if (currentPos == 0) {
            ToastUtil.shortTips("已经是第一张了");
            return;
        }
        currentPos--;
        setImage();
    }

    private void nextPhoto() {
        if (currentPos == ConfigManager.getInstance().getImgList().size() - 1) {
            ToastUtil.shortTips("已经是最后一张了");
            return;
        }
        currentPos++;
        setImage();
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected void initView() {
        iv = findViewById(R.id.iv);
        configManager = ConfigManager.getInstance();
//        List<String> imgList = configManager.getImgList();
//        Log.e(TAG,"image:"+ imgList);
//        myVPAdater = new MyVPAdater(this, imgList);
//        vp.setAdapter(myVPAdater);
        setImage();
        action = new Runnable() {
            @Override
            public void run() {
                currentPos++;
                if (currentPos >= ConfigManager.getInstance().getImgList().size()) {
                    currentPos = 0;
                }
                setImage();
                iv.postDelayed(this, Math.max(configManager.getImgInterval(), 0));
            }
        };
        autoPlay();
    }

    private void autoPlay() {
        iv.removeCallbacks(action);
        if (configManager.isImgAutoPlay()) {
            iv.postDelayed(action, Math.max(configManager.getImgInterval(), 0));
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_photo;
    }

    public static class MyVPAdater extends PagerAdapter {

        private final Context mContext;
        private final List<String> mData;

        public MyVPAdater(Context context, List<String> data) {
            this.mContext = context;
            this.mData = data;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView iv = new ImageView(mContext);
            Glide.with(mContext).load(mData.get(position)).into(iv);
            container.addView(iv);
            return iv;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iv.removeCallbacks(action);
    }
}
