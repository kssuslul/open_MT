package com.example.open_mt;

public class Task {

    private int id;
    private String name;
    private boolean done;
    private String dateTime;
    private boolean notify;

    public Task(String name, boolean done) {

        this.id = (int) System.currentTimeMillis();
        this.name = name;
        this.done = done;
        this.dateTime = "";
        this.notify = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
