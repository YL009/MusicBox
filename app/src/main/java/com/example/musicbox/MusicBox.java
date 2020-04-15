package com.example.musicbox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicBox extends Activity implements View.OnClickListener {
    // 获取界面中显示歌曲标题、作者文本框
    TextView title, author;
    // 播放/暂停、停止按钮
    ImageButton play, stop,pre,next;
    // 声明音量管理器
    public AudioManager mAudioManager = null;
    // 定义进度条
    public static SeekBar audioSeekBar = null;
    // 定义音量大小
    public SeekBar audioVolume = null;
    ActivityReceiver activityReceiver;
    public static final String CTL_ACTION = "org.crazyit.action.CTL_ACTION";
    public static final String UPDATE_ACTION = "org.crazyit.action.UPDATE_ACTION";
    // 定义音乐的播放状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    String[] titleStrs = new String[] { "心愿", "约定", "美丽新世界" };
    String[] authorStrs = new String[] { "未知艺术家", "周蕙", "伍佰" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 获取程序界面界面中的两个按钮
        play = (ImageButton) this.findViewById(R.id.play);
        stop = (ImageButton) this.findViewById(R.id.stop);
        title = (TextView) findViewById(R.id.title);
        author = (TextView) findViewById(R.id.author);
        audioVolume = (SeekBar) findViewById(R.id.seekBar2);
        audioSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        pre=(ImageButton) this.findViewById(R.id.pre);
        next=(ImageButton) this.findViewById(R.id.next);

        // 为两个按钮的单击事件添加监听器
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        pre.setOnClickListener(this);
        next.setOnClickListener(this);

        // 播放进度监听
        audioSeekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        // 退出后再次进去程序时，进度条保持持续更新
        if (MusicService.mPlayer != null) {
            // 设置进度条的最大值
            MusicBox.audioSeekBar.setMax(MusicService.mPlayer.getDuration());
            audioSeekBar.setProgress(MusicService.mPlayer.getCurrentPosition());
        }
        // 得到当前音量对象
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // 把当前音量值赋给进度条
        audioVolume.setProgress(mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));
        // 监听音量
        audioVolume.setOnSeekBarChangeListener(new AudioVolumeChangeEvent());

        activityReceiver = new ActivityReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        // 注册BroadcastReceiver
        registerReceiver(activityReceiver, filter);

        Intent intent = new Intent(this, MusicService.class);
        // 启动后台Service
        startService(intent);
    }

    // 音量监听
    class AudioVolumeChangeEvent implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
                    0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    // 播放进度监听，别忘了Service里面还有个进度条刷新
    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // 假设改变源于用户拖动
            if (fromUser) {
                MusicService.mPlayer.seekTo(progress);
                // 当进度条的值改变时，音乐播放器从新的位置开始播放
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            MusicService.mPlayer.pause();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MusicService.mPlayer.start();
        }

    }

    // 自定义的BroadcastReceiver，负责监听从Service传回来的广播
    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取Intent中的update消息，update代表播放状态
            int update = intent.getIntExtra("update", -1);
            // 获取Intent中的current消息，current代表当前正在播放的歌曲
            int current = intent.getIntExtra("current", -1);
            if (current >= 0) {
                title.setText(titleStrs[current]);
                author.setText(authorStrs[current]);
            }
            switch (update) {
                case 0x11:
                    play.setImageResource(R.drawable.play);
                    status = 0x11;
                    break;
                // 控制系统进入播放状态
                case 0x12:
                    // 播放状态下设置使用暂停图标
                    play.setImageResource(R.drawable.pause);
                    // 设置当前状态
                    status = 0x12;
                    break;
                // 控制系统进入暂停状态
                case 0x13:
                    // 暂停状态下设置使用播放图标
                    play.setImageResource(R.drawable.play);
                    // 设置当前状态
                    status = 0x13;
                    break;
            }
        }
    }

    @Override
    public void onClick(View source) {
        // 创建Intent
        Intent intent = new Intent("org.crazyit.action.CTL_ACTION");
        switch (source.getId()) {
            // 按下播放/暂停按钮
            case R.id.play:
                intent.putExtra("control", 1);
                break;
            // 按下停止按钮
            case R.id.stop:
                intent.putExtra("control", 2);
                break;
            case R.id.pre:
                intent.putExtra("control", 3);
                break;
            case R.id.next:
                intent.putExtra("control", 4);
                break;
        }
        // 发送广播，将被Service组件中的BroadcastReceiver接收到
        sendBroadcast(intent);
    }
}