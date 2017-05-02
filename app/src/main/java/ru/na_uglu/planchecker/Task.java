package ru.na_uglu.planchecker;


class Task {
    int id;
    String title;
    int plannedTime;
    int realTime;
    boolean done = false;

    public Task() {
        id = 0;
        title = "";
        plannedTime = 0;
        realTime = 0;
    }

    Task(int id, String title, int estimatedTime, int realTime, boolean done) {
        this.id = id;
        this.title = title;
        plannedTime = estimatedTime;
        this.realTime = realTime;
        this.done = done;
    }
}
