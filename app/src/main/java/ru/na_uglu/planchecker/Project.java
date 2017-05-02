package ru.na_uglu.planchecker;

import java.util.ArrayList;
import java.util.Date;

class Project {
    int id;
    String title;
    String comment;
    ArrayList<Task> tasks;

    private int plannedTime;
    private int realTime;

    public Project() {
        id = 0;
        title = "";
        comment = "";
        plannedTime = 0;
        realTime = 0;
        tasks = new ArrayList<>();
    }

    Project(int id, String title, String comment, ArrayList<Task> tasks) {
        this.id = id;
        this.title = title;
        this.comment = comment;
        this.tasks = tasks;
        plannedTime = countPlannedTime();
        realTime = countRealTime();
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
}
