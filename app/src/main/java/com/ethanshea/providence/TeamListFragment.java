package com.ethanshea.providence;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

public class TeamListFragment extends RecyclerListFragment {

    public TeamListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        setRecyclerAdapter(new TeamListAdapter());
        return v;
    }


    private class TeamListAdapter extends RecyclerListFragment.JsonArrayAdapter {
        public TeamListAdapter() {
            super(MainActivity.database.optJSONArray("teams"));
        }

        @Override
        public JsonObjectViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_team_list_item, parent, false);
            TeamViewHolder vh = new TeamViewHolder(v);
            return vh;
        }

        public void itemSelected(int pos) {
            openTeam(pos);
        }

        public class TeamViewHolder extends JsonObjectViewHolder {
            private TextView name;

            public TeamViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.teamList_name);
            }

            public void adjust(JSONObject o) {
                name.setText(o.optString("number") + "- " + o.optString("name"));
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
