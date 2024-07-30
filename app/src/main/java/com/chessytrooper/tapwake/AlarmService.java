package com.chessytrooper.tapwake;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.Handler;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String alarmSoundUri = intent.getStringExtra("ALARM_SOUND");
        int duration = intent.getIntExtra("ALARM_DURATION", 5 * 60 * 1000);

        playAlarm(Uri.parse(alarmSoundUri), duration);

        return START_STICKY;
    }

    private void playAlarm(Uri alarmSound, int duration) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
            mediaPlayer.setDataSource(getApplicationContext(), alarmSound);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

            handler.postDelayed(this::stopSelf, duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}