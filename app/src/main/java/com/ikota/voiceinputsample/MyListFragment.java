package com.ikota.voiceinputsample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;


public class MyListFragment extends Fragment{

    private Context mAppContext;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseActivity.sBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BaseActivity.sBus.unregister(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAppContext = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_list, container, false);

        mRecyclerView = (RecyclerView) root.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mAppContext);
        mRecyclerView.setLayoutManager(layoutManager);

        ArrayList<Item> items = new ArrayList<>();
        for(int i=0;i<20;i++) {
            items.add(new Item("Item at position "+i));
        }
        mRecyclerView.setAdapter(new MyListAdapter(mAppContext, items));

        return root;
    }

    @Subscribe
    public void onReceivedVoiceCommand(VoiceEvent ev) {
        try {
            int position = Integer.valueOf(ev.query);
            //mRecyclerView.scrollToPosition(position);
            mRecyclerView.scrollBy(0,100);
        } catch (NumberFormatException e) {
            Log.i("VoiceCommand", "Received command : "+ev.query);
        }
    }

    @Subscribe
    public void onItemClicked(ClickEvent ev) {
        Toast.makeText(getActivity(),
                String.format("Item at %d is clidked !!", ev.position),
                Toast.LENGTH_SHORT).show();
    }

    public static class VoiceEvent {
        String query;
        public VoiceEvent(String query) {
            this.query = query;
        }
    }

    public static class ClickEvent {
        public final int position;
        public ClickEvent(int position) {
            this.position = position;
        }
    }

}
