package ru.na_uglu.planchecker;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ru.na_uglu.planchecker.R.id.never;
import static ru.na_uglu.planchecker.R.id.time;

public class WorkingTimeChart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working_time_chart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BarChart chart = (BarChart) findViewById(R.id.chart);

        LocalData data = new LocalData(this, false);
        ArrayList<TimeInterval> timeIntervals = data.getTimeIntervals();

        int daysInInterval = getDaysCountInInterval(
                timeIntervals.get(0).whenHappened,
                timeIntervals.get(timeIntervals.size()-1).whenHappened) + 1;
        final String[] xAxisText = fillDatesArray(timeIntervals.get(0).whenHappened, daysInInterval);
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisText[(int) value];
            }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);

        String day = timeIntervals.get(0).getDateWhenHappened();
        ArrayList<ArrayList<Float>> bars = new ArrayList<>();
        int barId = 0;
        for (int i = 0; i < daysInInterval; i++) {
            ArrayList<TimeInterval> timeIntervalsInDay = getTimeIntervalsInDay(timeIntervals, xAxisText[i]);
            bars.add(new ArrayList<Float>());
            if (timeIntervalsInDay.size() > 0) {
                timeIntervalsInDay = orderTimeIntervalsByTask(timeIntervalsInDay);
                int intervalIndex = 0;
                int taskId = timeIntervals.get(intervalIndex).taskId;
                int time = 0;
                for (TimeInterval intervalInDay: timeIntervalsInDay) {
                    if (taskId == intervalInDay.taskId) {
                        time += intervalInDay.time;
                    } else {
                        bars.get(barId).add((float) time);
                        taskId = intervalInDay.taskId;
                        time = intervalInDay.time;
                    }
                }
                bars.get(barId).add((float) time);
            }
            barId++;
        }

        List<BarEntry> entries = getBarEntrysFrom2dList(bars);

        BarDataSet dataSet = new BarDataSet(entries, "Working times");
        int[] colors = new int[3];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }
        dataSet.setColors(colors);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);


        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(true);
        chart.setDrawValueAboveBar(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.invalidate();
    }

    private ArrayList<BarEntry> getBarEntrysFrom2dList(ArrayList<ArrayList<Float>> bars) {
        int maxStack = getMaxSize(bars);
        int i = 0;
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (ArrayList<Float> oneBar: bars) {
            float[] timesToOneBar = new float[maxStack];
            int j = 0;
            for (Float f : oneBar) {
                timesToOneBar[j++] = f;
            }
            barEntries.add(new BarEntry(i++, timesToOneBar));
        }
        return barEntries;
    }

    private int getMaxSize(ArrayList<ArrayList<Float>> bars) {
        int max = 0;
        for (ArrayList<Float> bar : bars) {
            if (max < bar.size()) {
                max = bar.size();
            }
        }
        return max;
    }

    private ArrayList<TimeInterval> orderTimeIntervalsByTask(ArrayList<TimeInterval> timeIntervalsInDay) {
        Collections.sort(timeIntervalsInDay, new Comparator<TimeInterval>() {
            @Override
            public int compare(TimeInterval o1, TimeInterval o2) {
                return o1.taskId - o2.taskId;
            }
        });
        return timeIntervalsInDay;

    }

    private ArrayList<TimeInterval> getTimeIntervalsInDay(ArrayList<TimeInterval> timeIntervals, String s) {
        ArrayList<TimeInterval> inDay = new ArrayList<>();
        for (TimeInterval interval : timeIntervals) {
            if (interval.getDateWhenHappened().contains(s)) {
                inDay.add(interval);
            }
        }
        return inDay;
    }

    private String[] fillDatesArray(Date whenHappened, int daysInInterval) {
        String[] dates = new String[daysInInterval];
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(whenHappened);
        for (int i = 0; i < daysInInterval; i++) {
            dates[i] = format.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates;
    }

    private int getDaysCountInInterval(Date fromDate, Date toDate) {
        int daysCount;
        Calendar calendarFrom = Calendar.getInstance();
        Calendar calendarTo = Calendar.getInstance();
        calendarFrom.setTime(fromDate);
        calendarTo.setTime(toDate);
        if (calendarFrom.get(Calendar.YEAR) != calendarTo.get(Calendar.YEAR)) {
            daysCount = calendarTo.get(Calendar.DAY_OF_YEAR) + (calendarFrom.getActualMaximum(Calendar.DAY_OF_YEAR) - calendarFrom.get(Calendar.DAY_OF_YEAR));
        } else {
            daysCount = calendarTo.get(Calendar.DAY_OF_YEAR) - calendarFrom.get(Calendar.DAY_OF_YEAR);
        }
        return daysCount;
    }

}
