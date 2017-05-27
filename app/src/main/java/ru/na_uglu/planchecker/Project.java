package ru.na_uglu.planchecker;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class Project {
    int id;
    String title;
    String comment;
    ArrayList<Task> tasks;
    String lastModified;

    private int plannedTime;
    private int realTime;
    boolean done;

    public Project() {
        id = 0;
        title = "";
        comment = "";
        lastModified = "";
        plannedTime = 0;
        realTime = 0;
        tasks = new ArrayList<>();
    }

    Project(int id, String title, String comment, ArrayList<Task> tasks, boolean done, String lastModified) {
        this.id = id;
        this.title = title;
        this.comment = comment;
        this.lastModified = lastModified;
        this.tasks = tasks;
        plannedTime = countPlannedTime();
        realTime = countRealTime();
        this.done = done;
    }

    private int countRealTime() {
        int time = 0;
        for (Task task : tasks) {
            time += task.realTime;
        }
        return time;
    }

    private int countPlannedTime() {
        int time = 0;
        for (Task task : tasks) {
            time += task.plannedTime;
        }
        return time;
    }

    int getPlannedTime() {
        return plannedTime;
    }

    int getRealTime() {
        return realTime;
    }

    String getDateStarted(Context context) {
        String started = context.getString(R.string.still_no_tasks);
        if (tasks.size() > 0) {
            started = findMinimumWhenStarted();
        }
        return started;
    }

    private String findMinimumWhenStarted() {
        Date minDate = new Date();
        for (Task task: tasks) {
            if ((task.whenCreated != null) && task.whenCreated.before(minDate)) {
                minDate = task.whenCreated;
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return format.format(minDate);
    }

    String getDateCompleted(Context context) {
        String completed = context.getString(R.string.still_no_end_date);
        if (done) {
            completed = findMaximumWhenCompleted();
        }
        return completed;
    }

    private String findMaximumWhenCompleted() {
        Date maxDate = new Date(0);
        for (Task task: tasks) {
            if (task.whenCompleted.after(maxDate)) {
                maxDate = task.whenCompleted;
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return format.format(maxDate);
    }
}
