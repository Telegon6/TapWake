package com.chessytrooper.tapwake;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReadAlarmActivity extends AppCompatActivity {

    private static final String TAG = "ReadAlarmActivity";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private TextView actionText;
    private ImageView scanningCircle;
    private ImageView scanningBackground;
    private Button actionButton;
    private AnimatedVectorDrawable expandingCircleAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_alarm);

        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        setupNfc();
    }

    private void initViews() {
        scanningCircle = findViewById(R.id.scanningCircle);
        scanningBackground = findViewById(R.id.scanningBackground);
        actionButton = findViewById(R.id.actionButton);
        actionText = findViewById(R.id.actionText);

        actionButton.setVisibility(View.GONE);

        expandingCircleAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.expanding_circle);
        scanningCircle.setImageDrawable(expandingCircleAnimation);
    }

    private void setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE);

        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefFilter.addDataType("application/json");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Failed to add MIME type.", e);
        }

        intentFiltersArray = new IntentFilter[]{ndefFilter,};
        techListsArray = new String[][]{new String[]{Ndef.class.getName()}};
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
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    Toast.makeText(this, "Tag is not NDEF.", Toast.LENGTH_SHORT).show();
                    return;
                }

                NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                if (ndefMessage != null) {
                    for (NdefRecord ndefRecord : ndefMessage.getRecords()) {
                        if (ndefRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA &&
                                ndefRecord.toMimeType().equals("application/json")) {
                            String payload = new String(ndefRecord.getPayload(), StandardCharsets.UTF_8);
                            showAlarmDetails(payload);
                            return;
                        }
                    }
                }
            }
        }
        Toast.makeText(this, "No valid NFC data found", Toast.LENGTH_SHORT).show();
    }

    private void showAlarmDetails(String alarmData) {
        Log.d(TAG, "Showing alarm details: " + alarmData);
        Intent intent = new Intent(this, AlarmDetailsActivity.class);
        intent.putExtra("ALARM_DATA", alarmData);
        startActivity(intent);
        finish(); // Finish ReadAlarmActivity after starting AlarmDetailsActivity
    }
}