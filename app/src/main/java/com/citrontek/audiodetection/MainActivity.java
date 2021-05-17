package com.citrontek.audiodetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btn_test;
    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private  static final int REQUEST_CODE=2223;
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

        //初始化音频资源
        //mediaPlayer=new MediaPlayer();
        //mediaPlayer.setDataSource();
        mediaPlayer=MediaPlayer.create(this, R.raw.test);
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
                for (int i = 0; i < 20; i++) {

                    Log.i("TAG", "onWaveFormDataCapture: "+waveform[i]+"||"+samplingRate);
                }
            }

            @Override
            /**
             *返回经过fft变换后的信息
             */
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) 					{
                    byte[] model = new byte[fft.length / 2 + 1];
                    model[0] = (byte) Math.abs(fft[1]);
                    int j = 1;

                    for (int i = 2; i < 18;) {
                        model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                        i += 2;
                        j++;
                    }
                for (int i = 0; i < model.length; i++) {

                    Log.i("TAG", "onWaveFormDataCapture: "+model[i]+"||"+samplingRate);
                }
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);


    }

    private void initView(){
        btn_test=findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer!=null){
                    visualizer.setEnabled(true);
                    mediaPlayer.start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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