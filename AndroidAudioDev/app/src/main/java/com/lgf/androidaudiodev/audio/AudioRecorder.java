package com.lgf.androidaudiodev.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.lgf.androidaudiodev.bean.AudioInfo;
import com.lgf.androidaudiodev.utils.ByteUtil;
import com.lgf.androidaudiodev.utils.FileUtil;
import com.lgf.androidaudiodev.utils.TimeUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by garment on 2018/10/25.
 * 音频录制器，使用的是AudioRecord接口。
 */

public class AudioRecorder {

    private final String TAG = AudioRecorder.class.getSimpleName();

    /** 默认音频源*/
    public final int DEF_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    /**默认音频采用率*/
    public int DEF_SAMPLE_RATE = 44100;

    /**默认音频声道,单声道*/
    public int DEF_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    /**默认音频编码格式，注意，如果使用的是ENCODING_PCM_16BIT，则读取音频数据时，需使用public int read (short[] audioData,
     int offsetInShorts, int sizeInShorts)，使用的是short类型的数组*/
    public int DEF_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**PCM文件后缀名*/
    public final String PCM_FILE_SUFFIX = ".pcm";

    /** wav文件后缀名*/
    public final String WAV_FILE_SUFFIX = ".wav";

    public String CHILD_PATH = "AudioRecorder";

    /**采集数据缓冲区大小*/
    private int mBufferSize = 0;

    /**录音状态*/
    private RecordStatus mStatus = RecordStatus.STATUS_NOT_READY;

    /**AudioRecod对象*/
    private AudioRecord mAudioRecord;

    /**生成的音频文件名*/
    private String mCurrentAudioFilePath;

    /**音频录制业务线程池*/
    private ExecutorService mThreadPoolExecutor;

    private AudioTrackPlayer mAudioTrackPlayer;

    //私有构造函数
    private AudioRecorder(){

    }

    public static AudioRecorder getInstance(){
        return AudioRecorderInstance.audioRecorder;
    }

    public static class AudioRecorderInstance{
        private static AudioRecorder audioRecorder = new AudioRecorder();
    }

    /**
     * 初始化AudioRecorder
     * @param audioSource
     * @param sampleRate
     * @param channelConfig
     * @param audioFormat
     * @param bufferSizeInByte
     */
    public void initAudioRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat, int bufferSizeInByte){
        mAudioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSizeInByte);
        mStatus = RecordStatus.STATUS_READY;
        mThreadPoolExecutor = Executors.newCachedThreadPool();
    }

    /**
     * 初始化AudioRecorder
     */
    public void initAudioRecorder(){
        mBufferSize = AudioRecord.getMinBufferSize(DEF_SAMPLE_RATE, DEF_CHANNEL_CONFIG, DEF_AUDIO_FORMAT);
        //get 2048 byte by the default value.
        Log.i(TAG, "### initAudioRecorder buffersize:" + mBufferSize);
        mAudioRecord = new AudioRecord(DEF_AUDIO_SOURCE, DEF_SAMPLE_RATE, DEF_CHANNEL_CONFIG, DEF_AUDIO_FORMAT, mBufferSize);
        mStatus = RecordStatus.STATUS_READY;
        mThreadPoolExecutor = Executors.newCachedThreadPool();
        mAudioTrackPlayer = new AudioTrackPlayer();
        mAudioTrackPlayer.initializePlayer();
    }

    /**
     * 开始录音
     */
    public void startRecording(final boolean isRestart){
        if (mAudioRecord == null){
            throw new IllegalStateException("初始化失败，请检查是否禁止了录音相关权限");
        }
        mAudioRecord.startRecording();
        mStatus = RecordStatus.STATUS_RECORDING;
        //执行音频数据的处理
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                processAudioData(isRestart);
            }
        });
    }

    /**
     * 音频数据的处理,保存为.pcm格式的文件
     */
    private void processAudioData(boolean isRestart){
        File audioFile = null;
        if (isRestart){
            audioFile = new File(mCurrentAudioFilePath);
        } else {
            long timeStamp = System.currentTimeMillis();
            audioFile = new File(FileUtil.getExternalStoragePath(CHILD_PATH), TimeUtils.getFormatTimeStr(timeStamp, "yyyyMMddhhmmss") + PCM_FILE_SUFFIX);
            FileUtil.createFile(audioFile);
            mCurrentAudioFilePath = audioFile.getAbsolutePath();
        }

        DataOutputStream dataOutputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(audioFile);
            //注意一级级的包装，不能跨级包装
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
            short[] shortBuffer = new short[mBufferSize];

            byte[] buffer = new byte[mBufferSize];
            while (mStatus == RecordStatus.STATUS_RECORDING){
                int len = mAudioRecord.read(shortBuffer, 0, mBufferSize);
                for (int i = 0; i < len; i ++){
                    //注意读取short类型的数据时，需要使用小端的模式。
                    dataOutputStream.writeShort(Short.reverseBytes(shortBuffer[i]));
                }

//                mAudioTrackPlayer.play(ByteUtil.shortArrToByteArrLittleEndian(shortBuffer, len), 0, buffer.length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (dataOutputStream != null){
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 暂停录音
     */
    public boolean pauseRecording(){
        mStatus = RecordStatus.STATUS_PAUSED;
        return true;
    }

    /**
     * 完成录音，并保存录音文件，把.pcm格式的音频文件转换为.wav格式的音频
     */
    public boolean finishRecording(int duration){
        mStatus = RecordStatus.STATUS_FINISHED;
        long timeStamp = System.currentTimeMillis();
        String fileName = TimeUtils.getFormatTimeStr(timeStamp, "yyyyMMddhhmmss") + WAV_FILE_SUFFIX;
        File audioFile = new File(FileUtil.getExternalStoragePath(CHILD_PATH), fileName);
        FileUtil.transformPcmToWav(mCurrentAudioFilePath, audioFile.getAbsolutePath(), false);
        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setAudioFileName(fileName);
        audioInfo.setFilePath(audioFile.getAbsolutePath());
        audioInfo.setDuration(duration);
        audioInfo.save();
        return true;
    }

    /**
     * 获取录音状态
     * @return
     */
    public RecordStatus getRecorderStatus(){
        return mStatus;
    }

    public enum RecordStatus{
        //准备录制
        STATUS_NOT_READY,
        //准备好录制
        STATUS_READY,
        //正在录制
        STATUS_RECORDING,
        //暂停录制
        STATUS_PAUSED,
        //完成录制
        STATUS_FINISHED,
        //录制失败
        STATUS_FIALED
    }
}
