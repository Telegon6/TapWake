package com.chessytrooper.tapwake;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.graphics.Color;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private EditText dateEditText;
    private EditText descriptionEditText;
    private EditText editTextText;
    private Button addAlarmButton;
    private TextView wordCountTextView;
    private Calendar calendar;
    private static final int MAX_WORDS = 100;
    private static final int RINGTONE_PICKER_REQUEST_CODE = 1;
    private Uri selectedAlarmSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set default alarm sound
        selectedAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        timePicker = findViewById(R.id.timePicker);
        dateEditText = findViewById(R.id.editTextDate);
        descriptionEditText = findViewById(R.id.editTextDescription);
        editTextText = findViewById(R.id.editTextText);
        addAlarmButton = findViewById(R.id.button);
        wordCountTextView = findViewById(R.id.textView3);

        calendar = Calendar.getInstance();

        addAlarmButton.setBackgroundColor(Color.parseColor("#808080"));

        setupDatePicker();
        setupDescriptionEditText();
        updateAddAlarmButtonState();

        addAlarmButton.setOnClickListener(v -> addAlarm());
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateEditText();
        };

        dateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(AddAlarmActivity.this, dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void updateDateEditText() {
        String dateFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        dateEditText.setText(sdf.format(calendar.getTime()));
        updateAddAlarmButtonState();
    }

    private void setupDescriptionEditText() {
        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int wordCount = countWords(s.toString());
                updateWordCount(wordCount);
                if (wordCount > MAX_WORDS) {
                    String trimmedText = trimToWordLimit(s.toString(), MAX_WORDS);
                    descriptionEditText.setText(trimmedText);
                    descriptionEditText.setSelection(trimmedText.length());
                }
                updateAddAlarmButtonState();
            }
        });
    }

    private int countWords(String s) {
        return s.trim().split("\\s+").length;
    }

    private String trimToWordLimit(String s, int wordLimit) {
        String[] words = s.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, wordLimit); i++) {
            builder.append(words[i]).append(" ");
        }
        return builder.toString().trim();
    }

    private void updateWordCount(int count) {
        wordCountTextView.setText(String.format(Locale.getDefault(),
                "Words: %d/%d", count, MAX_WORDS));
    }

    private void updateAddAlarmButtonState() {
        boolean isNameEdit = !editTextText.getText().toString().isEmpty();
        boolean isDateSelected = !dateEditText.getText().toString().isEmpty();
        boolean isDescriptionEntered = !descriptionEditText.getText().toString().isEmpty();

        addAlarmButton.setEnabled(isDateSelected && isDescriptionEntered && isNameEdit);

        if (isDateSelected && isDescriptionEntered) {
            addAlarmButton.setBackgroundColor(Color.parseColor("#0876E5"));
        }
    }

    private void openRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedAlarmSound);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedAlarmSound = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        }
    }

    private void addAlarm() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String date = dateEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        Calendar current = Calendar.getInstance();
        Calendar selected = (Calendar) calendar.clone();
        selected.set(Calendar.HOUR_OF_DAY, hour);
        selected.set(Calendar.MINUTE, minute);

        if (selected.before(current)) {
            Toast.makeText(this, "Cannot set an alarm in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, NfcWriteActivity.class);
        intent.putExtra("ALARM_HOUR", hour);
        intent.putExtra("ALARM_MINUTE", minute);
        intent.putExtra("ALARM_DATE", date);
        intent.putExtra("ALARM_DESCRIPTION", description);
        intent.putExtra("ALARM_SOUND", selectedAlarmSound.toString());
        intent.putExtra("ALARM_DURATION", 5 * 60 * 1000); // 5 minutes in milliseconds

        startActivity(intent);
    }
}