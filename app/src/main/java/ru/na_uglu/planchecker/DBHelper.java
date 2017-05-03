package ru.na_uglu.planchecker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "projectChecker.db";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `projects` (\n" +
                " `id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " `title` TEXT NOT NULL,\n" +
                " `comment` TEXT);");
        insertProjectData(db);

        db.execSQL("CREATE TABLE `tasks` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`title` TEXT, " +
                "`project_id` INTEGER NOT NULL, " +
                "`estimated_time` INTEGER, " +
                "`real_time` INTEGER, " +
                "`pomodoros` INTEGER, " +
                "`comment` TEXT, " +
                "`done` INTEGER DEFAULT 0 )");
        insertTasksData(db);

        db.execSQL("CREATE TABLE `time_intervals` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`task_id` INTEGER NOT NULL, " +
                "`time` INTEGER NOT NULL, " +
                "`when_added` TEXT NOT NULL )");
    }

    private void insertProjectData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO `projects` VALUES (1,'Cooking App','');");
        db.execSQL("INSERT INTO `projects` VALUES (2,'WhenHub App','');");
    }

    private void insertTasksData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO `tasks` VALUES (1,'Create recipes list',1,50,0,2,'',0);");
        db.execSQL("INSERT INTO `tasks` VALUES (2,'Fill recipes list',1,100,0,4,'',0);");
        db.execSQL("INSERT INTO `tasks` VALUES (3,'Create landscape xmls',1,25,0,1,'',0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS projects");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }
}
