package ru.na_uglu.planchecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "projectChecker.db";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 12);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `projects` (\n" +
                " `id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " `title` TEXT NOT NULL,\n" +
                " `comment` TEXT,\n" +
                " `done` INTEGER DEFAULT 0,\n" +
                " `last_modified` TEXT);");
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
                "`when_done` TEXT, " +
                "`last_modified` TEXT)");
        insertTasksData(db);

        db.execSQL("CREATE TABLE `time_intervals` (" +
                " `when_added` TEXT NOT NULL, " +
                " `task_id` INTEGER NOT NULL, " +
                " `time` INTEGER NOT NULL, " +
                " PRIMARY KEY(`when_added`))");
    }

    private void insertProjectData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO `projects` VALUES (1,'"+context.getResources().getString(R.string.project_foreign_language)+"','"+
                context.getResources().getString(R.string.project_foreign_language_comment)+"', 0, '"+LocalData.formatDate()+"');");
        db.execSQL("INSERT INTO `projects` VALUES (2,'"+context.getResources().getString(R.string.project_blogging)+"','', 0, '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `projects` VALUES (3,'"+context.getResources().getString(R.string.project_app)+"','', 0, '"+LocalData.formatDate()+");");
    }

    private void insertTasksData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO `tasks` VALUES (1,'"+context.getResources().getString(R.string.task_read_story)+"',1,300,0,'', '2017-01-05T12:00:00Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (2,'"+context.getResources().getString(R.string.task_grammar)+"',1,60,0,'','2017-01-05T12:10:32Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (3,'"+context.getResources().getString(R.string.task_new_words)+"',1,120,0,'','2017-01-05T12:12:04Z',0, '', '"+LocalData.formatDate()+");");

        db.execSQL("INSERT INTO `tasks` VALUES (4,'"+context.getResources().getString(R.string.task_choose_theme)+"',2,30,0,'"+
                context.getResources().getString(R.string.task_choose_theme_comment)+"','2017-01-05T12:17:08Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (5,'"+context.getResources().getString(R.string.task_find_5_articles)+"',2,120,0,'','2017-01-05T12:18:14Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (6,'"+context.getResources().getString(R.string.task_make_plan)+"',2,60,0,'','2017-01-05T12:19:44Z',0, '', '"+LocalData.formatDate()+");");

        db.execSQL("INSERT INTO `tasks` VALUES (7,'"+context.getResources().getString(R.string.task_wireframes)+"',3,90,0,'','2017-01-05T12:30:54Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (8,'"+context.getResources().getString(R.string.task_database)+"',3,120,0,'','2017-01-05T12:31:59Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (9,'"+context.getResources().getString(R.string.task_dbhelper)+"',3,60,0,'','2017-01-05T12:32:44Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (10,'"+context.getResources().getString(R.string.task_write_code)+"',3,120,0,'','2017-01-05T12:33:23Z',0, '', '"+LocalData.formatDate()+");");
        db.execSQL("INSERT INTO `tasks` VALUES (11,'"+context.getResources().getString(R.string.task_activity)+"',3,330,0,'','2017-01-05T12:34:07Z',0, '', '"+LocalData.formatDate()+");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= 8) {
            db.execSQL("ALTER TABLE projects ADD COLUMN `last_modified` TEXT");
            ContentValues projectUpdateValues = new ContentValues();
            projectUpdateValues.put("last_modified", LocalData.formatDate());
            db.update("projects", projectUpdateValues, null, null);

            db.execSQL("ALTER TABLE tasks ADD COLUMN `last_modified` TEXT");
            ContentValues taskUpdateValues = new ContentValues();
            taskUpdateValues.put("last_modified", LocalData.formatDate());
            db.update("tasks", taskUpdateValues, null, null);

            db.execSQL("CREATE TABLE `time_intervals_backup` (" +
                    " `when_added` TEXT NOT NULL, " +
                    " `task_id` INTEGER NOT NULL, " +
                    " `time` INTEGER NOT NULL, " +
                    " PRIMARY KEY(`when_added`))");
            db.execSQL("INSERT INTO time_intervals_backup SELECT when_added, task_id, time FROM time_intervals");
            db.execSQL("DROP TABLE time_intervals");
            db.execSQL("ALTER TABLE time_intervals_backup RENAME TO time_intervals");
        } else {
            db.execSQL("DROP TABLE IF EXISTS projects");
            db.execSQL("DROP TABLE IF EXISTS tasks");
            db.execSQL("DROP TABLE IF EXISTS time_intervals");
            onCreate(db);
        }
    }
}
