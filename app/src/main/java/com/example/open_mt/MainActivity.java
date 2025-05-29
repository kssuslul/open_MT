package com.example.open_mt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Task> taskList;
    private TaskAdapter adapter;
    private SharedPreferences prefs;
    private static final String PREF_KEY = "task_list";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("MyTasks", MODE_PRIVATE);
        taskList = loadTasks();

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, prefs);
        recyclerView.setAdapter(adapter);

        ImageButton addTaskButton = findViewById(R.id.buttonAddNotification);
        addTaskButton.setOnClickListener(v -> {

            taskList.add(new Task("Новая задача", false));
            adapter.notifyItemInserted(taskList.size() - 1);
            saveTasks();
        });

        ImageButton timerButton = findViewById(R.id.timer_BTN);
        timerButton.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, TimerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        ImageButton infoButton = findViewById(R.id.inf_BTN);
        infoButton.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, InfActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        ImageButton delDoneBtn = findViewById(R.id.del_BTN);
        delDoneBtn.setOnClickListener(this::showConditionsDialog);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("task_id")) {

            int taskId = intent.getIntExtra("task_id", -1);
            if (taskId != -1) highlightTask(taskId);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void highlightTask(int taskId) {

        for (int i = 0; i < taskList.size(); i++) {

            Task task = taskList.get(i);
            if (task.getId() == taskId) {

                recyclerView.scrollToPosition(i);
                adapter.setHighlightedTaskId(taskId);
                adapter.notifyDataSetChanged();

                new Handler().postDelayed(() -> {

                    adapter.setHighlightedTaskId(-1);
                    adapter.notifyDataSetChanged();
                }, 2000);
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showConditionsDialog(View view) {

        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog, null);

        View nfCheckContainer = (View) dialogView.findViewById(R.id.nfCheck).getParent();
        if (nfCheckContainer != null) {
            nfCheckContainer.setVisibility(View.GONE);
        }

        TextView conditionsText = dialogView.findViewById(R.id.editTask);
        conditionsText.setText("Удалить выполненные задачи?");
        conditionsText.setFocusable(false);
        conditionsText.setClickable(false);

        ImageButton cancelBtn = dialogView.findViewById(R.id.cancel_BTN);
        ImageButton applyBtn = dialogView.findViewById(R.id.apply_BTN);
        AlertDialog dialog = new AlertDialog.Builder(view.getContext(), R.style.CustomDialogStyle).setView(dialogView).create();
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        applyBtn.setOnClickListener(v -> {

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            for (Task task : taskList) {

                if (task.getDone() && task.isNotify()) {

                    Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(

                            getApplicationContext(),
                            task.getId(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    alarmManager.cancel(pendingIntent);
                }
            }

            List<Task> toRemove = new ArrayList<>();
            for (Task task : taskList) {

                if (task.getDone()) toRemove.add(task);
            }

            taskList.removeAll(toRemove);
            adapter.notifyDataSetChanged();
            saveTasks();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveTasks() {

        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString(PREF_KEY, json);
        editor.apply();
    }

    private ArrayList<Task> loadTasks() {

        Gson gson = new Gson();
        String json = prefs.getString(PREF_KEY, null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }
}