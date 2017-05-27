package ru.na_uglu.planchecker;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class TimeInterval {
    long id;
    Date whenHappened;
    int time;
    int taskId = 0;

    TimeInterval() {
    }

    TimeInterval(Date whenHappened, int time) {
        this.id = whenHappened.getTime();
        this.whenHappened = whenHappened;
        this.time = time;
    }

    TimeInterval(int taskId, Date whenHappened, int time) {
        this.id = whenHappened.getTime();
        this.whenHappened = whenHappened;
        this.time = time;
        this.taskId = taskId;
    }

    String getDateWhenHappened() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(whenHappened);
    }

    String getTimeWhenHappened() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(whenHappened);
    }
}
