package ru.na_uglu.planchecker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentProjectInfo extends Fragment {

    int projectId;

    public FragmentProjectInfo() {
    }

    public static FragmentProjectInfo newInstance(int projectId) {
        FragmentProjectInfo fragment = new FragmentProjectInfo();
        Bundle args = new Bundle();
        args.putInt("projectId", projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getInt("projectId");
        } else {
            projectId = 1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);

        TextView projectStarted = (TextView) view.findViewById(R.id.project_started_date);
        TextView projectCompleted = (TextView) view.findViewById(R.id.project_compleated_date);
        TextView projectRealTime = (TextView) view.findViewById(R.id.project_real_time);
        TextView projectEstimatedTime = (TextView) view.findViewById(R.id.project_estimated_time);
        TextView projectAverageTime = (TextView) view.findViewById(R.id.project_average_time);

        Context context = view.getContext();
        LocalData data = new LocalData(context, false);
        Project myProject = data.getProject(projectId);
        projectStarted.setText(myProject.getDateStarted(getContext()));
        projectCompleted.setText(myProject.getDateCompleted(getContext()));
        projectRealTime.setText(Task.formatTimeInHoursAndMinutes(myProject.getRealTime()));
        projectEstimatedTime.setText(Task.formatTimeInHoursAndMinutes(myProject.getPlannedTime()));
        projectAverageTime.setText(Task.formatTimeInHoursAndMinutes(data.getAverageTime(projectId)));

        return view;
    }
}
