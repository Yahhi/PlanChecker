package ru.na_uglu.planchecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

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
            int taskId = tasksCursor.getInt(tasksCursor.getColumnIndex("id"));
            String taskTitle = tasksCursor.getString(tasksCursor.getColumnIndex("title"));
            int taskEstimatedTime = tasksCursor.getInt(tasksCursor.getColumnIndex("estimated_time"));
            int taskRealTime = tasksCursor.getInt(tasksCursor.getColumnIndex("real_time"));
            boolean taskDone = tasksCursor.getInt(tasksCursor.getColumnIndex("done")) > 0;
            tasks.add(new Task(taskId, taskTitle, taskEstimatedTime, taskRealTime, taskDone));
            tasksCursor.moveToNext();
        }
        tasksCursor.close();

        return new Project(id, title, comment, tasks);
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
        Cursor projectCursor = db.rawQuery("SELECT * FROM projects WHERE id = ?", new String[]{Integer.toString(projectId)});
        projectCursor.moveToFirst();
        Project project = getProjectFromCursor(projectCursor);
        projectCursor.close();
        return project;
    }

    void closeDataConnection() {
        db.close();
    }
}
