package ru.na_uglu.planchecker;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Task {
    int id;
    String title;
    String comment;
    int plannedTime;
    int realTime;

    Date whenCreated;
    Date whenCompleted;

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

    Task(int id, String title, String comment, int estimatedTime, int realTime, String createdDate, String completedDate) {
        this.id = id;
        this.title = title;
        this.comment = comment;
        plannedTime = estimatedTime;
        this.realTime = realTime;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            whenCreated = format.parse(createdDate);
            if ((completedDate != null) && (completedDate.length() > 0)) {
                whenCompleted = format.parse(completedDate);
            } else {
                whenCompleted = new Date(0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getPlannedTime() {
        return Integer.toString(plannedTime);
    }

    public String getRealTime() {
        return Integer.toString(realTime);
    }
}
