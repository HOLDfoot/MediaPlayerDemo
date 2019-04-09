package app.xiaoming.mediaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.xiaoming.mediaplayer.module.RingToneModule;

public class MainActivity extends AppCompatActivity {
    
    TextView tvPlay;
    Button btnRecord;
    
    static final int startRecord = 1;
    static final int startSuccess = 10;
    static final int startRecordFail = 11;
    static final int stopRecord = 2;
    
    static final int startPlay = 3;
    static final int playSuccess = 30;
    static final int stopPlay = 4;
    static final int playEnd = 5;
    static final int playError = 6;
    
    private int state;
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;
    
    private File outputFile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new RingToneModule(this);
        
        tvPlay = findViewById(R.id.tv_play);
        btnRecord = findViewById(R.id.btn_record);
        tvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == startPlay || state == playSuccess) {
                    // stop
                    
                } else if (outputFile != null) {
                    // play
                    handler.sendEmptyMessage(startPlay);
                } else {
                    Toast.makeText(MainActivity.this, "请先录制", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("zmr", "onClick state= " + state);
                if (state == startSuccess || state == startRecord) {
                    handler.sendEmptyMessage(stopRecord);
                } else {
                    permissionForM();
                }
            }
        });
    }
    
    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1101);
        } else {
            handler.sendEmptyMessage(startRecord);
        }
    }
    
    private void startRecord() {
        Log.d("zmr", "startRecord");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("zmr", "startRecord run");
                File file = new File(Environment.getExternalStorageDirectory(), "zmr/" + System.currentTimeMillis() + ".m4a");
                file.getParentFile().mkdir();
                Log.d("zmr", "startRecord file= " + file.getAbsolutePath());
                try {
                    file.createNewFile();
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioSamplingRate(44100);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setAudioEncodingBitRate(96000);
                    mediaRecorder.setOutputFile(file.getAbsolutePath());
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    // 开始录音成功
                    outputFile = file; // 记录录音的文件
                    handler.sendEmptyMessage(startSuccess);
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(startRecordFail);
                }
    
            }
        });
    }
    
    private void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaPlayer = null;
        }
    }
    
    private void play() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile.getAbsolutePath());
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            handler.sendEmptyMessage(playEnd);
                        }
                    });
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            handler.sendEmptyMessage(playSuccess);
                        }
                    });
                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            handler.sendEmptyMessage(playError);
                            return false;
                        }
                    });
                    mediaPlayer.setVolume(1, 1);
                    mediaPlayer.setLooping(false);
                    mediaPlayer.prepare();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }
    
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            state = msg.what;
            Log.d("zmr", "handleMessage state= " + state);
            switch (msg.what) {
                case startRecord:
                    startRecord();
                    btnRecord.setText("准备开始录制");
                    break;
                case startSuccess:
                    btnRecord.setText("已开始录制");
                    tvPlay.setText(outputFile.getAbsolutePath());
                    break;
                case startRecordFail:
                    btnRecord.setText("开始录制错误");
                    break;
                case stopRecord:
                    btnRecord.setText("点击再次开始录制");
                    stopRecord();
                    break;
                    
                case startPlay:
                    play();
                    tvPlay.setText("准备开始播放");
                    break;
                case playSuccess:
                    tvPlay.setText("已开始播放");
                    break;
                case stopPlay:
                    stopPlay();
                    tvPlay.setText("取消播放, 点击重新播放");
                    break;
                case playEnd:
                    stopPlay();
                    tvPlay.setText("播放结束, 点击重新播放");
                    break;
                case playError:
                    stopPlay();
                    tvPlay.setText("播放错误, 点击重新播放");
                    break;
            }
        }
    };
}
