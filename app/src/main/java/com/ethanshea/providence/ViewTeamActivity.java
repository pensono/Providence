package com.ethanshea.providence;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class ViewTeamActivity extends Activity {

    private RecyclerView recyclerView;
    private JSONObject teamData;
    private LinearLayout attributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_team);
        overridePendingTransition(R.anim.slide_in, R.anim.still);

        int pos = getIntent().getIntExtra(TeamListFragment.TEAM_INDEX, 0);

        Log.i(MainActivity.LOG_TAG, "Opening up team at index:" + pos);
        try {
            teamData = MainActivity.database.getJSONArray("teams").getJSONObject(pos);
        } catch (JSONException e) {
            teamData = new JSONObject();
            Log.e(MainActivity.LOG_TAG, "Could not find the team with index:" + pos, e);
        }

        //Set up the team attributes list
        attributes = (LinearLayout) findViewById(R.id.viewTeam_attributes);
        Log.d(MainActivity.LOG_TAG, teamData.toString());
//        attributes.removeAllViews();
        Iterator<String> keys = teamData.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if ((key.equals("name")) || (key.equals("number"))) continue;
            addAttributeUI(key);
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

    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.still, R.anim.slide_out);
    }

    private void addAttributeUI(final String key) {
        addAttributeUI(key, false);
    }

    private void addAttributeUI(final String key, boolean showKeyboard) {
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
        if (showKeyboard) {
            showKeyboard(valueField);
        }
    }

    private void showKeyboard(final EditText editText) {
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, 0);
            }
        }, 10);
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
        Log.d(MainActivity.LOG_TAG, "Showing new attribute dialog...");
        //Switch to AsyncTask if this takes too long
        new Thread(new Runnable() {
            public void run() {
                //Generate a list of all of the attributes used
                //Use a tree set so we can have our chips alphebetized
                final Set<String> attributes = new TreeSet<>();
                JSONArray teams = MainActivity.database.optJSONArray("teams");
                Iterator<String> keys;
                for (int i = 0; i < teams.length(); i++) {
                    keys = teams.optJSONObject(i).keys();
                    while (keys.hasNext()) {
                        attributes.add(keys.next());
                    }
                }
                //Don't show the user any attribute suggestions already added to this team
                Iterator<String> attrs = teamData.keys();
                while (attrs.hasNext()) {
                    attributes.remove(attrs.next());
                }
//                attributes.remove("name");
//                attributes.remove("number");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View attributeView = getLayoutInflater().inflate(R.layout.dialog_new_attribute, null);
                        ViewGroup chips = (ViewGroup) attributeView.findViewById(R.id.addAttribute_chips);
                        final EditText attributeName = (EditText) attributeView.findViewById(R.id.addAttribute_name);

                        for (final String attr : attributes) {
                            TextView chip = (TextView) getLayoutInflater().inflate(R.layout.chip_basic, null);
                            chip.setText(attr);
                            chip.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    attributeName.setText(attr);
                                    //There's a possible chance to get the text from the view, but I don't think it's worth it
                                }
                            });
                            chips.addView(chip);
                        }

                        final Dialog dialog = new Dialog(ViewTeamActivity.this);
                        dialog.setContentView(attributeView);
                        dialog.setTitle(R.string.add_attribute);
                        dialog.setCancelable(true);

                        attributeView.findViewById(R.id.addAttribute_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        attributeView.findViewById(R.id.addAttribute_add).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String name = attributeName.getText().toString();
                                try {
                                    teamData.put(name, "");
                                } catch (JSONException e) {
                                    Log.e(MainActivity.LOG_TAG, "Something strange happened when adding an attribute to the team", e);
                                }
                                dialog.dismiss();
                                addAttributeUI(name, true);
                            }
                        });
                        dialog.show();
                        showKeyboard(attributeName);
                    }
                });
            }
        }).start();
    }
}
