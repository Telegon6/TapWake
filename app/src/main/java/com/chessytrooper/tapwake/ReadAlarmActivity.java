package com.chessytrooper.tapwake;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class ReadAlarmActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private Button actionButton;
    private ImageView scanningCircle;
    private AnimatedVectorDrawable expandingCircleAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_alarm);

        scanningCircle = findViewById(R.id.scanningCircle);
        actionButton = findViewById(R.id.actionButton);

        expandingCircleAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.expanding_circle);
        scanningCircle.setImageDrawable(expandingCircleAnimation);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int flags = 0;
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags | PendingIntent.FLAG_IMMUTABLE);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFiltersArray = new IntentFilter[] {tagDetected, ndefDetected, techDetected};

        techListsArray = new String[][] { new String[] { Ndef.class.getName() },
                new String[] { NdefFormatable.class.getName() } };

        // Handle NFC detection if the activity was started by an NFC intent
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            onNewIntent(getIntent());
        }
        try {
            ndefDetected.addDataType("application/json");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndefDetected,};
        techListsArray = new String[][]{new String[]{Ndef.class.getName()}};

        setupUI();
    }

    private void setupUI() {
        actionButton.setText("Scanning");
        actionButton.setEnabled(false);

        actionButton.setOnClickListener(v -> {
            if (actionButton.getText().equals("Download")) {
                actionButton.setText("Downloading");
                actionButton.setEnabled(false);
            } else if (actionButton.getText().equals("Back to Home")) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
        if (expandingCircleAnimation != null && !expandingCircleAnimation.isRunning()) {
            expandingCircleAnimation.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        if (expandingCircleAnimation != null && expandingCircleAnimation.isRunning()) {
            expandingCircleAnimation.stop();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (detectedTag != null) {
                Ndef ndef = Ndef.get(detectedTag);
                if (ndef != null) {
                    try {
                        ndef.connect();
                        NdefMessage ndefMessage = ndef.getNdefMessage();
                        NdefRecord[] records = ndefMessage.getRecords();
                        if (records.length > 0) {
                            NdefRecord record = records[0];
                            String alarmData = new String(record.getPayload(), Charset.forName("US-ASCII"));
                            executeAlarm(alarmData);
                        }
                        ndef.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "NFC Tag Detected", Toast.LENGTH_SHORT).show();
                        if (expandingCircleAnimation != null && expandingCircleAnimation.isRunning()) {
                            expandingCircleAnimation.stop();
                        }
                        scanningCircle.setVisibility(View.GONE);
                        actionButton.setText("Download");
                        actionButton.setEnabled(true);
                    });
                }
            }
        }
    }

    private void executeAlarm(String alarmData) {
        try {
            JSONObject alarmJson = new JSONObject(alarmData);
            int hour = alarmJson.getInt("hour");
            int minute = alarmJson.getInt("minute");
            String date = alarmJson.getString("date");
            String description = alarmJson.getString("description");
            String sound = alarmJson.getString("sound");
            int duration = alarmJson.getInt("duration");

            Intent serviceIntent = new Intent(this, AlarmService.class);
            serviceIntent.putExtra("ALARM_SOUND", sound);
            serviceIntent.putExtra("ALARM_DURATION", duration);
            startService(serviceIntent);

            Toast.makeText(this, "Alarm set for " + hour + ":" + minute + " on " + date, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse alarm data", Toast.LENGTH_LONG).show();
        }
    }
}
