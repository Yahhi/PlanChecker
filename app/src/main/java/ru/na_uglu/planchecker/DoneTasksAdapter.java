package ru.na_uglu.planchecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class DoneTasksAdapter extends BaseAdapter {

    private final ArrayList<Task> tasks;
    Context context;

    DoneTasksAdapter(Context context, ArrayList<Task> items) {
        tasks = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_done_task, parent, false);
        }
        TextView taskTitle = (TextView) convertView.findViewById(R.id.task_title_in_done);
        taskTitle.setText(tasks.get(position).title);
        TextView taskPlannedTime = (TextView) convertView.findViewById(R.id.task_time_plan_in_done);
        taskPlannedTime.setText(Task.formatTimeInHoursAndMinutes(tasks.get(position).plannedTime));
        TextView taskRealTime = (TextView) convertView.findViewById(R.id.task_time_real_in_done);
        taskRealTime.setText(Task.formatTimeInHoursAndMinutes(tasks.get(position).realTime));
        return convertView;
    }
}
