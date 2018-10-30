package com.lgf.androidaudiodev;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lgf.androidaudiodev.audio.AudioRecorder;
import com.lgf.androidaudiodev.base.BaseActivity;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private TextView mRecordTimeTV;

    private Button mStartPauseBtn;
    private Button mFinishListBtn;

    private Timer mTimer;
    private int mRecordTime;

    private TimeHandler mTimeHandler;


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        mRecordTimeTV = (TextView) findViewById(R.id.tv_record_time);
        mStartPauseBtn = (Button) findViewById(R.id.btn_start_pause);
        mFinishListBtn = (Button) findViewById(R.id.btn_finish_list);
        mStartPauseBtn.setOnClickListener(this);
        mFinishListBtn.setOnClickListener(this);
    }

    @Override
    public void initData() {
        mTimeHandler = new TimeHandler();
        AudioRecorder.getInstance().initAudioRecorder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_pause:
                onAudioControl();
                break;
            case R.id.btn_finish_list:
                onFinishListControl();
                break;
            default:
        }
    }

    private void onAudioControl() {
        AudioRecorder.RecordStatus recordStatus = AudioRecorder.getInstance().getRecorderStatus();
        if (recordStatus == AudioRecorder.RecordStatus.STATUS_READY || recordStatus == AudioRecorder.RecordStatus.STATUS_FINISHED) {
            //开始
            //听说可以完成
//            AudioManager audioManager =(AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
//
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//
//            audioManager.setSpeakerphoneOn(true);

            AudioRecorder.getInstance().startRecording(false);
            startTimer(0);
            mStartPauseBtn.setText("press to pause");
            mFinishListBtn.setText("press to finish");
        } else if (recordStatus == AudioRecorder.RecordStatus.STATUS_RECORDING) {
            //暂停
            AudioRecorder.getInstance().pauseRecording();
            mTimer.cancel();
            mStartPauseBtn.setText("press to restart");
            mFinishListBtn.setText("press to finish");
        } else if (recordStatus == AudioRecorder.RecordStatus.STATUS_PAUSED) {
            //继续
            AudioRecorder.getInstance().startRecording(true);
            startTimer(mRecordTime);
            mStartPauseBtn.setText("press to pause");
            mFinishListBtn.setText("press to finish");
        }
    }

    private void onFinishListControl(){
        AudioRecorder.RecordStatus recordStatus = AudioRecorder.getInstance().getRecorderStatus();
        if (recordStatus == AudioRecorder.RecordStatus.STATUS_RECORDING || recordStatus == AudioRecorder.RecordStatus.STATUS_PAUSED){
            AudioRecorder.getInstance().finishRecording(mRecordTime);
            mStartPauseBtn.setText("press to start");
            mFinishListBtn.setText("show audio list");
        } else {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, AudioListActivity.class);
            startActivity(intent);
        }
    }

    private void startTimer(final int initTime) {
        final long baseTime = SystemClock.elapsedRealtime();
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                int time = (int) ((SystemClock.elapsedRealtime() - baseTime) / 1000) + initTime;
                mRecordTime = time;
                String hh = new DecimalFormat("00").format(time / 3600);
                String mm = new DecimalFormat("00").format(time % 3600 / 60);
                String ss = new DecimalFormat("00").format(time % 60);
                String timeFormat = new String(hh + ":" + mm + ":" + ss);
                Message msg = new Message();
                msg.obj = timeFormat;
                mTimeHandler.sendMessage(msg);
            }
        }, 0, 1000);
    }

    private class TimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mRecordTimeTV.setText((String) msg.obj);
        }
    }
}
