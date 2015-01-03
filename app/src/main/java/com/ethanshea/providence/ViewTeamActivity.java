package com.ethanshea.providence;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class ViewTeamActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final String TEAM_INDEX = "team index";
    //Used for taking images
    private static final String IMAGE_FULL_PATH = "image full path";
    private static final String IMAGE_NAME = "image name";


    private JSONObject teamData;
    private int teamIndex; //Depricate this
    private LinearLayout attributes;
    private ImageView imageView;

    private Uri imageUri;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_team);
        overridePendingTransition(R.anim.slide_in, R.anim.still);

        int pos;
        if (savedInstanceState != null) {
            pos = savedInstanceState.getInt(TEAM_INDEX);
            String uri = savedInstanceState.getString(IMAGE_FULL_PATH);
            if (uri != null)
                imageUri = Uri.parse(uri);
        } else {
            pos = getIntent().getIntExtra(TeamListFragment.TEAM_INDEX, 0);
            teamIndex = pos;
        }

        Log.i(MainActivity.LOG_TAG, "Opening up team at index:" + pos);
        try {
            teamData = MainActivity.database.getJSONArray("teams").getJSONObject(pos);
        } catch (JSONException e) {
            teamData = new JSONObject();
            Log.e(MainActivity.LOG_TAG, "Could not find the team with index:" + pos, e);
        }

        //Set up the image
        imageView = (ImageView) findViewById(R.id.viewTeam_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        if (teamData.has("image")) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(getImageDir() + File.separator + teamData.optString("image")));
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

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            String timeStamp = new SimpleDateFormat("dd_HHmmss").format(new Date());
            imageName = teamData.optString("number", "000") + "_" + timeStamp + ".jpg";
            File imageFile = new File(getImageDir(), imageName);
            imageUri = Uri.fromFile(imageFile);
            try {
                imageFile.createNewFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, "Error creating the new team image file. Path: " + imageUri, e);
            }

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File getImageDir() {return getExternalFilesDir(Environment.DIRECTORY_PICTURES);}

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                teamData.put("image", imageName);
            } catch (JSONException e) {
                Log.e(MainActivity.LOG_TAG, "Error adding the image attribute to the team data", e);
            }

            try {
                //Scale the image to 720 by whatever (most likely 1280 or 720 depending on the aspect ratio.)
                InputStream is = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();

                imageView.setImageBitmap(bitmap);
                //We might as well keep the original for right after the image is taken.
                //Create a new one to scale and save
                float ratio = bitmap.getHeight() / bitmap.getWidth();
                Bitmap saveBitmap = Bitmap.createScaledBitmap(bitmap, 720, (int) (ratio * 720f), true);
                OutputStream os = getContentResolver().openOutputStream(imageUri);
                saveBitmap.compress(Bitmap.CompressFormat.JPEG, 60, os);
                os.close();
            } catch (FileNotFoundException e) {
                Log.e(MainActivity.LOG_TAG, "Error creating or resizing team image file. Looking at:" + imageUri.getPath(), e);
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, "Error closing or using some file stream", e);
            }
            Log.i(MainActivity.LOG_TAG, "Image saved at: " + imageUri.getPath());
        }
    }

    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.still, R.anim.slide_out);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(IMAGE_FULL_PATH, imageUri);
        outState.putInt(TEAM_INDEX, teamIndex);
        outState.putString(IMAGE_NAME, imageName);
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
                attributes.remove("image");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup attributeView = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_new_attribute, null);
                        ViewGroup chips = (ViewGroup) attributeView.findViewById(R.id.addAttribute_chips);
                        final EditText attributeName = (EditText) attributeView.findViewById(R.id.addAttribute_name);

                        if (attributes.size() == 0) {
                            attributeView.removeView(chips);
                        } else {
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
                        }

                        final Dialog dialog = new Dialog(ViewTeamActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(attributeView);
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
                                name = name.trim();
                                dialog.dismiss();

                                if (name.isEmpty()) return;
                                if (teamData.has(name)) {
                                    //The user tried to add an attribute that's already there. Just ignore it for now
                                    //TODO Later, use inline error correction in the text field, and disable the add button ot stop duplicates
                                    // orrr... close the dialog and move focus to the attribute they typed in. Decisions, decisions.
                                    return;
                                }

                                try {
                                    teamData.put(name, "");
                                    addAttributeUI(name, true);
                                } catch (JSONException e) {
                                    Log.e(MainActivity.LOG_TAG, "Something strange happened when adding an attribute to the team", e);
                                }
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
