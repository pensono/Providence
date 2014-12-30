package com.ethanshea.providence;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MatchListFragment extends RecyclerListFragment {
    public MatchListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        setRecyclerAdapter(new MatchListAdapter());
        return v;
    }


    private class MatchListAdapter extends RecyclerListFragment.JsonArrayAdapter {
        public MatchListAdapter() {
            super(MainActivity.database.optJSONArray("matches"));
        }

        @Override
        public JsonObjectViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_match_list_item, parent, false);
            MatchViewHolder vh = new MatchViewHolder(v);
            return vh;
        }

        public void itemSelected(int pos) {
            openTeam(pos);
        }

        public class MatchViewHolder extends JsonObjectViewHolder {
            private TextView title;
            private TextView redTeams;
            private TextView blueTeams;

            public MatchViewHolder(View v) {
                super(v);
                title = (TextView) v.findViewById(R.id.matchList_title);
                redTeams = (TextView) v.findViewById(R.id.matchList_redTeams);
                blueTeams = (TextView) v.findViewById(R.id.matchList_blueTeams);
            }

            public void adjust(JSONObject o) {
                title.setText("Match #" + o.optInt("number", 0));
                redTeams.setText(TextUtils.join(", ", mkArray(o.optJSONArray("red"))));
                blueTeams.setText(TextUtils.join(", ", mkArray(o.optJSONArray("blue"))));
            }

            private String[] mkArray(JSONArray array) {
                String[] result = new String[array.length()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = array.optString(i);
                }
                return result;
            }
        }
    }

    public static String TEAM_INDEX = "team index";

    private void openTeam(int teamIndex) {
        Intent intent = new Intent(getActivity(), ViewTeamActivity.class);
        intent.putExtra(TEAM_INDEX, teamIndex);
        startActivity(intent);
    }
}
