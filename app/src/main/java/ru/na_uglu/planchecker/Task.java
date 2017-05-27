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
    String lastModified;

    Date whenCreated;
    Date whenCompleted;

    Task() {
        id = 0;
        title = "";
        comment = "";
        plannedTime = 0;
        realTime = 0;
        lastModified = "";
    }

    Task(int id, String title, String comment, int estimatedTime, int realTime, String createdDate, String completedDate, String lastModified) {
        this.id = id;
        this.title = title;
        this.comment = comment;
        plannedTime = estimatedTime;
        this.realTime = realTime;
        this.lastModified = lastModified;
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

}
