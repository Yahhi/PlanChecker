package ru.na_uglu.planchecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.na_uglu.planchecker.R.id.time;

class LocalData {
    private SQLiteDatabase db;

    LocalData(Context context, boolean needToWrite) {
        DBHelper dbHelper = new DBHelper(context);
        if (needToWrite) {
            db = dbHelper.getWritableDatabase();
        } else {
            db = dbHelper.getReadableDatabase();
        }
    }

    ArrayList<Project> getProjects() {
        ArrayList<Project> projects = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM projects WHERE done = 0", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            projects.add(getProjectFromCursor(cursor, false));
            cursor.moveToNext();
        }
        cursor.close();
        return projects;
    }

    private Project getProjectFromCursor(Cursor cursor, boolean withDoneTasks) {
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String comment = cursor.getString(cursor.getColumnIndex("comment"));
        Boolean done = cursor.getInt(cursor.getColumnIndex("done")) > 0;

        Cursor tasksCursor;
        if (!withDoneTasks) {
            tasksCursor = db.rawQuery("SELECT * FROM tasks WHERE done = 0 AND project_id = ?", new String[]{Integer.toString(id)});
        } else {
            tasksCursor = db.rawQuery("SELECT * FROM tasks WHERE project_id = ?", new String[]{Integer.toString(id)});
        }
        tasksCursor.moveToFirst();
        ArrayList<Task> tasks = new ArrayList<>();
        while (!tasksCursor.isAfterLast()) {
            tasks.add(getTaskFromCursor(tasksCursor));
            tasksCursor.moveToNext();
        }
        tasksCursor.close();

        return new Project(id, title, comment, tasks, done);
    }

    private Task getTaskFromCursor(Cursor cursor) {
        int taskId = cursor.getInt(cursor.getColumnIndex("id"));
        String taskTitle = cursor.getString(cursor.getColumnIndex("title"));
        int taskEstimatedTime = cursor.getInt(cursor.getColumnIndex("estimated_time"));
        int taskRealTime = cursor.getInt(cursor.getColumnIndex("real_time"));
        String taskCreated = cursor.getString(cursor.getColumnIndex("when_created"));
        String taskDone = cursor.getString(cursor.getColumnIndex("when_done"));
        String comment = cursor.getString(cursor.getColumnIndex("comment"));
        return new Task(taskId, taskTitle, comment, taskEstimatedTime, taskRealTime, taskCreated, taskDone);
    }

    void saveProject(int id, String title, String comment) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("comment", comment);
        if (id == 0) {
            db.insert("projects", "", values);
        } else {
            db.update("projects", values, "id = ?", new String[]{Integer.toString(id)});
        }
    }

    Project getProject(int projectId) {
        return getProject(projectId, true);
    }

    Project getProject(int projectId, boolean withDoneTasks) {
        Cursor projectCursor = db.rawQuery("SELECT * FROM projects WHERE id = ?",
                new String[]{Integer.toString(projectId)});
        projectCursor.moveToFirst();
        Project project = getProjectFromCursor(projectCursor, withDoneTasks);
        projectCursor.close();
        return project;
    }

    void closeDataConnection() {
        db.close();
    }

    Task getTask(int taskId) {
        Cursor cursor = db.rawQuery("SELECT * FROM tasks WHERE id = ?",
                new String[]{Integer.toString(taskId)});
        cursor.moveToFirst();
        Task task;
        if (cursor.getCount() > 0) {
            task = getTaskFromCursor(cursor);
        } else {
            task = new Task();
        }
        cursor.close();
        return task;
    }

    String getProjectTitleForTask(int taskId) {
        String title = "";
        Cursor cursor = db.rawQuery("SELECT project_id FROM tasks WHERE id = ?",
                new String[]{Integer.toString(taskId)});
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            int projectId = cursor.getInt(cursor.getColumnIndex("project_id"));
            Cursor projectCursor = db.rawQuery("SELECT title FROM projects WHERE id = ?",
                    new String[]{Integer.toString(projectId)});
            if (projectCursor.getCount() > 0) {
                projectCursor.moveToFirst();
                title = projectCursor.getString(projectCursor.getColumnIndex("title"));
            }
            projectCursor.close();
        }
        cursor.close();
        return title;
    }

    void makeTaskDone(int taskId) {
        ContentValues values = new ContentValues();
        values.put("done", 1);
        values.put("when_done", formatDate());
        db.update("tasks", values, "id = ?", new String[]{Integer.toString(taskId)});
    }

    void addTimeToTask(int taskId, int time) {
        if (time > 0) {
            ContentValues timeValues = new ContentValues();
            timeValues.put("task_id", taskId);
            timeValues.put("time", time);
            timeValues.put("when_added", formatDate());
            db.insert("time_intervals", "", timeValues);

            Cursor currentTimeCursor = db.rawQuery("SELECT real_time FROM tasks WHERE id = ?",
                    new String[]{Integer.toString(taskId)});
            currentTimeCursor.moveToFirst();
            int curentTaskTime = currentTimeCursor.getInt(currentTimeCursor.getColumnIndex("real_time"));
            currentTimeCursor.close();
            ContentValues updatedTimeValues = new ContentValues();
            updatedTimeValues.put("real_time", curentTaskTime + time);
            db.update("tasks", updatedTimeValues, "id = ?", new String[]{Integer.toString(taskId)});
        }
    }

    private String getNowFormatted() {
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        return currentDateTime;
    }

    static String formatDate() {
        Date date = new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
        String formatResult = format1.format(date) + "T" + format2.format(date) + "Z";
        return formatResult;
    }

    int[] getFiveLastTimeIntervals(int taskId) {
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals WHERE task_id = ? ORDER BY id DESC",
                new String[]{Integer.toString(taskId)});
        cursor.moveToFirst();
        int[] intervals = new int[cursor.getCount()];
        int i = 0;
        while (!cursor.isAfterLast()) {
            intervals[i++] = cursor.getInt(cursor.getColumnIndex("time"));
            cursor.moveToNext();
        }
        cursor.close();
        return intervals;
    }

    WhenhubEvent[] getAllTimeIntervals() {
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals ORDER BY id ASC", null);
        WhenhubEvent[] times = new WhenhubEvent[cursor.getCount()];
        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int taskId = cursor.getInt(cursor.getColumnIndex("task_id"));
            int time = cursor.getInt(cursor.getColumnIndex("time"));
            String whenAdded = cursor.getString(cursor.getColumnIndex("when_added"));
            Task task = getTask(taskId);
            times[i++] = new WhenhubEvent(
                    task.title + " (" + getProjectTitleForTask(taskId) + ")",
                    whenAdded,
                    time);
            cursor.moveToNext();
        }
        cursor.close();
        return times;
    }

    ArrayList<TimeInterval> getTimeIntervalsForTask(int taskId) {
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals WHERE task_id = ?",
                new String[]{Integer.toString(taskId)});
        ArrayList<TimeInterval> times = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            int time = cursor.getInt(cursor.getColumnIndex("time"));
            String whenAdded = cursor.getString(cursor.getColumnIndex("when_added"));
            times.add(new TimeInterval(id, getDateFromString(whenAdded), time));
            cursor.moveToNext();
        }
        cursor.close();
        return times;
    }

    private Date getDateFromString(String whenAdded) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            date = format.parse(whenAdded);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    void saveTask(int taskId, int projectId, String title, int estimatedTime, String comment) {
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("title", title);
        values.put("estimated_time", estimatedTime);
        values.put("comment", comment);
        if (taskId == 0) {
            values.put("when_created", formatDate());
            db.insert("tasks", "", values);
        } else {
            db.update("tasks", values, "id = ?", new String[]{Integer.toString(taskId)});
        }
    }

    int convertEnteredTimeToInt(String estimatedTime) {
        String[] hoursAndMinutes = estimatedTime.split(":");
        int timeInMinutes;
        try {
            if (hoursAndMinutes.length == 2) {
                timeInMinutes = Integer.parseInt(hoursAndMinutes[0]) * 60;
                timeInMinutes += Integer.parseInt(hoursAndMinutes[1]);
            } else {
                timeInMinutes = Integer.parseInt(hoursAndMinutes[0]);
            }
        } catch (NumberFormatException e) {
            timeInMinutes = 0;
        }
        return timeInMinutes;
    }

    String[] getProjectTitles() {
        Cursor cursor = db.rawQuery("SELECT title FROM projects WHERE done = 0 ORDER BY id ASC", null);
        String[] titles = new String[cursor.getCount()];
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            titles[i++] = cursor.getString(cursor.getColumnIndex("title"));
            cursor.moveToNext();
        }
        cursor.close();
        return titles;
    }

    int getProjectIdFromTitle(String selectedItem) {
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT id FROM projects WHERE title = ?", new String[]{selectedItem});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id;
    }

    int getTaskIdFromTitle(String selectedItem) {
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT id FROM tasks WHERE title = ?", new String[]{selectedItem});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id;
    }

    String[] getTaskTitles(String projectTitle) {
        int projectId = getProjectIdFromTitle(projectTitle);
        Cursor cursorTaskTitles = db.rawQuery(
                "SELECT title FROM tasks WHERE done = 0 AND project_id = ? ORDER BY id ASC",
                new String[]{Integer.toString(projectId)});
        String[] titles = new String[cursorTaskTitles.getCount()];
        cursorTaskTitles.moveToFirst();
        int i = 0;
        while (!cursorTaskTitles.isAfterLast()) {
            titles[i++] = cursorTaskTitles.getString(cursorTaskTitles.getColumnIndex("title"));
            cursorTaskTitles.moveToNext();
        }
        cursorTaskTitles.close();
        return titles;
    }

    void deleteProject(int projectId) {
        db.delete("tasks", "project_id = ?", new String[]{Integer.toString(projectId)});
        db.delete("projects", "id = ?", new String[]{Integer.toString(projectId)});
    }

    void makeProjectDone(int projectId) {
        ContentValues values = new ContentValues();
        values.put("done", 1);

        db.update("projects", values, "id = ?", new String[]{Integer.toString(projectId)});
    }

    void deleteTask(int id) {
        db.delete("tasks", "id = ?", new String[]{Integer.toString(id)});
    }

    WhenhubEvent[] getAllAccuracyRates() {
        Cursor cursor = db.rawQuery("SELECT * FROM tasks WHERE done > 0", null);
        WhenhubEvent[] accuracyResults = new WhenhubEvent[cursor.getCount()];
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title")) +
                    " (" + getProjectTitleForTask(id) + ")";
            Integer realTime = cursor.getInt(cursor.getColumnIndex("real_time"));
            Integer estimatedTime = cursor.getInt(cursor.getColumnIndex("estimated_time"));
            Integer customField = NetworkSync.getAccuracyRate(realTime, estimatedTime);
            String whenDone = cursor.getString(cursor.getColumnIndex("when_done"));
            accuracyResults[i++] = new WhenhubEvent(title, whenDone, customField);
            cursor.moveToNext();
        }
        cursor.close();
        return accuracyResults;
    }

    ArrayList<Task> getTasksForProject(int projectId, boolean done) {
        Cursor cursor;
        if (done) {
            cursor = db.rawQuery("SELECT * FROM tasks WHERE done > 0 AND project_id = ?",
                    new String[]{Integer.toString(projectId)});
        } else {
            cursor = db.rawQuery("SELECT * FROM tasks WHERE done = 0 AND project_id = ?",
                    new String[]{Integer.toString(projectId)});
        }
        cursor.moveToFirst();
        ArrayList<Task> tasks = new ArrayList<>(cursor.getCount());
        while (!cursor.isAfterLast()) {
            tasks.add(getTaskFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return tasks;
    }

    int getAverageTime(int projectId) {
        Cursor cursor = db.rawQuery(
                "SELECT time, when_added FROM time_intervals WHERE task_id IN (SELECT id FROM tasks WHERE project_id = ?) ORDER BY when_added ASC",
                new String[]{Integer.toString(projectId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int averageTime = 0;
            int[] timeInDay = getTimeSumsForDay(cursor);
            for (int aTimeInDay : timeInDay) {
                averageTime += aTimeInDay;
            }
            cursor.close();
            return averageTime / timeInDay.length;
        }
        cursor.close();
        return 0;
    }

    private int[] getTimeSumsForDay(Cursor cursor) {
        int[] sums = new int[cursor.getCount()];
        String date = cursor.getString(cursor.getColumnIndex("when_added")).substring(0, 10);
        int i = 0;
        while (!cursor.isAfterLast()) {
            String newDate = cursor.getString(cursor.getColumnIndex("when_added")).substring(0, 10);
            int time = cursor.getInt(cursor.getColumnIndex("time"));
            if (date.equals(newDate)) {
                sums[i] += time;
            } else {
                date = newDate;
                i++;
                sums[i] = time;
            }
            cursor.moveToNext();
        }
        int[] finalSums = new int[i];
        for (int j = 0; j < i; j++) {
            finalSums[j] = sums[j];
        }
        return finalSums;
    }

    void deleteTimeInterval(int id) {
        db.delete("time_intervals", "id = ?", new String[]{Integer.toString(id)});
    }

    ArrayList<TimeInterval> getTimeIntervals() {
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals ORDER BY when_added", null);
        cursor.moveToFirst();
        ArrayList<TimeInterval> allTimes = getTimeIntervalsFromCursor(cursor);
        cursor.close();
        return allTimes;
    }

    private ArrayList<TimeInterval> getTimeIntervalsFromCursor(Cursor cursor) {
        ArrayList<TimeInterval> timeIntervals = new ArrayList<>(cursor.getCount());
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            int taskId = cursor.getInt(cursor.getColumnIndex("task_id"));
            int time = cursor.getInt(cursor.getColumnIndex("time"));
            String whenAdded = cursor.getString(cursor.getColumnIndex("when_added"));
            timeIntervals.add(new TimeInterval(id, taskId, getDateFromString(whenAdded), time));
            cursor.moveToNext();
        }
        return timeIntervals;
    }
}
