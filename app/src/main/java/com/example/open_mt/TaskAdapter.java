package com.example.open_mt;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private SharedPreferences prefs;
    private static final String PREF_KEY = "task_list";

    public TaskAdapter(List<Task> taskList, SharedPreferences prefs) {

        this.taskList = taskList;
        this.prefs = prefs;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {

        Task task = taskList.get(position);
        holder.taskText.setText(task.getName());

        if (task.getId() == highlightedTaskId) holder.itemView.setBackgroundResource(R.drawable.bg_task_highlighted);
        else holder.itemView.setBackgroundResource(R.drawable.bg_task_rounded);

        holder.taskCheck.setOnCheckedChangeListener(null);
        holder.taskCheck.setChecked(task.getDone());

        holder.taskCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            task.setDone(isChecked);
            saveTasks();
        });

        holder.taskText.setOnClickListener(v -> showEditDialog(v, task));
    }

    private void saveTasks() {

        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString(PREF_KEY, json);
        editor.apply();
    }

    private void showEditDialog(View view, Task task) {

        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog, null);

        EditText input = dialogView.findViewById(R.id.editTask);
        ImageButton cancelBtn = dialogView.findViewById(R.id.cancel_BTN);
        ImageButton saveBtn = dialogView.findViewById(R.id.apply_BTN);
        CheckBox nfCheck = dialogView.findViewById(R.id.nfCheck);
        View dateTimeContainer = dialogView.findViewById(R.id.dateTimeContainer);
        EditText editDate = dialogView.findViewById(R.id.date_time_Txt);

        input.setMaxLines(9);
        editDate.setText(task.getDateTime());
        nfCheck.setChecked(task.isNotify());
        dateTimeContainer.setVisibility(task.isNotify() ? View.VISIBLE : View.GONE);
        nfCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            dateTimeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        editDate.setOnClickListener(v -> {

            View dialogViewq = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_date_time_picker, null);

            android.widget.DatePicker datePicker = dialogViewq.findViewById(R.id.datePicker);
            TimePicker timePicker = dialogViewq.findViewById(R.id.timePicker);
            ImageButton apply = dialogViewq.findViewById(R.id.apply_BTN);
            ImageButton cancel = dialogViewq.findViewById(R.id.cancel_BTN);

            Calendar calendar = Calendar.getInstance();
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));

            AlertDialog dialog = new AlertDialog.Builder(view.getContext(), R.style.CustomDialogStyle).setView(dialogViewq).create();

            apply.setOnClickListener(view1 -> {

                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                String selectedDateTime = String.format(Locale.getDefault(), "%02d.%02d.%d %02d:%02d", dayOfMonth, month + 1, year, hour, minute);
                editDate.setText(selectedDateTime);
                task.setDateTime(selectedDateTime);
                task.setNotify(nfCheck.isChecked());
                if (nfCheck.isChecked()) scheduleNotification(view.getContext(), task);

                dialog.dismiss();
            });

            cancel.setOnClickListener(view1 -> dialog.dismiss());
            dialog.show();
        });

        if ("Новая задача".equals(task.getName())) input.setText("");
        else {

            input.setText(task.getName());
            input.setSelection(input.getText().length());
        }

        AlertDialog dialog = new AlertDialog.Builder(view.getContext(), R.style.CustomDialogStyle).setView(dialogView).create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        saveBtn.setOnClickListener(v -> {

            task.setName(input.getText().toString().trim());
            saveTasks();
            notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.setOnShowListener(d -> {

            input.requestFocus();
            input.postDelayed(() -> {

                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }, 100);
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {

        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskText;
        CheckBox taskCheck;

        public TaskViewHolder(View itemView) {

            super(itemView);
            taskText = itemView.findViewById(R.id.taskTxt);
            taskCheck = itemView.findViewById(R.id.taskCheck);
        }
    }

    private int highlightedTaskId = -1;

    public void setHighlightedTaskId(int taskId) {

        this.highlightedTaskId = taskId;
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification(Context context, Task task) {

        Intent intent = new Intent(context, NotificationReceiver.class);

        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_name", task.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(

                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String[] parts = task.getDateTime().split("[ .:]");
        if (parts.length != 5) return;

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int year = Integer.parseInt(parts[2]);
        int hour = Integer.parseInt(parts[3]);
        int minute = Integer.parseInt(parts[4]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
