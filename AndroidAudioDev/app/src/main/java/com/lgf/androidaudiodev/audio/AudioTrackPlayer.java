package com.lgf.androidaudiodev.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by garment on 2018/10/28.
 * 音频播放器，使用的是AudioTrack接口
 */

public class AudioTrackPlayer {

    private static final String TAG = AudioTrackPlayer.class.getName();

    public static final int DEF_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    public static final int DEF_SAMPLE_RATE = 44100;
    public static final int DEF_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int DEF_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEF_PLAY_MODE = AudioTrack.MODE_STREAM;

    private boolean mIsPlayerInitialized;
    private AudioTrack mAudioTrack;

    public boolean initializePlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat){
        if (mIsPlayerInitialized){
            Log.e(TAG, "Player has already mIsPlayerInitialized...");
            return false;
        }
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        mAudioTrack = new AudioTrack(streamType,sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, DEF_PLAY_MODE);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
            Log.e(TAG, "AudioTrack initialize fail!");
            return false;
        }
        mIsPlayerInitialized = true;
        return true;
    }

    public boolean initializePlayer(){
        return initializePlayer(DEF_STREAM_TYPE, DEF_SAMPLE_RATE, DEF_CHANNEL_CONFIG, DEF_AUDIO_FORMAT);
    }

    public void stopPlayer(){
        if (!mIsPlayerInitialized){
            return;
        }
        if (mAudioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING){
            mAudioTrack.stop();
        }
        mAudioTrack.release();
        mIsPlayerInitialized = false;
    }

    public boolean play(byte[] audioData, int offsetInBytes, int sizeInBytes){
        if (!mIsPlayerInitialized){
            Log.e(TAG, "Player has been not initialized");
            return false;
        }
        mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
        mAudioTrack.play();
        return true;
    }

}
