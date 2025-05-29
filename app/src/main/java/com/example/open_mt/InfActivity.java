package com.example.open_mt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class InfActivity extends AppCompatActivity {

    private ImageButton timerButton;
    private ImageButton mainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inf);

        timerButton = findViewById(R.id.timer_BTN);
        mainButton = findViewById(R.id.task_BTN);

        timerButton.setOnClickListener(v -> {

            Intent intent = new Intent(InfActivity.this, TimerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        mainButton.setOnClickListener(v -> {

            Intent intent = new Intent(InfActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }
}
