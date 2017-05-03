package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class TimerActivity extends AppCompatActivity {
    private Chronometer chronometer;
    private boolean chronometerActive = false;
    long chronometerBase;

    private int taskId;
    private int realTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chronometer = (Chronometer) findViewById(R.id.chronometer_timer);
        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", 0);
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

    public void onPlayPause(View view) {
        if (chronometerActive) {
            chronometer.stop();
            LocalData data = new LocalData(this, true);
            int timeInMinutes = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
            data.addTimeToTask(taskId, timeInMinutes);
            realTime += timeInMinutes;
            showRealTime();
            int[] lastTimerIntervals = data.getFiveLastTimeIntervals(taskId);
            showFiveLastTimersIntervals(lastTimerIntervals);
            data.closeDataConnection();

            Button playPauseButton = (Button) findViewById(R.id.StartChronometerButton);
            playPauseButton.setBackgroundResource(R.drawable.ic_play_circle_filled_24dp);
            chronometer.setBase(SystemClock.elapsedRealtime());
            setResult(Activity.RESULT_OK);
        } else {
            Button playPauseButton = (Button) findViewById(R.id.StartChronometerButton);
            playPauseButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp);
            chronometerBase = SystemClock.elapsedRealtime();
            chronometer.setBase(chronometerBase);
            chronometer.start();
        }
        chronometerActive = !chronometerActive;
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
        if (chronometerActive) {
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
                        Button playPauseButton = (Button) findViewById(R.id.StartChronometerButton);
                        playPauseButton.callOnClick();
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
        chronometerActive = preferences.getBoolean("chronometerActive", false);
        chronometerBase = preferences.getLong("chronometerBase", 0);
        if (chronometerActive) {
            chronometer.setBase(chronometerBase);
            chronometer.start();
        }
        taskId = preferences.getInt("taskId", 0);
        realTime = preferences.getInt("realTime", 0);
    }

    private void saveImportantVariables() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("chronometerActive", chronometerActive);
        preferencesEditor.putLong("chronometerBase", chronometerBase);
        preferencesEditor.putInt("taskId", taskId);
        preferencesEditor.putInt("realTime", realTime);
        preferencesEditor.apply();
    }
}
