package com.chessytrooper.tapwake;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
    private Button addAlarmButton;
    private TextView wordCountTextView;
    private Calendar calendar;
    private static final int MAX_WORDS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);

        timePicker = findViewById(R.id.timePicker);
        dateEditText = findViewById(R.id.editTextDate);
        descriptionEditText = findViewById(R.id.editTextDescription);
        addAlarmButton = findViewById(R.id.button);
        wordCountTextView = findViewById(R.id.textView3);

        calendar = Calendar.getInstance();

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
            new DatePickerDialog(AddAlarmActivity.this, dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
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
        boolean isDateSelected = !dateEditText.getText().toString().isEmpty();
        boolean isDescriptionEntered = !descriptionEditText.getText().toString().isEmpty();
        addAlarmButton.setEnabled(isDateSelected && isDescriptionEntered);
    }

    private void addAlarm() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String date = dateEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        // Create an Intent to start the NfcWriteActivity
        Intent intent = new Intent(this, NfcWriteActivity.class);
        intent.putExtra("ALARM_HOUR", hour);
        intent.putExtra("ALARM_MINUTE", minute);
        intent.putExtra("ALARM_DATE", date);
        intent.putExtra("ALARM_DESCRIPTION", description);
        // Add default values for sound and vibration
        intent.putExtra("ALARM_SOUND", "default_sound");
        intent.putExtra("ALARM_VIBRATION", true);

        startActivity(intent);
    }

//    private void addAlarm() {
//        int hour = timePicker.getHour();
//        int minute = timePicker.getMinute();
//        String date = dateEditText.getText().toString();
//        String description = descriptionEditText.getText().toString();
//
//        // TODO: Implement alarm creation logic here
//        // For now, we'll just show a toast message
//        String alarmInfo = String.format("Alarm set for %02d:%02d on %s\nDescription: %s",
//                hour, minute, date, description);
//        Toast.makeText(this, alarmInfo, Toast.LENGTH_LONG).show();
//    }
}