package com.chessytrooper.tapwake;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void openAddAlarm(View view){
        Intent i = new Intent(this, AddAlarmActivity.class);
        startActivity(i);
    }

    public void openReadAlarm(View view) {
        Intent i = new Intent(this, ReadAlarmActivity.class);
        startActivity(i);
    }
}