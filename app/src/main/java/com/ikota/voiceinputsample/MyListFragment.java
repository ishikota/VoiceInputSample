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

    // Voice Command Keywords
    private static final ArrayList<String> UP_WORDS = new ArrayList<String>() {
        {add("up");}
        {add("above");}
        {add("before");}
    };

    private static final ArrayList<String> DOWN_WORDS = new ArrayList<String>() {
        {add("down");}
        {add("below");}
        {add("next");}
    };

    private static final ArrayList<String> TOP_WORDS = new ArrayList<String>() {
        {add("top");}
        {add("first");}
    };

    private static final ArrayList<String> BOTTOM_WORDS = new ArrayList<String>() {
        {add("bottom");}
        {add("last");}
    };


    private Context mAppContext;
    private RecyclerView mRecyclerView;
    private ArrayList<Item> mItemList;

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

        mItemList = new ArrayList<>();
        for(int i=0;i<20;i++) {
            mItemList.add(new Item("Item at position "+i));
        }

        mRecyclerView.setAdapter(new MyListAdapter(mAppContext, mItemList));

        return root;
    }

    @Subscribe
    public void onReceivedVoiceCommand(VoiceEvent ev) {
        String[] res = ev.query.split(" ");
        int row_height = (int)mAppContext.getResources().getDimension(R.dimen.row_height);
        for(String q : res) {
            q = q.toLowerCase();
            if (UP_WORDS.contains(q)) {
                mRecyclerView.scrollBy(0, -row_height);
            } else if (DOWN_WORDS.contains(q)) {
                mRecyclerView.scrollBy(0, row_height);
            } else if(TOP_WORDS.contains(q)) {
                mRecyclerView.scrollToPosition(0);
            } else if(BOTTOM_WORDS.contains(q)) {
                mRecyclerView.scrollToPosition(mItemList.size()-1);
            } else {
                try {
                    int position = Integer.valueOf(q);
                    mRecyclerView.scrollToPosition(position);
                } catch (NumberFormatException e) {
                    Log.d("Receive Command", e.toString());
                }
            }
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
