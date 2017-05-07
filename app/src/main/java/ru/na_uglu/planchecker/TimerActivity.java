package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import static android.R.attr.fragment;

public class TimerActivity extends AppCompatActivity implements OnFragmentTimeAddedListener {
    private Chronometer chronometer;
    long chronometerBase;

    int taskId;
    private int realTime;
    private boolean pomodoroMode = false;
    private timeIsGoing timingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", 0);
        pomodoroMode = intent.getBooleanExtra("pomodoroMode", false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (pomodoroMode) {
            PomodoroFragment fragment = new PomodoroFragment();
            timingFragment = fragment;
            transaction.add(R.id.place_for_timer, fragment);
        } else {
            CommonTimerFragment fragment = new CommonTimerFragment();
            timingFragment = fragment;
            transaction.add(R.id.place_for_timer, fragment);
        }
        transaction.commit();

        LocalData data = new LocalData(this, false);
        Task task = data.getTask(taskId);
        TextView taskTitle = (TextView) findViewById(R.id.chronometer_task_title);
        taskTitle.setText(task.title);
        TextView projectTitle = (TextView) findViewById(R.id.chronometer_project_title);
        projectTitle.setText(data.getProjectTitleForTask(taskId));
        TextView estimatedTime = (TextView) findViewById(R.id.chronometer_estimated_task_time);
        estimatedTime.setText(Task.formatTimeInHoursAndMinutes(task.plannedTime));
        realTime = task.realTime;
        showRealTime();
        int[] lastTimerIntervals = data.getFiveLastTimeIntervals(taskId);
        showFiveLastTimersIntervals(lastTimerIntervals);
        data.closeDataConnection();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_task_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalData data = new LocalData(getApplicationContext(), true);
                data.makeTaskDone(taskId);
                data.closeDataConnection();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void showRealTime() {
        TextView realTimeText = (TextView) findViewById(R.id.chronometer_total_task_time);
        realTimeText.setText(Task.formatTimeInHoursAndMinutes(realTime));
    }

    private void showFiveLastTimersIntervals(int[] lastTimerIntervals) {
        TextView[] viewsForTimeIntervals = new TextView[5];
        viewsForTimeIntervals[0] = (TextView) findViewById(R.id.lastTimers0);
        viewsForTimeIntervals[1] = (TextView) findViewById(R.id.lastTimers1);
        viewsForTimeIntervals[2] = (TextView) findViewById(R.id.lastTimers2);
        viewsForTimeIntervals[3] = (TextView) findViewById(R.id.lastTimers3);
        viewsForTimeIntervals[4] = (TextView) findViewById(R.id.lastTimers4);
        for (int i = 0; i < 5; i++) {
            if (i < lastTimerIntervals.length) {
                viewsForTimeIntervals[i].setText(Task.formatTimeInHoursAndMinutes(lastTimerIntervals[i]));
                viewsForTimeIntervals[i].setVisibility(View.VISIBLE);
            } else {
                viewsForTimeIntervals[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (timingFragment.isTimerActive()) {
            askIfStopChronometer();
        } else {
            super.onBackPressed();
        }
    }

    private void askIfStopChronometer() {
        DialogInterface.OnClickListener wannaStopTimer = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        timingFragment.stopTimerAndSave();
                        finish();
                        break;

                }
            }
        };
        AlertDialog.Builder wannaStopDialog = new AlertDialog.Builder(this);
        wannaStopDialog.setTitle(R.string.back_on_timer_activity_title);
        wannaStopDialog.setMessage(R.string.back_on_timer_activity);
        wannaStopDialog.setNegativeButton(R.string.no, wannaStopTimer);
        wannaStopDialog.setPositiveButton(R.string.yes, wannaStopTimer);
        wannaStopDialog.setCancelable(false);
        wannaStopDialog.show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveImportantVariables();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreImportantVariables();
    }

    private void restoreImportantVariables() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        taskId = preferences.getInt("taskId", 0);
        realTime = preferences.getInt("realTime", 0);
    }

    private void saveImportantVariables() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt("taskId", taskId);
        preferencesEditor.putInt("realTime", realTime);
        preferencesEditor.apply();
    }

    @Override
    public int getTaskIdentifier() {
        return taskId;
    }

    @Override
    public void onTimeAddedInteraction(int timeInMinutes) {
        LocalData data = new LocalData(this, true);
        data.addTimeToTask(taskId, timeInMinutes);
        realTime += timeInMinutes;
        showRealTime();
        int[] lastTimerIntervals = data.getFiveLastTimeIntervals(taskId);
        showFiveLastTimersIntervals(lastTimerIntervals);
        data.closeDataConnection();
        setResult(Activity.RESULT_OK);
    }

}
