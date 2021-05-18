package com.citrontek.audiodetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.citrontek.audiodetection.recoder.RecorderManager;
import com.citrontek.audiodetection.recoder.RecordingUtil;
import com.citrontek.audiodetection.view.ChartView;
import com.citrontek.audiodetection.view.ChartViewFFT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btn_test,btn_recode;
    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private  static final int REQUEST_CODE=2223;
    private boolean isRecoding;
    private ChartView chartView;
    private ChartViewFFT chartViewFFT;
    private String filePath= "test.mp3";
    private RecorderManager recorderManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        //检查权限
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        }


    }

    private void initPlay(){
        //初始化音频资源
        //mediaPlayer=new MediaPlayer();
        //mediaPlayer.setDataSource();
        Uri uri = Uri.parse(getFilesDir()+filePath);
        if(uri!=null){
            mediaPlayer=MediaPlayer.create(this, uri);
        }else {
            mediaPlayer=MediaPlayer.create(this, R.raw.test5);
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                visualizer.setEnabled(false);
                Log.i(TAG, "onCompletion: 播放完成");
            }
        });

        //声明Visualizer
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        //设置采样大小
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            /**
             * 返回波形信息
             */
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                chartView.updateVisualizer(waveform);
            }

            @Override
            /**
             *返回经过fft变换后的信息
             */
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) 					{

                chartViewFFT.updateVisualizer(fft);
            }
        }, Visualizer.getMaxCaptureRate() /2, true, true);
    }

    private void initView(){
        btn_test=findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isRecoding){
                    initPlay();
                    visualizer.setEnabled(true);
                    mediaPlayer.start();
                }

            }
        });
        btn_recode=findViewById(R.id.btn_show);
        btn_recode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecoding){
                    isRecoding=false;
                    recorderManager.stop();
                    btn_recode.setText("录音");
                }else {
                    recorderManager=new RecordingUtil(getApplicationContext(),getFilesDir() + filePath);
                    isRecoding=true;
                    recorderManager.start();
                    btn_recode.setText("停止录音");
                }
            }
        });
        chartView=findViewById(R.id.voice_view);
        chartViewFFT=findViewById(R.id.fft_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recorderManager!=null){
            recorderManager.stop();
        }
        //销毁音频资源
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            visualizer.setEnabled(false);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        //停止采样
        if (visualizer != null) {
            visualizer.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"权限已获取成功",Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onRequestPermissionsResult: 11111");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                } else {
                    Log.i(TAG, "onRequestPermissionsResult: 22222222");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }
}