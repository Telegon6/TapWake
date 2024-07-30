package com.chessytrooper.tapwake;

import android.animation.ObjectAnimator;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

public class NfcWriteActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private ImageView scanningCircle;
    private Button actionButton;
    private ObjectAnimator rotationAnimator;
    private AnimatedVectorDrawable expandingCircleAnimation;
    private String alarmData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_IMMUTABLE;
        }
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef,};
        techListsArray = new String[][]{new String[]{Ndef.class.getName()}};

        alarmData = createAlarmJson();

        setupUI();
    }

    private void setupUI() {
//        rotationAnimator = ObjectAnimator.ofFloat(scanningCircle, "rotation", 0f, 360f);
//        rotationAnimator.setDuration(2000);
//        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
//        rotationAnimator.setInterpolator(new LinearInterpolator());

        actionButton.setText("Scanning");
        actionButton.setEnabled(false);
        //rotationAnimator.start();

        actionButton.setOnClickListener(v -> {
            if (actionButton.getText().equals("Upload")) {
                actionButton.setText("Uploading");
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
                if (expandingCircleAnimation != null && expandingCircleAnimation.isRunning()) {
                    expandingCircleAnimation.stop();
                }
                scanningCircle.setVisibility(View.GONE);
                actionButton.setText("Upload");
                actionButton.setEnabled(true);
            }
        }
    }

    private void writeNfcTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                NdefRecord mimeRecord = NdefRecord.createMime("application/json", alarmData.getBytes(Charset.forName("US-ASCII")));
                NdefMessage ndefMessage = new NdefMessage(mimeRecord);
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Alarm data written to NFC tag", Toast.LENGTH_LONG).show();
                    actionButton.setText("Back to Home");
                    actionButton.setEnabled(true);
                });
            }
        } catch (IOException | FormatException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "Error writing to NFC tag", Toast.LENGTH_LONG).show();
                actionButton.setText("Upload");
                actionButton.setEnabled(true);
            });
        }
    }

    private String createAlarmJson() {
        Intent intent = getIntent();
        JSONObject json = new JSONObject();
        try {
            json.put("hour", intent.getIntExtra("ALARM_HOUR", 0));
            json.put("minute", intent.getIntExtra("ALARM_MINUTE", 0));
            json.put("date", intent.getStringExtra("ALARM_DATE"));
            json.put("description", intent.getStringExtra("ALARM_DESCRIPTION"));
            json.put("sound", intent.getStringExtra("ALARM_SOUND"));
            json.put("vibration", intent.getBooleanExtra("ALARM_VIBRATION", true));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}