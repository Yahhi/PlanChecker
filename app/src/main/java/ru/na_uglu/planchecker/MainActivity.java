package ru.na_uglu.planchecker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
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
        registerForContextMenu(projectsList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.projects_list) {
            MenuInflater inflater = getMenuInflater();
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int projectId = (int) info.id;
            LocalData data = new LocalData(getBaseContext(), false);
            menu.setHeaderTitle(data.getProject(projectId).title);
            data.closeDataConnection();
            inflater.inflate(R.menu.project_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int projectId = (int) info.id;
        switch (item.getItemId()) {
            case R.id.context_menu_edit:
                Intent intent = new Intent(getBaseContext(), AddProject.class);
                intent.putExtra("projectId", projectId);
                startActivityForResult(intent, REQUEST_PROJECT_EDITION);
                return true;
            case R.id.context_menu_delete:
                askIfReallyDeleteProject(projectId);

                return true;
            case R.id.context_menu_done:
                askIfReallyMakeProjectDone(projectId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected void askIfReallyDoSomething(String dialogTitle, String dialogMessage, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(dialogTitle);
        dialog.setMessage(dialogMessage);
        dialog.setNegativeButton(R.string.no, listener);
        dialog.setPositiveButton(R.string.yes, listener);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void askIfReallyDeleteProject(final int projectId) {
        DialogInterface.OnClickListener wannaDelete = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        LocalData data = new LocalData(getBaseContext(), true);
                        data.deleteProject(projectId);
                        data.closeDataConnection();
                        fillTaskList();
                        break;

                }
            }
        };
        LocalData data = new LocalData(getBaseContext(), false);
        askIfReallyDoSomething(
                data.getProject(projectId).title,
                getString(R.string.delete_project_dialog),
                wannaDelete);
        data.closeDataConnection();
    }

    private void askIfReallyMakeProjectDone(final int projectId) {
        DialogInterface.OnClickListener wannaMakeProjectDone = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        LocalData data = new LocalData(getBaseContext(), true);
                        data.makeProjectDone(projectId);
                        data.closeDataConnection();
                        fillTaskList();
                        break;

                }
            }
        };
        LocalData data = new LocalData(getBaseContext(), false);
        askIfReallyDoSomething(
                data.getProject(projectId).title,
                getString(R.string.make_project_done_dialog),
                wannaMakeProjectDone);
        data.closeDataConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.info_menu_item) {
            //TODO open info activity
        } else if (id == R.id.whenhub_menu_item) {
            //TODO open whenhub for user
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
