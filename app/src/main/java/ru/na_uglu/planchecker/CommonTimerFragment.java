package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;

import static android.content.Context.MODE_PRIVATE;

public class CommonTimerFragment extends Fragment implements timeIsGoing {

    private boolean chronometerActive = false;
    private long chronometerBase;
    private Chronometer chronometer;
    private Button playPauseButton;

    private OnFragmentTimeAddedListener mListener;

    public CommonTimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_timer, container, false);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer);
        playPauseButton = (Button) view.findViewById(R.id.button_play_pause_timer);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chronometerActive) {
                    chronometer.stop();
                    int timeInMinutes = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
                    if (mListener != null) {
                        mListener.onTimeAddedInteraction(timeInMinutes);
                    }

                    playPauseButton.setBackgroundResource(R.drawable.ic_play_circle_filled_24dp);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                } else {
                    playPauseButton.setBackgroundResource(R.drawable.ic_stop);
                    chronometerBase = SystemClock.elapsedRealtime();
                    chronometer.setBase(chronometerBase);
                    chronometer.start();
                }
                chronometerActive = !chronometerActive;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getActivity().getPreferences(MODE_PRIVATE);
        chronometerActive = preferences.getBoolean("chronometerActive", false);
        chronometerBase = preferences.getLong("chronometerBase", 0);
        if (chronometerActive) {
            chronometer.setBase(chronometerBase);
            chronometer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("chronometerActive", chronometerActive);
        preferencesEditor.putLong("chronometerBase", chronometerBase);
        preferencesEditor.apply();
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
    }

    @Override
    public boolean isTimerActive() {
        return chronometerActive;
    }

    @Override
    public void stopTimerAndSave() {
        playPauseButton.callOnClick();
    }
}
