package ru.na_uglu.planchecker;


class Task {
    int id;
    String title;
    String comment;
    int plannedTime;
    int realTime;
    boolean done = false;

    Task() {
        id = 0;
        title = "";
        comment = "";
        plannedTime = 0;
        realTime = 0;
    }

    static String formatTimeInHoursAndMinutes(int time) {
        int hours = (int) Math.ceil(time / 60);
        String hoursString;
        if (hours < 10) {
            hoursString = "0" + hours;
        } else {
            hoursString = Integer.toString(hours);
        }
        int minutes = time % 60;
        String minutesString;
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = Integer.toString(minutes);
        }
        return hoursString + ":" + minutesString;
    }

    Task(int id, String title, String comment, int estimatedTime, int realTime, boolean done) {
        this.id = id;
        this.title = title;
        this.comment = comment;
        plannedTime = estimatedTime;
        this.realTime = realTime;
        this.done = done;
    }

    public String getPlannedTime() {
        return Integer.toString(plannedTime);
    }

    public String getRealTime() {
        return Integer.toString(realTime);
    }
}
