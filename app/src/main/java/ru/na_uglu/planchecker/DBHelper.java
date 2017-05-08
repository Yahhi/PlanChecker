package ru.na_uglu.planchecker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "projectChecker.db";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `projects` (\n" +
                " `id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " `title` TEXT NOT NULL,\n" +
                " `comment` TEXT,\n" +
                " `done` INTEGER DEFAULT 0);");
        insertProjectData(db);

        db.execSQL("CREATE TABLE `tasks` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`title` TEXT, " +
                "`project_id` INTEGER NOT NULL, " +
                "`estimated_time` INTEGER, " +
                "`real_time` INTEGER, " +
                "`comment` TEXT, " +
                "`when_created` TEXT, " +
                "`done` INTEGER DEFAULT 0, " +
                "`when_done` TEXT)");
        insertTasksData(db);

        db.execSQL("CREATE TABLE `time_intervals` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`task_id` INTEGER NOT NULL, " +
                "`time` INTEGER NOT NULL, " +
                "`when_added` TEXT NOT NULL )");
    }

    private void insertProjectData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO `projects` VALUES (1,'Foreign language mastery','What to do to make my Spanish better', 0);");
        db.execSQL("INSERT INTO `projects` VALUES (2,'Blogging','', 0);");
        db.execSQL("INSERT INTO `projects` VALUES (3,'My app project','', 0);");
    }

    private void insertTasksData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO `tasks` VALUES (1,'Read story in new book',1,300,0,'', '2017-01-05T12:00:00Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (2,'Look through grammar',1,60,0,'','2017-01-05T12:10:32Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (3,'Remember new words',1,120,0,'','2017-01-05T12:12:04Z',0, '');");

        db.execSQL("INSERT INTO `tasks` VALUES (4,'Choose the most popular theme on Quora',2,30,0,'Need to write about interesting themes','2017-01-05T12:17:08Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (5,'Find and read 10 articles on the theme',2,120,0,'','2017-01-05T12:18:14Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (6,'Make a plan for my own article',2,60,0,'','2017-01-05T12:19:44Z',0, '');");

        db.execSQL("INSERT INTO `tasks` VALUES (7,'Draw wireframes',3,90,0,'','2017-01-05T12:30:54Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (8,'Create database structure',3,120,0,'','2017-01-05T12:31:59Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (9,'Write DBHelper class',3,60,0,'','2017-01-05T12:32:44Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (10,'Write code for alarm broadcast receiver',3,120,0,'','2017-01-05T12:33:23Z',0, '');");
        db.execSQL("INSERT INTO `tasks` VALUES (11,'Create activity for database edition',3,330,0,'','2017-01-05T12:34:07Z',0, '');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS projects");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS time_intervals");
        onCreate(db);
    }
}
