package ru.na_uglu.planchecker;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import static android.R.attr.dialogTitle;

public class InfoTaskActivity extends AppCompatActivity {

    int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        taskId = getIntent().getIntExtra("taskId", 1);

        LocalData data = new LocalData(this, false);
        this.setTitle(data.getTask(taskId).title);
        ArrayList<TimeInterval> timeIntervals = data.getTimeIntervalsForTask(taskId);
        DateTimeListAdapter adapter = new DateTimeListAdapter(this, timeIntervals);
        final ExpandableListView listTimes = (ExpandableListView) findViewById(R.id.expandable_list_times);
        listTimes.setAdapter(adapter);
        listTimes.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(parent.getContext());
                final DateTimeListAdapter listAdapter = (DateTimeListAdapter) listTimes.getExpandableListAdapter();
                TimeInterval clickedTime = (TimeInterval) listAdapter.getChild(groupPosition, childPosition);
                dialog.setTitle(clickedTime.getDateWhenHappened() + " " + clickedTime.getTimeWhenHappened());
                dialog.setMessage(R.string.delete_time);
                dialog.setNegativeButton(R.string.no, null);
                dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listAdapter.removeChildAtPosition(groupPosition, childPosition);
                    }
                });
                dialog.show();
                return true;
            }
        });
        data.closeDataConnection();

    }

}
