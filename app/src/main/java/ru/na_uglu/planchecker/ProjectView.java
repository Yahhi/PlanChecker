package ru.na_uglu.planchecker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ProjectView extends AppCompatActivity {

    static final int REQUEST_PROJECT_EDITION = 565;
    static final int REQUEST_TIME_EDITION = 566;

    FragmentProjectInfo infoFragment;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_view);

        Intent receivedData = getIntent();
        projectId = receivedData.getIntExtra("projectId", 1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        LocalData data = new LocalData(this, false);
        this.setTitle(data.getProject(projectId).title);
        data.closeDataConnection();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_project_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menu_edit:
                Intent intent = new Intent(getBaseContext(), AddProject.class);
                intent.putExtra("projectId", projectId);
                startActivityForResult(intent, REQUEST_PROJECT_EDITION);
                return true;
            case R.id.menu_delete:
                askIfReallyDeleteProject(projectId);
                return true;
            case R.id.menu_done:
                askIfReallyMakeProjectDone(projectId);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void askIfReallyDoSomething(String dialogTitle, String dialogMessage, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(dialogTitle);
        dialog.setMessage(dialogMessage);
        dialog.setNegativeButton(R.string.no, null);
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

                        setResult(RESULT_OK);
                        finish();
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

                        setResult(RESULT_OK);
                        infoFragment.fillViewFromDatabase();
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            infoFragment = FragmentProjectInfo.newInstance(projectId);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return infoFragment;
                case 1:
                    return FragmentTasks.newInstance(projectId, true);
                case 2:
                    return FragmentTasks.newInstance(projectId, false);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.time_info_project);
                case 1:
                    return getString(R.string.done_info_project);
                case 2:
                    return getString(R.string.active_info_project);
            }
            return null;
        }
    }
}
