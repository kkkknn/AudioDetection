package com.citrontek.audiodetection.recoder;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class RecordingUtil implements RecorderManager {
    private static final String TAG = "RecordingUtil";
    private Context context = null;
    private String path = null;
    private MediaRecorder mRecorder = null;
    public RecordingUtil(Context context, String path) {
        this.context = context;
        this.path = path;
        mRecorder = new MediaRecorder();
    }

    @Override
    public boolean start() {
        //设置音源为Micphone
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置封装格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(path);
        //设置编码格式
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "prepare() failed");
        }
        //录音
        mRecorder.start();
        return false;
    }

    @Override
    public boolean stop() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        return false;
    }
}
