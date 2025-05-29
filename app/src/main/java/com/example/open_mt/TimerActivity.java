package com.example.open_mt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    private TextView timerTextView;
    private ImageButton startStopButton;
    private boolean isRunning = false;
    private long startTime = 0;
    private long elapsedTime = 0;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerTextView = findViewById(R.id.timerTxt);
        startStopButton = findViewById(R.id.ss_BTN);

        startStopButton.setOnClickListener(v -> {

            if (isRunning) stopTimer();
            else startTimer();
        });

        ImageButton timerButton = findViewById(R.id.task_BTN);
        timerButton.setOnClickListener(v -> {

            Intent intent = new Intent(TimerActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        ImageButton infoButton = findViewById(R.id.inf_BTN);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(TimerActivity.this, InfActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void startTimer() {

        isRunning = true;
        startTime = System.currentTimeMillis() - elapsedTime;
        handler.postDelayed(updateTimerRunnable, 0);
        startStopButton.setImageResource(R.drawable.stop);
    }

    private void stopTimer() {

        isRunning = false;
        elapsedTime = System.currentTimeMillis() - startTime;
        handler.removeCallbacks(updateTimerRunnable);
        startStopButton.setImageResource(R.drawable.start);
    }

    private final Runnable updateTimerRunnable = new Runnable() {

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {

            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - startTime;

            int milliseconds = (int) (timeElapsed % 1000);
            int seconds = (int) (timeElapsed / 1000) % 60;
            int minutes = (int) (timeElapsed / (1000 * 60)) % 60;
            int hours = (int) (timeElapsed / (1000 * 60 * 60));

            timerTextView.setText(String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds));
            handler.postDelayed(this, 0);
        }
    };
}
