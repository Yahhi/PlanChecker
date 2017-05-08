package ru.na_uglu.planchecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PomodoroFragment extends Fragment implements timeIsGoing {

    private static int TIME25MINUTES = 2 * 60;
    private static int TIME5MINUTES = 1 * 60;

    private PomodoroStatus pomodoroStatus = PomodoroStatus.notActive;
    private long pomodoroBase;
    private long pomodoroDone;
    private long whenToStopInMillis;
    private Button playPauseButton;
    private Button flowButton;
    private Button relaxButton;
    private TextView pomodoroTimeForUser;

    private ScheduledExecutorService updateTimeOnUI;

    private OnFragmentTimeAddedListener mListener;

    private PendingIntent alarmIntent;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("POMODORO", "received broadcast");
            Log.i("POMODORO", "Status changed from " + pomodoroStatus.toString());
            boolean over25minutes = intent.getBooleanExtra("inform25minutes", true);
            if (over25minutes) {
                statusChangeFromActiveToEnded();
            } else {
                statusChangeFromRelaxToNotActive();
            }
            Log.i("POMODORO", "to " + pomodoroStatus.toString());
        }
    };

    private void statusChangeFromRelaxToNotActive() {
        playPauseButton.setBackgroundResource(R.drawable.ic_play_circle_filled_24dp);
        pomodoroStatus = PomodoroStatus.notActive;
        pomodoroTimeForUser.setText("25:00");
    }

    private void statusChangeFromActiveToEnded() {
        showFlowRelaxButtons();
        pomodoroStatus = PomodoroStatus.ended;
    }

    private void showFlowRelaxButtons() {
        pomodoroTimeForUser.setText("00:00");
        playPauseButton.setVisibility(View.INVISIBLE);
        flowButton.setVisibility(View.VISIBLE);
        relaxButton.setVisibility(View.VISIBLE);
    }
    private void hideFlowRelaxButtons() {
        playPauseButton.setVisibility(View.VISIBLE);
        flowButton.setVisibility(View.INVISIBLE);
        relaxButton.setVisibility(View.INVISIBLE);
    }

    public PomodoroFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTimeOnUI = Executors.newSingleThreadScheduledExecutor();
        updateTimeOnUI.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                changeTimeValues();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void changeTimeValues() {
        if (pomodoroStatus.equals(PomodoroStatus.active)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pomodoroTimeForUser.setText(
                            formatTimeInMinutesAndSeconds(
                                    (whenToStopInMillis - SystemClock.elapsedRealtime())));
                }
            });
        } else if (pomodoroStatus.equals(PomodoroStatus.flow)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pomodoroTimeForUser.setText(
                            "25:00 + " + formatTimeInMinutesAndSeconds(
                                    (SystemClock.elapsedRealtime() - pomodoroBase - TIME25MINUTES * 1000)));
                }
            });
        } else if (pomodoroStatus.equals(PomodoroStatus.relax)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pomodoroTimeForUser.setText(
                            formatTimeInMinutesAndSeconds(
                                    (whenToStopInMillis - SystemClock.elapsedRealtime())));
                }
            });
        }
    }

    private String formatTimeInMinutesAndSeconds(long timeInMillis) {
        String timeString;
        long minutes = timeInMillis / 1000 / 60;
        long seconds = timeInMillis / 1000 - minutes * 60;
        if (minutes < 10) {
            timeString = "0" + minutes;
        } else {
            timeString = Long.toString(minutes);
        }
        timeString += ":";
        if (seconds < 10) {
            timeString += "0" + seconds;
        } else {
            timeString += Long.toString(seconds);
        }
        return timeString;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pomodoro, container, false);
        pomodoroTimeForUser = (TextView) view.findViewById(R.id.pomodoro_time);
        playPauseButton = (Button) view.findViewById(R.id.button_play_pause_pomodoro);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("POMODORO", "Status changed from " + pomodoroStatus.toString());
                if (pomodoroStatus.equals(PomodoroStatus.notActive)) {
                    playPauseButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    pomodoroStatus = PomodoroStatus.active;
                    pomodoroBase = SystemClock.elapsedRealtime();
                    startCountdownTimer(TIME25MINUTES);
                } else if (pomodoroStatus.equals(PomodoroStatus.active)){
                    playPauseButton.setBackgroundResource(R.drawable.ic_play_circle_filled_24dp);
                    pomodoroStatus = PomodoroStatus.paused;
                    cancelAlarm();
                    pomodoroDone = SystemClock.elapsedRealtime() - pomodoroBase;
                } else if (pomodoroStatus.equals(PomodoroStatus.paused)){
                    playPauseButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    pomodoroStatus = PomodoroStatus.active;
                    pomodoroBase = SystemClock.elapsedRealtime() - pomodoroDone;
                    startCountdownTimer(TIME25MINUTES - (int) (pomodoroDone / 1000));
                } else if (pomodoroStatus.equals(PomodoroStatus.relax)) {
                    playPauseButton.setBackgroundResource(R.drawable.ic_play_circle_filled_24dp);
                    pomodoroStatus = PomodoroStatus.notActive;
                    cancelAlarm();
                    pomodoroTimeForUser.setText("25:00");
                } else if (pomodoroStatus.equals(PomodoroStatus.flow)) {
                    playPauseButton.setBackgroundResource(R.drawable.ic_play_circle_filled_24dp);
                    pomodoroStatus = PomodoroStatus.notActive;
                    pomodoroTimeForUser.setText("25:00");
                    int timeInMinutes = (int) ((SystemClock.elapsedRealtime() - pomodoroBase) / 60 / 1000);
                    if (mListener != null) {
                        mListener.onTimeAddedInteraction(timeInMinutes);
                    }
                }
                Log.i("POMODORO", "to " + pomodoroStatus.toString());
            }
        });
        flowButton = (Button) view.findViewById(R.id.button_flow);
        flowButton.setVisibility(View.INVISIBLE);
        flowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("POMODORO", "Status changed from " + pomodoroStatus.toString());
                pomodoroStatus = PomodoroStatus.flow;
                flowButton.setVisibility(View.INVISIBLE);
                relaxButton.setVisibility(View.INVISIBLE);
                playPauseButton.setVisibility(View.VISIBLE);
                playPauseButton.setBackgroundResource(R.drawable.ic_stop);
                Log.i("POMODORO", "to " + pomodoroStatus.toString());
            }
        });
        relaxButton = (Button) view.findViewById(R.id.button_relax);
        relaxButton.setVisibility(View.INVISIBLE);
        relaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("POMODORO", "Status changed from " + pomodoroStatus.toString());
                startCountdownTimer(TIME5MINUTES);
                pomodoroStatus = PomodoroStatus.relax;
                flowButton.setVisibility(View.INVISIBLE);
                relaxButton.setVisibility(View.INVISIBLE);
                playPauseButton.setVisibility(View.VISIBLE);
                playPauseButton.setBackgroundResource(R.drawable.ic_skip);
                if (mListener != null) {
                    mListener.onTimeAddedInteraction(25);
                }
                Log.i("POMODORO", "to " + pomodoroStatus.toString());
            }
        });

        return view;
    }

    private void startCountdownTimer(int seconds) {
        whenToStopInMillis = SystemClock.elapsedRealtime() + seconds * 1000;
        setAlarm(seconds);
    }

    private void setAlarm(int timeToWaitInSeconds) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), TimerEndReceiver.class);
        intent.putExtra("vibration", false);
        int taskId = ((OnFragmentTimeAddedListener) getActivity()).getTaskIdentifier();
        intent.putExtra("taskId", taskId);
        if (timeToWaitInSeconds == (TIME5MINUTES)) {
            intent.putExtra("inform25minutes", false);
            Log.i("POMODORO", "inform25minutes = false");
        }
        long timeWhenStartedInMillis = Calendar.getInstance().getTime().getTime();
        alarmIntent = PendingIntent.getBroadcast(getContext(), 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long whenToAlarm = getTimeOnCalendar(timeWhenStartedInMillis, timeToWaitInSeconds * 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, whenToAlarm, alarmIntent);
            String dateString = DateFormat.getDateTimeInstance().format(whenToAlarm);
            Log.i("POMODORO", "Alarm created for " + dateString + " with intent 1000");
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, whenToAlarm, alarmIntent);
        }
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Log.i("POMODORO", "Alarm cancelled");
        alarmManager.cancel(alarmIntent);
    }

    private long getTimeOnCalendar(long time, long timeToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.MILLISECOND, (int) timeToAdd);
        return calendar.getTime().getTime();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentTimeAddedListener) {
            mListener = (OnFragmentTimeAddedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        updateTimeOnUI.shutdown();
    }

    @Override
    public boolean isTimerActive() {
        return pomodoroStatus.isTimerActive();
    }

    @Override
    public void stopTimerAndSave() {
        int timeInMinutes = 0;

        if (pomodoroStatus.equals(PomodoroStatus.active)) {
            timeInMinutes = (int) ((SystemClock.elapsedRealtime() - pomodoroBase) / 60 / 1000);
        } else if (pomodoroStatus.equals(PomodoroStatus.paused)) {
            timeInMinutes = (int) (pomodoroDone / 60 / 1000);
        } else if(pomodoroStatus.equals(PomodoroStatus.ended)) {
            timeInMinutes = 25;
        } else if (pomodoroStatus.equals(PomodoroStatus.flow)) {
            timeInMinutes = (int) ((SystemClock.elapsedRealtime() - pomodoroBase) / 60 / 1000);
        }
        if (mListener != null) {
            mListener.onTimeAddedInteraction(timeInMinutes);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter("ru.na-uglu.planchecker"));
        restoreImportantVariables();
        changeTimeValues();
    }

    private void restoreImportantVariables() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        pomodoroStatus = PomodoroStatus.getStatusFromString(preferences.getString("pomodoroStatus", PomodoroStatus.notActive.toString()));
        pomodoroBase = preferences.getLong("pomodoroBase", 0);
        pomodoroDone = preferences.getLong("pomodoroDone", 0);
        whenToStopInMillis = preferences.getLong("whenToStopInMillis", 0);
        if (pomodoroStatus == PomodoroStatus.active) {
            if (whenToStopInMillis <= SystemClock.elapsedRealtime()) {
                statusChangeFromActiveToEnded();
            } else {
                hideFlowRelaxButtons();
            }
        } else if (pomodoroStatus == PomodoroStatus.relax) {
            if (whenToStopInMillis <= SystemClock.elapsedRealtime()) {
                statusChangeFromRelaxToNotActive();
            } else {
                playPauseButton.setBackgroundResource(R.drawable.ic_skip);
            }
        } else if (pomodoroStatus == PomodoroStatus.ended) {
            showFlowRelaxButtons();
        } else if (pomodoroStatus == PomodoroStatus.flow) {
            playPauseButton.setBackgroundResource(R.drawable.ic_stop);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        saveImportantVariables();
    }

    private void saveImportantVariables() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("pomodoroStatus", pomodoroStatus.toString());
        preferencesEditor.putLong("pomodoroBase", pomodoroBase);
        preferencesEditor.putLong("pomodorodone", pomodoroDone);
        preferencesEditor.putLong("whenToStopInMillis", whenToStopInMillis);
        preferencesEditor.apply();

    }
}
