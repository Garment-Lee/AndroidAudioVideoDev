package com.lgf.androidaudiodev;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lgf.androidaudiodev.audio.AudioTrackPlayer;
import com.lgf.androidaudiodev.audio.WavFileReader;
import com.lgf.androidaudiodev.base.BaseActivity;
import com.lgf.androidaudiodev.bean.AudioInfo;
import org.litepal.LitePal;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by garment on 2018/10/27.
 */

public class AudioListActivity extends BaseActivity{

    private final String TAG = AudioListActivity.class.getSimpleName();

    private RecyclerView mAudioListRV = null;
    private AudioListAdapter mAudioListAdapter = null;
    private List<AudioInfo> mAudioListData;
    private AudioTrackPlayer mAudioTrackPlayer;
    private WavFileReader mWavFileReader;

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_list;
    }

    @Override
    public void initView() {
        mAudioListRV = (RecyclerView) findViewById(R.id.rv_audio_list);
    }

    @Override
    public void initData() {
        mAudioListData = LitePal.findAll(AudioInfo.class);
        mAudioListAdapter = new AudioListAdapter();
        mAudioListRV.setLayoutManager(new LinearLayoutManager(this));
        mAudioListRV.setAdapter(mAudioListAdapter);
        mAudioTrackPlayer = new AudioTrackPlayer();
        mAudioTrackPlayer.initializePlayer();
        mWavFileReader = new WavFileReader();
    }

    private class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>{

        @Override
        public AudioListAdapter.AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_layout, parent, false);
            AudioListAdapter.AudioViewHolder viewHolder = new AudioViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(AudioListAdapter.AudioViewHolder holder, final int position) {
            AudioInfo audioInfo = mAudioListData.get(position);
            holder.fileNameTV.setText(audioInfo.getAudioFileName());
            int duration = audioInfo.getDuration();
            String hh = new DecimalFormat("00").format(duration / 3600);
            String mm = new DecimalFormat("00").format(duration % 3600 / 60);
            String ss = new DecimalFormat("00").format(duration % 60);
            String timeFormat = new String(hh + ":" + mm + ":" + ss);
            holder.timeLengthTV.setText(timeFormat);
            holder.recordDateTV.setText(audioInfo.getCreateTime());
            holder.playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String filePath = mAudioListData.get(position).getFilePath();
                    Log.i(TAG, "### filePath:" + filePath);
                    final byte[] buffer = new byte[1024*2];
                    try {
                        mWavFileReader.openFile(filePath);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int length;
                                while ((length = mWavFileReader.readData(buffer,0,buffer.length)) > 0){
                                    mAudioTrackPlayer.play(buffer, 0, length);
                                }
                            }
                        }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAudioListData == null ? 0 : mAudioListData.size();
        }

        public class AudioViewHolder extends RecyclerView.ViewHolder{

            Button playBtn;
            TextView fileNameTV;
            TextView timeLengthTV;
            TextView recordDateTV;

            public AudioViewHolder(View itemView) {
                super(itemView);
                playBtn = (Button)itemView.findViewById(R.id.btn_audio_play);
                fileNameTV = (TextView) itemView.findViewById(R.id.tv_audio_file_name);
                timeLengthTV = (TextView) itemView.findViewById(R.id.tv_audio_time_length);
                recordDateTV = (TextView) itemView.findViewById(R.id.tv_audio_record_date);
            }
        }
    }
}