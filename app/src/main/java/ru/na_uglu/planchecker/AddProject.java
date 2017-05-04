package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class AddProject extends AppCompatActivity {

    private EditText projectTitle;
    private EditText projectComment;

    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectTitle = (EditText) findViewById(R.id.project_title_editable);
        projectComment = (EditText) findViewById(R.id.project_comment_editable);

        Intent receivedData = getIntent();
        projectId = receivedData.getIntExtra("projectId", 0);
        if (projectId > 0) {
            LocalData data = new LocalData(this, false);
            Project project = data.getProject(projectId);
            projectTitle.setText(project.title);
            projectComment.setText(project.comment);
            data.closeDataConnection();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_menuitem) {
            LocalData data = new LocalData(this, true);
            data.saveProject(projectId,
                    projectTitle.getText().toString(),
                    projectComment.getText().toString());
            data.closeDataConnection();

            setResult(Activity.RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
