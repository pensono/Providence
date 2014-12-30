package com.ethanshea.providence;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class MainActivity extends FragmentActivity {
    public static final String LOG_TAG = "Providence";

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager pager;

    public static JSONObject database;
    private final String DB_FILENAME = "providenceData.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load our data
        File dbFile = new File(DB_FILENAME);
        database = dbFile.exists() ? loadJson(dbFile) : makeDb();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), database);

        // Set up the ViewPager with the sections adapter.
        pager = (ViewPager) findViewById(R.id.main_pager);
        pager.setAdapter(mSectionsPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.main_tabs);
        tabs.setViewPager(pager);

        //Change the title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Providence");
    }

    private JSONObject makeDb() {
        Log.i(MainActivity.LOG_TAG, "Making a new database");
        try {
            return new JSONObject(getResources().getString(R.string.database_template));
        } catch (JSONException e) {
            Log.e(MainActivity.LOG_TAG, "Error creating JSON database", e);
        }
        return new JSONObject();
    }

    private JSONObject loadJson(File dbFile) {
        Log.i(MainActivity.LOG_TAG, "Loading database");
        try {
            FileInputStream fis = new FileInputStream(dbFile);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            return new JSONObject(new String(buffer, "UTF-8"));
        } catch (FileNotFoundException e) {
            Log.e(MainActivity.LOG_TAG, "Database file not found. Path:" + dbFile.getAbsolutePath(), e);
        } catch (UnsupportedEncodingException e) {
            Log.e(MainActivity.LOG_TAG, "Unsupported encoding while reading the database file.", e);
        } catch (JSONException e) {
            Log.e(MainActivity.LOG_TAG, "Json exception while reading the database file. Did you modify the file by hand? if so, you may have messed up the formatting somehow", e);
        } catch (IOException e) {
            Log.e(MainActivity.LOG_TAG, "IO exception while reading the database file.", e);
        }
        return makeDb();
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private JSONObject database;

        public SectionsPagerAdapter(FragmentManager fm, JSONObject db) {
            super(fm);
            database = db;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TeamListFragment();
                case 1:
                    return new MatchListFragment();
                default:
            }
            Log.e(MainActivity.LOG_TAG, "Somebody has managed to scroll to an invalid position in MainActivity");
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
            }
            return null;
        }
    }
}
