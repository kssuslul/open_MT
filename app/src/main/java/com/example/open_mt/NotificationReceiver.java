package com.example.open_mt;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_channel";

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {

        int taskId = intent.getIntExtra("task_id", -1);
        if (taskId == -1) return;

        SharedPreferences prefs = context.getSharedPreferences("MyTasks", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("task_list", null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        ArrayList<Task> taskList = gson.fromJson(json, type);
        if (taskList == null) return;

        Task currentTask = null;
        for (Task task : taskList) {

            if (task.getId() == taskId) {

                currentTask = task;
                break;
            }
        }
        if (currentTask == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(

                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("task_id", taskId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(

                context,
                taskId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)

                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("Напоминание")
                .setContentText(currentTask.getName() != null ? currentTask.getName() : "Задача")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(taskId, builder.build());

        currentTask.setDateTime("");
        currentTask.setNotify(false);

        prefs.edit().putString("task_list", gson.toJson(taskList)).apply();
    }
}
