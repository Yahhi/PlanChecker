package ru.na_uglu.planchecker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
            projects.add(getProjectFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return projects;
    }

    private Project getProjectFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String comment = cursor.getString(cursor.getColumnIndex("comment"));

        Cursor tasksCursor = db.rawQuery("SELECT * FROM tasks WHERE done = 0 AND project_id = ?", new String[]{Integer.toString(id)});
        tasksCursor.moveToFirst();
        ArrayList<Task> tasks = new ArrayList<>();
        while (!tasksCursor.isAfterLast()) {
            tasks.add(getTaskFromCursor(tasksCursor));
            tasksCursor.moveToNext();
        }
        tasksCursor.close();

        return new Project(id, title, comment, tasks);
    }

    private Task getTaskFromCursor(Cursor cursor) {
        int taskId = cursor.getInt(cursor.getColumnIndex("id"));
        String taskTitle = cursor.getString(cursor.getColumnIndex("title"));
        int taskEstimatedTime = cursor.getInt(cursor.getColumnIndex("estimated_time"));
        int taskRealTime = cursor.getInt(cursor.getColumnIndex("real_time"));
        boolean taskDone = cursor.getInt(cursor.getColumnIndex("done")) > 0;
        String comment = cursor.getString(cursor.getColumnIndex("comment"));
        return new Task(taskId, taskTitle, comment, taskEstimatedTime, taskRealTime, taskDone);
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
        Cursor projectCursor = db.rawQuery("SELECT * FROM projects WHERE id = ?",
                new String[]{Integer.toString(projectId)});
        projectCursor.moveToFirst();
        Project project = getProjectFromCursor(projectCursor);
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
        db.update("tasks", values, "id = ?", new String[]{Integer.toString(taskId)});
    }

    void addTimeToTask(int taskId, int time) {
        ContentValues timeValues = new ContentValues();
        timeValues.put("task_id", taskId);
        timeValues.put("time", time);
        timeValues.put("when_added", getNowFormatted());
        db.insert("time_intervals", "", timeValues);

        Cursor currentTimeCursor = db.rawQuery("SELECT real_time FROM tasks WHERE id = ?",
                new String[]{Integer.toString(taskId)});
        currentTimeCursor.moveToFirst();
        int curentTaskTime = currentTimeCursor.getInt(currentTimeCursor.getColumnIndex("real_time"));
        currentTimeCursor.close();
        ContentValues updatedTimeValues = new ContentValues();
        updatedTimeValues.put("real_time", curentTaskTime + time);
        Log.i("TIME", "was " + curentTaskTime);
        Log.i("TIME", "added " + time);
        db.update("tasks", updatedTimeValues, "id = ?", new String[]{Integer.toString(taskId)});
    }

    private String getNowFormatted() {
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        Log.i("TIME", "at " + currentDateTime);
        return currentDateTime;
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

    void saveTask(int taskId, int projectId, String title, int estimatedTime, String comment) {
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("title", title);
        values.put("estimated_time", estimatedTime);
        values.put("comment", comment);
        if (taskId == 0) {
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
}
