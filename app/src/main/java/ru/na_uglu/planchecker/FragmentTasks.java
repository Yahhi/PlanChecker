package ru.na_uglu.planchecker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class FragmentTasks extends Fragment {

    private static final int REQUEST_TIME_VIEW = 568;
    int projectId;
    boolean done;

    public FragmentTasks() {
    }

    public static FragmentTasks newInstance(int projectId, boolean doneTasks) {
        FragmentTasks fragment = new FragmentTasks();
        Bundle args = new Bundle();
        args.putInt("projectId", projectId);
        args.putBoolean("doneTasks", doneTasks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getInt("projectId");
            done = getArguments().getBoolean("doneTasks");
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
        final TasksAdapter adapter = new TasksAdapter(context, data.getTasksForProject(projectId, done));
        doneTasksList.setAdapter(adapter);
        doneTasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task clickedTask = (Task) adapter.getItem(position);
                if (clickedTask.realTime > 0) {
                    Intent intent = new Intent(getContext(), InfoTaskActivity.class);
                    intent.putExtra("taskId", (int) id);
                    startActivityForResult(intent, REQUEST_TIME_VIEW);
                }
            }
        });
        data.closeDataConnection();

        return view;
    }

}
