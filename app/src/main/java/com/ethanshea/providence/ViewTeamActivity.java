package com.ethanshea.providence;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class ViewTeamActivity extends Activity {

    private RecyclerView recyclerView;
    private JSONObject teamData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_team);

        int pos = getIntent().getIntExtra(TeamListFragment.TEAM_INDEX, 0);

        Log.i(MainActivity.LOG_TAG, "Opening up team at index:" + pos);
        try {
            teamData = MainActivity.database.getJSONArray("teams").getJSONObject(pos);
        } catch (JSONException e) {
            teamData = new JSONObject();
            Log.e(MainActivity.LOG_TAG, "Could not find the team with index:" + pos, e);
        }

        //Set up the team attributes list
        LinearLayout attributes = (LinearLayout) findViewById(R.id.viewTeam_attributes);
        Log.d(MainActivity.LOG_TAG, teamData.toString());
//        attributes.removeAllViews();
        Iterator<String> keys = teamData.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if ((key.equals("name")) || (key.equals("number"))) continue;
            int id = key.hashCode();
            Log.d(MainActivity.LOG_TAG, key);
            View attribute = getLayoutInflater().inflate(R.layout.layout_team_attribute, attributes, true);

            TextView name = (TextView) attribute.findViewById(R.id.teamAttribute_name);
            name.setText(key);
            name.setId(id);

            EditText valueField = (EditText) attribute.findViewById(R.id.teamAttribute_value);
            valueField.setId(id);
            valueField.setText(teamData.optString(key, "Value not found"));
            valueField.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        teamData.put(key, s.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(teamData.optInt("number", 0) + " " + teamData.optString("name", "No name found..."));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_team, menu);
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

    public void addAttribute(View view) {

    }
}
