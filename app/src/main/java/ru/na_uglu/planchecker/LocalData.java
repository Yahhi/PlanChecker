package ru.na_uglu.planchecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
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
        return getProjects(false);
    }

    ArrayList<Project> getProjects(boolean includingDone) {
        ArrayList<Project> projects = new ArrayList<>();
        String selectString = "SELECT * FROM projects";
        if (!includingDone) {
            selectString += " WHERE done = 0";
        }
        Cursor cursor = db.rawQuery(selectString, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            projects.add(getProjectFromCursor(cursor, includingDone));
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
        String lastModified = cursor.getString(cursor.getColumnIndex("last_modified"));

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

        return new Project(id, title, comment, tasks, done, lastModified);
    }

    private Task getTaskFromCursor(Cursor cursor) {
        int taskId = cursor.getInt(cursor.getColumnIndex("id"));
        String taskTitle = cursor.getString(cursor.getColumnIndex("title"));
        int taskEstimatedTime = cursor.getInt(cursor.getColumnIndex("estimated_time"));
        int taskRealTime = cursor.getInt(cursor.getColumnIndex("real_time"));
        String taskCreated = cursor.getString(cursor.getColumnIndex("when_created"));
        String taskDone = cursor.getString(cursor.getColumnIndex("when_done"));
        String comment = cursor.getString(cursor.getColumnIndex("comment"));
        String lastModified = cursor.getString(cursor.getColumnIndex("last_modified"));
        return new Task(taskId, taskTitle, comment, taskEstimatedTime, taskRealTime, taskCreated, taskDone, lastModified);
    }

    void saveProject(int id, String title, String comment) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("comment", comment);
        values.put("last_modified", DateTimeFormater.formatDate());
        if (id == 0) {
            db.insert("projects", "", values);
        } else {
            db.update("projects", values, "id = ?", new String[]{Integer.toString(id)});
        }
    }

    void saveProject(int id, String title, String comment, boolean done, String lastModified) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("comment", comment);
        int doneInt;
        if (done) {
            doneInt = 1;
        } else {
            doneInt = 0;
        }
        values.put("done", doneInt);
        values.put("last_modified", lastModified);
        if (isDBEntryExist("projects", id)) {
            db.update("projects", values, "id = ?", new String[]{Integer.toString(id)});
        } else {
            values.put("id", id);
            db.insert("projects", "", values);
        }
    }

    private boolean isDBEntryExist(String tableName, int id) {
        String sqlSelect = "SELECT * FROM " + tableName + " WHERE id = ?";
        Cursor cursor = db.rawQuery(sqlSelect, new String[]{String.valueOf(id)});
        Boolean exists = false;
        if (cursor.getCount() > 0) {
            exists = true;
        }
        cursor.close();
        return exists;
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
        values.put("when_done", DateTimeFormater.formatDate());
        values.put("last_modified", DateTimeFormater.formatDate());
        db.update("tasks", values, "id = ?", new String[]{Integer.toString(taskId)});
    }

    void addTimeToTask(int taskId, int time) {
        if (time > 0) {
            ContentValues timeValues = new ContentValues();
            timeValues.put("task_id", taskId);
            timeValues.put("time", time);
            timeValues.put("when_added", DateTimeFormater.formatDate());
            db.insert("time_intervals", "", timeValues);

            Cursor currentTimeCursor = db.rawQuery("SELECT real_time FROM tasks WHERE id = ?",
                    new String[]{Integer.toString(taskId)});
            currentTimeCursor.moveToFirst();
            int curentTaskTime = currentTimeCursor.getInt(currentTimeCursor.getColumnIndex("real_time"));
            currentTimeCursor.close();
            ContentValues updatedTimeValues = new ContentValues();
            updatedTimeValues.put("real_time", curentTaskTime + time);
            updatedTimeValues.put("last_modified", DateTimeFormater.formatDate());
            db.update("tasks", updatedTimeValues, "id = ?", new String[]{Integer.toString(taskId)});
        }
    }

    int[] getFiveLastTimeIntervals(int taskId) {
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals WHERE task_id = ? ORDER BY when_added DESC",
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
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals ORDER BY when_added ASC", null);
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
            int time = cursor.getInt(cursor.getColumnIndex("time"));
            String whenAdded = cursor.getString(cursor.getColumnIndex("when_added"));
            times.add(new TimeInterval(taskId, getDateFromString(whenAdded), time));
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
        values.put("last_modified", DateTimeFormater.formatDate());
        if (taskId == 0) {
            values.put("when_created", DateTimeFormater.formatDate());
            db.insert("tasks", "", values);
        } else {
            db.update("tasks", values, "id = ?", new String[]{Integer.toString(taskId)});
        }
    }

    private void saveTask(Task task, int projectId) {
        ContentValues values = new ContentValues();
        values.put("title", task.title);
        values.put("project_id", projectId);
        values.put("estimated_time", task.plannedTime);
        values.put("real_time", task.realTime);
        values.put("comment", task.comment);
        values.put("when_created", DateTimeFormater.formatDate(task.whenCreated));
        int intDone;
        if (task.whenCompleted.equals(new Date(0))) {
            intDone = 0;
        } else {
            intDone = 1;
        }
        values.put("done", intDone);
        values.put("when_done", DateTimeFormater.formatDate(task.whenCompleted));
        values.put("last_modified", task.lastModified);
        if (isDBEntryExist("tasks", task.id)) {
            db.update("tasks", values, "id = ?", new String[]{Integer.toString(task.id)});
        } else {
            values.put("id", task.id);
            db.insert("tasks", "", values);
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
        values.put("last_modified", DateTimeFormater.formatDate());
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
            Integer customField = WhenhubSync.getAccuracyRate(realTime, estimatedTime);
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
            if (timeInDay.length > 0) {
                return averageTime / timeInDay.length;
            } else {
                return 0;
            }
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

    void deleteTimeInterval(long id) {
        db.delete("time_intervals", "when_added = ?", new String[]{DateTimeFormater.formatDate(id)});
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
            int taskId = cursor.getInt(cursor.getColumnIndex("task_id"));
            int time = cursor.getInt(cursor.getColumnIndex("time"));
            String whenAdded = cursor.getString(cursor.getColumnIndex("when_added"));
            timeIntervals.add(new TimeInterval(taskId, getDateFromString(whenAdded), time));
            cursor.moveToNext();
        }
        return timeIntervals;
    }

    void uploadAllData() {
        Log.i("FIREBASE-sync", "upload started");
        ArrayList<Project> allProjects = getProjects(true);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = database.getReference(userId);

        for (Project projectToUpload: allProjects) {
            userReference.child("projects").child(String.valueOf(projectToUpload.id)).setValue(projectToUpload);
        }
        ArrayList<TimeInterval> allTimeIntervals = getTimeIntervals();
        for (TimeInterval timeToUpload: allTimeIntervals) {
            userReference.child("time_intervals").child(String.valueOf(timeToUpload.id)).setValue(timeToUpload);
        }
    }

    void downloadAllData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = database.getReference(userId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot projectsBase = dataSnapshot.child("projects");
                for (DataSnapshot oneProject : projectsBase.getChildren()) {
                    Project projectToStore = oneProject.getValue(Project.class);
                    saveFullRemoteProject(projectToStore);
                }
                DataSnapshot timeIntervalsBase = dataSnapshot.child("time_intervals");
                for (DataSnapshot oneInterval : timeIntervalsBase.getChildren()) {
                    TimeInterval timeIntervalToStore = oneInterval.getValue(TimeInterval.class);
                    saveRemoteTimeInterval(timeIntervalToStore);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void saveFullRemoteProject(Project projectToStore) {
        String projectLastModifiedLocal = getLastModified("projects", projectToStore.id);
        if (!isLocalDateOlder(projectLastModifiedLocal, projectToStore.lastModified)) {
            saveProject(projectToStore.id, projectToStore.title, projectToStore.comment, projectToStore.done, projectToStore.lastModified);
        }
        for (Task oneTask: projectToStore.tasks) {
            if (!isLocalDateOlder(getLastModified("tasks", oneTask.id), oneTask.lastModified)) {
                saveTask(oneTask, projectToStore.id);
            }
        }
    }

    private boolean isLocalDateOlder(String local, String remote) {
        if (local.compareTo(remote) >= 0) {
            return true;
        } else {
            return false;
        }

    }

    private String getLastModified(String tableName, int id) {
        String lastModified = "0000-00-00";
        String sqlSelect = "SELECT last_modified FROM " + tableName + " WHERE id = ?";
        Cursor cursor = db.rawQuery(sqlSelect,
                new String[]{String.valueOf(id)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lastModified = cursor.getString(cursor.getColumnIndex("last_modified"));
        }
        cursor.close();
        return lastModified;
    }

    void saveRemoteTimeInterval(TimeInterval timeIntervalToStore) {
        if (!timeIntervalExists(timeIntervalToStore.whenHappened)) {
            ContentValues timeValues = new ContentValues();
            timeValues.put("task_id", timeIntervalToStore.taskId);
            timeValues.put("time", timeIntervalToStore.time);
            timeValues.put("when_added", DateTimeFormater.formatDate(timeIntervalToStore.whenHappened));
            db.insert("time_intervals", "", timeValues);
        }
    }

    private boolean timeIntervalExists(Date whenHappened) {
        Cursor cursor = db.rawQuery("SELECT * FROM time_intervals WHERE when_added = ?",
                new String[]{DateTimeFormater.formatDate(whenHappened)});
        Boolean exists = false;
        if (cursor.getCount() > 0) {
            exists = true;
        }
        cursor.close();
        return exists;
    }
}
