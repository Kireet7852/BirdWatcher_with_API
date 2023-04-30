package com.example.birdwatcher.helpers;


import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.birdwatcher.R;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ApiAudioHelperActivity extends AppCompatActivity {

    TextView textview2;
    TextView textview3;
    Button pickAudio;
    Button button2;
    SeekBar seekbar1;

    String duration;
    MediaPlayer mediaPlayer;
    ScheduledExecutorService timer;
    public static final int PICK_FILE =99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_helper_audio);

        pickAudio = findViewById(R.id.buttonPickAudio);
        button2 = findViewById(R.id.button2);
        textview2 = findViewById(R.id.textView2);
        textview3 = findViewById(R.id.textView3);
        seekbar1 = findViewById(R.id.seekbar1);

        pickAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                startActivityForResult(intent, PICK_FILE);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        button2.setText("PLAY");
                        timer.shutdown();
                    } else {
                        mediaPlayer.start();
                        button2.setText("PAUSE");

                        timer = Executors.newScheduledThreadPool(1);
                        timer.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer != null) {
                                    if (!seekbar1.isPressed()) {
                                        seekbar1.setProgress(mediaPlayer.getCurrentPosition());
                                    }
                                }
                            }
                        }, 10, 10, TimeUnit.MILLISECONDS);
                    }
                }
            }
        });

        seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null){
                    int millis = mediaPlayer.getCurrentPosition();
                    long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
                    long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
                    long secs = total_secs - (mins*60);
                    textview3.setText(mins + ":" + secs + " / " + duration);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekbar1.getProgress());
                }
            }
        });

        button2.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK){
            if (data != null){
                Uri uri = data.getData();
                createMediaPlayer(uri);
                audioDetection(uri);
            }
        }
    }

    protected void audioDetection(Uri selectedAudioUri) {}

    public void createMediaPlayer(Uri uri){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();

            textview2.setText(getNameFromUri(uri));
            button2.setEnabled(true);

            int millis = mediaPlayer.getDuration();
            long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
            long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
            long secs = total_secs - (mins*60);
            duration = mins + ":" + secs;
            textview3.setText("00:00 / " + duration);
            seekbar1.setMax(millis);
            seekbar1.setProgress(0);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseMediaPlayer();
                }
            });
        } catch (IOException e){
            textview2.setText(e.toString());
        }
    }

    public String getNameFromUri(Uri uri){
        String fileName = "";
        Cursor cursor = null;
        cursor = getContentResolver().query(uri, new String[]{
                MediaStore.Images.ImageColumns.DISPLAY_NAME
        }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
        }
        if (cursor != null) {
            cursor.close();
        }
        return fileName;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    public void releaseMediaPlayer(){
        if (timer != null) {
            timer.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        button2.setEnabled(false);
        textview2.setText("TITLE");
        textview3.setText("00:00 / 00:00");
        seekbar1.setMax(100);
        seekbar1.setProgress(0);
    }
}