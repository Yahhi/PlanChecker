package ru.na_uglu.planchecker;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class AddTime extends AppCompatActivity {

    Spinner projectSelection;
    Spinner taskSelection;
    EditText time;

    MenuItem saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_time);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectSelection = (Spinner) findViewById(R.id.project_selection);
        taskSelection = (Spinner) findViewById(R.id.task_selection);
        taskSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        time = (EditText) findViewById(R.id.time_to_add);

        LocalData data = new LocalData(this, false);
        String[] projectTitles = data.getProjectTitles();
        data.closeDataConnection();
        projectSelection.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projectTitles));
        projectSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocalData localData = new LocalData(getBaseContext(), false);
                String[] taskTitles = localData.getTaskTitles(parent.getItemAtPosition(position).toString());
                taskSelection.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, taskTitles));
                localData.closeDataConnection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        saveButton = menu.getItem(0);
        saveButton.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_menuitem) {
            LocalData data = new LocalData(this, true);
            data.addTimeToTask(data.getTaskIdFromTitle(taskSelection.getSelectedItem().toString()),
                    data.convertEnteredTimeToInt(time.getText().toString()));
            data.closeDataConnection();

            setResult(Activity.RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
