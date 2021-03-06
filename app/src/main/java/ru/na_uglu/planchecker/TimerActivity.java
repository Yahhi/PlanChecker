package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class TimerActivity extends AppCompatActivity implements OnFragmentTimeAddedListener {

    int taskId;
    private int realTime;
    private boolean pomodoroMode = false;
    private timeIsGoing timingFragment;

    boolean mBound;
    WhenhubSync timeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (WhenhubSync.isSyncAvailable(getBaseContext())) {
            startService(new Intent(this, WhenhubSync.class));
        }

        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", 0);
        pomodoroMode = intent.getBooleanExtra("pomodoroMode", false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (pomodoroMode) {
            this.setTitle(R.string.title_activity_timer_pomodoro);
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
        String title = data.getProjectTitleForTask(taskId);
        projectTitle.setText(title);
        TextView estimatedTimeText = (TextView) findViewById(R.id.chronometer_estimated_task_time);
        estimatedTimeText.setText(DateTimeFormater.formatTimeInHoursAndMinutes(task.plannedTime));
        realTime = task.realTime;
        showRealTime();
        int[] lastTimerIntervals = data.getFiveLastTimeIntervals(taskId);
        showFiveLastTimersIntervals(lastTimerIntervals);
        data.closeDataConnection();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_task_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timingFragment.isTimerActive()) {
                    DialogInterface.OnClickListener wannaStopTimer = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    timingFragment.stopTimerAndSave();
                                    saveDoneTask();
                                    break;

                            }
                        }
                    };
                    AlertDialog.Builder wannaStopDialog = new AlertDialog.Builder(TimerActivity.this);
                    wannaStopDialog.setTitle(R.string.back_on_timer_activity_title);
                    wannaStopDialog.setMessage(R.string.back_on_timer_activity);
                    wannaStopDialog.setNegativeButton(R.string.no, null);
                    wannaStopDialog.setPositiveButton(R.string.yes, wannaStopTimer);
                    wannaStopDialog.setCancelable(false);
                    wannaStopDialog.show();
                } else {
                    saveDoneTask();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void saveDoneTask() {
        LocalData data = new LocalData(getApplicationContext(), true);
        data.makeTaskDone(taskId);
        if (mBound && WhenhubSync.isSyncAvailable(getBaseContext())) {
            Task task = data.getTask(taskId);
            String title = data.getProjectTitleForTask(taskId);
            title = task.title + " (" + title + ")";
            Task taskDone = data.getTask(taskId);
            timeService.createEventForAccuracy(new WhenhubEvent(
                    title,
                    DateTimeFormater.formatDate(),
                    WhenhubSync.getAccuracyRate(taskDone.realTime, taskDone.plannedTime))
            );
        }
        data.closeDataConnection();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showRealTime() {
        TextView realTimeText = (TextView) findViewById(R.id.chronometer_total_task_time);
        realTimeText.setText(DateTimeFormater.formatTimeInHoursAndMinutes(realTime));
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
                viewsForTimeIntervals[i].setText(DateTimeFormater.formatTimeInHoursAndMinutes(lastTimerIntervals[i]));
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
                        setResult(Activity.RESULT_OK);
                        finish();
                        break;

                }
            }
        };
        AlertDialog.Builder wannaStopDialog = new AlertDialog.Builder(this);
        wannaStopDialog.setTitle(R.string.back_on_timer_activity_title);
        wannaStopDialog.setMessage(R.string.back_on_timer_activity);
        wannaStopDialog.setNegativeButton(R.string.no, null);
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

        if (WhenhubSync.isSyncAvailable(getBaseContext())) {
            TextView taskTitle = (TextView) findViewById(R.id.chronometer_task_title);
            TextView projectTitle = (TextView) findViewById(R.id.chronometer_project_title);
            String title = taskTitle.getText().toString() +
                    " (" + projectTitle.getText().toString() + ")";
            timeService.createEventForTimeIntervals(new WhenhubEvent(title, DateTimeFormater.formatDate(), timeInMinutes));
        }

        setResult(Activity.RESULT_OK);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, WhenhubSync.class);
        bindService(intent, networkConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(networkConnection);
            mBound = false;
        }
    }

    private ServiceConnection networkConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WhenhubSync.LocalBinder myLocalBinder = (WhenhubSync.LocalBinder) service;
            timeService = myLocalBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timeService = null;
            mBound = false;
        }
    };
}
