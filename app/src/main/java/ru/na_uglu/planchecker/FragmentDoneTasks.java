package ru.na_uglu.planchecker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

public class FragmentDoneTasks extends Fragment {

    int projectId;

    public FragmentDoneTasks() {
    }

    public static FragmentDoneTasks newInstance(int projectId) {
        FragmentDoneTasks fragment = new FragmentDoneTasks();
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
            projectId = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done_tasks, container, false);

        ListView doneTasksList = (ListView) view.findViewById(R.id.list_done_tasks);
        Context context = view.getContext();
        LocalData data = new LocalData(context, false);
        doneTasksList.setAdapter(new DoneTasksAdapter(context, data.getDoneTasksForProject(projectId)));
        data.closeDataConnection();

        return view;
    }

}
