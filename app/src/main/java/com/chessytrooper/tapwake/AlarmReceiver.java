package com.chessytrooper.tapwake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String sound = intent.getStringExtra("ALARM_SOUND");
        int duration = intent.getIntExtra("ALARM_DURATION", 0);

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("ALARM_SOUND", sound);
        serviceIntent.putExtra("ALARM_DURATION", duration);
        context.startService(serviceIntent);
    }
}
