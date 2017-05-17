package ru.na_uglu.planchecker;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class TimeInterval {
    int id;
    Date whenHappened;
    int time;
    private String timeWhenHappened;

    TimeInterval(int id, Date whenHappened, int time) {
        this.id = id;
        this.whenHappened = whenHappened;
        this.time = time;
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
