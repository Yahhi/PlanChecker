package ru.na_uglu.planchecker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_PROJECT_EDITION = 399;
    static final int REQUEST_TASK_EDITION = 398;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_project = (FloatingActionButton) findViewById(R.id.fab_add_project);
        fab_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddProject.class);
                startActivityForResult(intent, REQUEST_PROJECT_EDITION);
            }
        });

        FloatingActionButton fab_task = (FloatingActionButton) findViewById(R.id.fab_add_task);
        fab_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddTask.class);
                startActivityForResult(intent, REQUEST_TASK_EDITION);
            }
        });

        FloatingActionButton fab_time = (FloatingActionButton) findViewById(R.id.fab_add_time);
        fab_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddTime.class);
                startActivityForResult(intent, ProjectTaskListAdapter.REQUEST_TIMER);
            }
        });

        fillTaskList();
    }

    private void fillTaskList() {
        LocalData data = new LocalData(this, false);
        ArrayList<Project> myProjects = data.getProjects();
        ProjectTaskListAdapter adapter = new ProjectTaskListAdapter(this, myProjects);
        ExpandableListView projectsList = (ExpandableListView) findViewById(R.id.projects_list);
        projectsList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ProjectTaskListAdapter.REQUEST_TIMER) {
            if (resultCode == RESULT_OK) {
                fillTaskList();
            }
        } else if ((requestCode == REQUEST_TASK_EDITION) || (requestCode == REQUEST_PROJECT_EDITION)) {
            if (resultCode == RESULT_OK) {
                fillTaskList();
            }
        }
    }
}
