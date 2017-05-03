package ru.na_uglu.planchecker;

import android.content.ContentValues;
import android.content.Context;
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

        Cursor cursor = db.rawQuery("SELECT * FROM projects", null);
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

        Cursor tasksCursor = db.rawQuery("SELECT * FROM tasks WHERE project_id = ?", new String[]{Integer.toString(id)});
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
        return new Task(taskId, taskTitle, taskEstimatedTime, taskRealTime, taskDone);
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
}
