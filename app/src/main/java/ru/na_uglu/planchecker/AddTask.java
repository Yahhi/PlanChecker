package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Array;
import java.util.Arrays;

public class AddTask extends AppCompatActivity {
    private int taskId;
    private TextView taskTitle;
    private Spinner projectSelection;
    private TextView taskTime;
    private TextView comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LocalData data = new LocalData(this, false);
        taskTitle = (TextView) findViewById(R.id.task_title_editable);
        taskTime = (TextView) findViewById(R.id.task_time_editable);
        comment = (TextView) findViewById(R.id.task_comment_editable);
        projectSelection = (Spinner) findViewById(R.id.project_titles_selectable);
        String[] projectTitles = data.getProjectTitles();
        projectSelection.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projectTitles));

        Intent comingData = getIntent();
        taskId = comingData.getIntExtra("taskId", 0);
        if (taskId != 0) {
            Task task = data.getTask(taskId);
            taskTitle.setText(task.title);
            taskTime.setText(Task.formatTimeInHoursAndMinutes(task.plannedTime));
            comment.setText(task.comment);
            projectSelection.setSelection(Arrays.asList(projectTitles).indexOf(data.getProjectTitleForTask(taskId)));
        }
        data.closeDataConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_menuitem) {
            LocalData data = new LocalData(this, true);
            int projectId = data.getProjectIdFromTitle(projectSelection.getSelectedItem().toString());
            data.saveTask(taskId, projectId,
                    taskTitle.getText().toString(),
                    taskTime.getText().toString(),
                    comment.getText().toString());
            data.closeDataConnection();

            setResult(Activity.RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt("taskId", taskId);
        preferencesEditor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        taskId = preferences.getInt("taskId", 0);
    }
}
