package ru.na_uglu.planchecker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;


class ProjectTaskListAdapter extends BaseExpandableListAdapter {
    private ArrayList<Project> projects;
    private Context context;

    ProjectTaskListAdapter(Context context, ArrayList<Project> projects) {
        this.projects = projects;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return projects.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return projects.get(groupPosition).tasks.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return projects.get(groupPosition).tasks;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return projects.get(groupPosition).tasks.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.project_item, parent, false);
        }

        TextView projectTitle = (TextView) convertView.findViewById(R.id.proect_title);
        projectTitle.setText(projects.get(groupPosition).title);

        TextView estimatedProjectTime = (TextView) convertView.findViewById(R.id.estimated_project_time);
        estimatedProjectTime.setText(Long.toString(projects.get(groupPosition).getPlannedTime()));

        TextView realProjectTime = (TextView) convertView.findViewById((R.id.real_project_time));
        realProjectTime.setText(Long.toString(projects.get(groupPosition).getRealTime()));

        /*ImageButton addTaskButton = (ImageButton) convertView.findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //TODO open add project activity
            }
        });*/

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        TextView taskTitle = (TextView) convertView.findViewById(R.id.task_title);
        taskTitle.setText(projects.get(groupPosition).tasks.get(childPosition).title);

        TextView estimatedTaskTime = (TextView) convertView.findViewById(R.id.estimated_task_time);
        estimatedTaskTime.setText(Long.toString(projects.get(groupPosition).tasks.get(childPosition).plannedTime));

        TextView realTaskTime = (TextView) convertView.findViewById(R.id.real_task_time);
        realTaskTime.setText(Long.toString(projects.get(groupPosition).tasks.get(childPosition).realTime));

        ImageButton pomodoroButton = (ImageButton) convertView.findViewById(R.id.pomodoro_button);
        pomodoroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO open activity with pomodoro timer
            }
        });

        ImageButton timerButton = (ImageButton) convertView.findViewById(R.id.timer_button);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO open common timer activity
            }
        });

        ImageButton editButton = (ImageButton) convertView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO open task editor
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
