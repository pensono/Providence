package com.ethanshea.providence;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class RecyclerListFragment extends Fragment {

    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecyclerListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, 1, getResources().getColor(R.color.dividerColor)));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    public void setRecyclerAdapter(RecyclerView.Adapter adapt) {
        recyclerView.setAdapter(adapt);
    }


    public abstract class JsonArrayAdapter extends RecyclerView.Adapter<JsonArrayAdapter.JsonObjectViewHolder> {
        private JSONArray data;

        public JsonArrayAdapter(JSONArray array) {
            data = array;
        }

        /**
         * Override this to get updates when an item is clicked.
         *
         * @param pos
         */
        public void itemSelected(int pos) {
        }

        @Override
        public void onBindViewHolder(JsonObjectViewHolder holder, int position) {
            try {
                holder.adjust(data.getJSONObject(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return data.length();
        }

        public abstract class JsonObjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public JsonObjectViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
            }

            public void onClick(View v) {
                int pos = recyclerView.getChildPosition(v);
                itemSelected(pos);
            }

            public abstract void adjust(JSONObject o);
        }
    }
}
