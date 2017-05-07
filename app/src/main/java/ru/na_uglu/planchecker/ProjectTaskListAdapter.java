package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
    static final int REQUEST_TIMER = 301;
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
        return projects.get(groupPosition).id;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return projects.get(groupPosition).tasks.get(childPosition).id;
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
        estimatedProjectTime.setText(
                Task.formatTimeInHoursAndMinutes(projects.get(groupPosition).getPlannedTime()));

        TextView realProjectTime = (TextView) convertView.findViewById((R.id.real_project_time));
        realProjectTime.setText(
                Task.formatTimeInHoursAndMinutes(projects.get(groupPosition).getRealTime()));

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        TextView taskTitle = (TextView) convertView.findViewById(R.id.task_title);
        taskTitle.setText(projects.get(groupPosition).tasks.get(childPosition).title);

        TextView estimatedTaskTime = (TextView) convertView.findViewById(R.id.estimated_task_time);
        estimatedTaskTime.setText(
                Task.formatTimeInHoursAndMinutes(
                        projects.get(groupPosition).tasks.get(childPosition).plannedTime));

        TextView realTaskTime = (TextView) convertView.findViewById(R.id.real_task_time);
        realTaskTime.setText(
                Task.formatTimeInHoursAndMinutes(
                        projects.get(groupPosition).tasks.get(childPosition).realTime));

        ImageButton pomodoroButton = (ImageButton) convertView.findViewById(R.id.pomodoro_button);
        pomodoroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TimerActivity.class);
                intent.putExtra("taskId", projects.get(groupPosition).tasks.get(childPosition).id);
                intent.putExtra("pomodoroMode", true);
                ((Activity) context).startActivityForResult(intent, REQUEST_TIMER);
            }
        });

        ImageButton timerButton = (ImageButton) convertView.findViewById(R.id.timer_button);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TimerActivity.class);
                intent.putExtra("taskId", projects.get(groupPosition).tasks.get(childPosition).id);
                ((Activity) context).startActivityForResult(intent, REQUEST_TIMER);
            }
        });

        ImageButton editButton = (ImageButton) convertView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddTask.class);
                intent.putExtra("taskId", projects.get(groupPosition).tasks.get(childPosition).id);
                ((Activity) context).startActivityForResult(intent, REQUEST_TIMER);
            }
        });

        ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).askIfReallyDoSomething(
                        projects.get(groupPosition).tasks.get(childPosition).title,
                        context.getString(R.string.delete_task_dialog),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalData data = new LocalData(context, true);
                                data.deleteTask(projects.get(groupPosition).tasks.get(childPosition).id);
                                data.closeDataConnection();

                                projects.get(groupPosition).tasks.remove(childPosition);
                                notifyDataSetChanged();
                            }
                        }
                );
            }
        });

        ImageButton doneButton = (ImageButton) convertView.findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).askIfReallyDoSomething(
                        projects.get(groupPosition).tasks.get(childPosition).title,
                        context.getString(R.string.make_task_done_dialog),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalData data = new LocalData(context, true);
                                data.makeTaskDone(projects.get(groupPosition).tasks.get(childPosition).id);
                                data.closeDataConnection();

                                projects.get(groupPosition).tasks.remove(childPosition);
                                notifyDataSetChanged();
                            }
                        }
                );
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
