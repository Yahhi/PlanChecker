package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentProjectInfo extends Fragment {

    int projectId;

    private TextView projectStarted;
    private TextView projectCompleted;
    private TextView projectRealTime;
    private TextView projectEstimatedTime;
    private TextView projectAverageTime;
    private TextView projectComment;

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
        projectStarted = (TextView) view.findViewById(R.id.project_started_date);
        projectCompleted = (TextView) view.findViewById(R.id.project_compleated_date);
        projectRealTime = (TextView) view.findViewById(R.id.project_real_time);
        projectEstimatedTime = (TextView) view.findViewById(R.id.project_estimated_time);
        projectAverageTime = (TextView) view.findViewById(R.id.project_average_time);
        projectComment = (TextView) view.findViewById(R.id.project_comment_view);

        fillViewFromDatabase();

        return view;
    }

    void fillViewFromDatabase() {
        Context context = getContext();

        LocalData data = new LocalData(context, false);
        Project myProject = data.getProject(projectId);
        projectStarted.setText(myProject.getDateStarted(getContext()));
        projectCompleted.setText(myProject.getDateCompleted(getContext()));
        projectRealTime.setText(Task.formatTimeInHoursAndMinutes(myProject.getRealTime()));
        projectEstimatedTime.setText(Task.formatTimeInHoursAndMinutes(myProject.getPlannedTime()));
        projectAverageTime.setText(Task.formatTimeInHoursAndMinutes(data.getAverageTime(projectId)));
        projectComment.setText(myProject.comment);
        data.closeDataConnection();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (
                ((requestCode == ProjectView.REQUEST_PROJECT_EDITION)
                        || (requestCode == ProjectView.REQUEST_TIME_EDITION))
                && (resultCode == Activity.RESULT_OK)) {
            fillViewFromDatabase();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
