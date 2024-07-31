package com.chessytrooper.tapwake;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class AlarmDetailsActivity extends AppCompatActivity {

    private TextView alarmTimeTextView;
    private TextView alarmDateTextView;
    private TextView alarmDescriptionTextView;
    private String sound;
    private int duration;
    private Calendar alarmTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "alarm created", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_alarm_detail);

        alarmTimeTextView = findViewById(R.id.alarmTimeTextView);
        alarmDateTextView = findViewById(R.id.alarmDateTextView);
        alarmDescriptionTextView = findViewById(R.id.alarmDescriptionTextView);

        String alarmData = getIntent().getStringExtra("ALARM_DATA");
        Toast.makeText(this, "received alarm " + alarmData, Toast.LENGTH_SHORT).show();
        parseAlarmData(alarmData);
    }

    private void parseAlarmData(String alarmData) {
        try {
            JSONObject alarmJson = new JSONObject(alarmData);
            int hour = alarmJson.getInt("hour");
            int minute = alarmJson.getInt("minute");
            String date = alarmJson.getString("date");
            String description = alarmJson.getString("description");
            sound = alarmJson.getString("sound");
            duration = alarmJson.getInt("duration");

            alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, hour);
            alarmTime.set(Calendar.MINUTE, minute);
            // Parse and set date if needed

            alarmTimeTextView.setText(String.format("%02d:%02d", hour, minute));
            alarmDateTextView.setText(date);
            alarmDescriptionTextView.setText(description);

            scheduleAlarm();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse alarm data", Toast.LENGTH_LONG).show();
        }
    }

    private void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("ALARM_SOUND", sound);
        intent.putExtra("ALARM_DURATION", duration);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }

        Toast.makeText(this, "Alarm scheduled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHomepage();
    }

    private void navigateToHomepage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
