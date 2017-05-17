package ru.na_uglu.planchecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

class DateTimeListAdapter extends BaseExpandableListAdapter {

    Context context;
    private ArrayList<ArrayList<TimeInterval>> times;

    DateTimeListAdapter(Context context, ArrayList<TimeInterval> timeIntervals) {
        this.context = context;
        if (timeIntervals.size() == 0) {
            times = new ArrayList<>(0);
        } else {
            times = new ArrayList<>();
            Date date = timeIntervals.get(0).whenHappened;
            times.add(new ArrayList<TimeInterval>());
            int i = 0;
            for (TimeInterval timeInterval : timeIntervals) {
                if (inSameDate(timeInterval.whenHappened, date)) {
                    times.get(i).add(timeInterval);
                } else {
                    times.add(new ArrayList<TimeInterval>());
                    times.get(++i).add(timeInterval);
                    date = timeInterval.whenHappened;
                }
            }
        }
    }

    private boolean inSameDate(Date whenHappened, Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(whenHappened);
        cal2.setTime(date);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    @Override
    public int getGroupCount() {
        return times.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return times.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return times.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return times.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(times.get(groupPosition).get(0).whenHappened);
        return cal.get(Calendar.YEAR) * 365 + cal.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return times.get(groupPosition).get(childPosition).id;
    }

    void removeChildAtPosition(int groupPosition, int childPosition) {
        LocalData data1 = new LocalData(context, true);
        data1.deleteTimeInterval(times.get(groupPosition).get(childPosition).id);
        data1.closeDataConnection();

        times.get(groupPosition).remove(childPosition);
        notifyDataSetInvalidated();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.date_item, parent, false);
        }
        TextView dateView = (TextView) convertView.findViewById(R.id.date_title);
        dateView.setText(times.get(groupPosition).get(0).getDateWhenHappened());
        TextView timeInDate = (TextView) convertView.findViewById(R.id.date_time);
        timeInDate.setText(getTimeSum(times.get(groupPosition)));
        return convertView;
    }

    private String getTimeSum(ArrayList<TimeInterval> timeIntervals) {
        int timeInt = 0;
        for (TimeInterval timeInterval:timeIntervals) {
            timeInt += timeInterval.time;
        }
        return Task.formatTimeInHoursAndMinutes(timeInt);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.date_time_item, parent, false);
        }
        TextView atTime = (TextView) convertView.findViewById(R.id.time_when_done);
        atTime.setText(times.get(groupPosition).get(childPosition).getTimeWhenHappened());
        TextView minutesCount = (TextView) convertView.findViewById(R.id.minutes_amount);
        minutesCount.setText(Integer.toString(times.get(groupPosition).get(childPosition).time));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
