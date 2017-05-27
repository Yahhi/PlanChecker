package ru.na_uglu.planchecker;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by User on 28.05.2017.
 */

class DateTimeFormater {

    static String formatDate() {
        return formatDate(0);
    }

    static String formatDate(long dateLong) {
        Date date;
        if (dateLong == 0) {
            date = new Date();
        } else {
            date = new Date(dateLong);
        }
        return formatDate(date);
    }

    static String formatDate(Date date) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
        String formatResult = format1.format(date) + "T" + format2.format(date) + "Z";
        return formatResult;
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
}
