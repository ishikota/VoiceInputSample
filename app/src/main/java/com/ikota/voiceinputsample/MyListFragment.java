package com.ikota.voiceinputsample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
        {add("back");}
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

    private int mCurrentPos = 0;

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
        mCurrentPos = 0;
        mItemList.get(mCurrentPos).selected = true;

        mRecyclerView.setAdapter(new MyListAdapter(mAppContext, mItemList));

        return root;
    }

    @Subscribe
    public void onReceivedVoiceCommand(VoiceEvent ev) {
        String[] res = ev.query.split(" ");
        for(String q : res) {
            q = q.toLowerCase();
            if (UP_WORDS.contains(q)) {
                if(mCurrentPos==0) return;
                unselectView(mCurrentPos);
                mRecyclerView.scrollToPosition(mCurrentPos-1);
                delaySelectView(mCurrentPos-1);
            } else if (DOWN_WORDS.contains(q)) {
                if(mCurrentPos==mItemList.size()-1) return;
                unselectView(mCurrentPos);
                mRecyclerView.scrollToPosition(mCurrentPos+1);
                delaySelectView(mCurrentPos+1);
            } else if(TOP_WORDS.contains(q)) {
                unselectView(mCurrentPos);
                mRecyclerView.scrollToPosition(0);
                delaySelectView(0);
            } else if(BOTTOM_WORDS.contains(q)) {
                unselectView(mCurrentPos);
                mRecyclerView.scrollToPosition(mItemList.size() - 1);
                delaySelectView(mItemList.size() - 1);

            } else {
                try {
                    int position = Integer.valueOf(q);
                    unselectView(mCurrentPos);
                    mRecyclerView.scrollToPosition(position);
                    delaySelectView(position);
                } catch (NumberFormatException e) {
                    Log.d("Receive Command", e.toString());
                }
            }
        }
    }

    private void selectView(int position) {
        if(position < 0 || mItemList.size() <= position) {
            Log.i("selectView", "OutOfBounds:"+position);
            return;
        }
        mCurrentPos = position;
        mItemList.get(mCurrentPos).selected = true;
        View target = getChildAtPosition(position);
        if(target != null) {
            target.findViewById(R.id.parent).setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else {
            Log.e("delaySelectView", "nullpo on position "+position);
            Toast.makeText(getActivity(), "Sorry, Error Occurred.", Toast.LENGTH_SHORT).show();
            throw new IllegalStateException("delaySelectView : nullpo on position "+position);
        }
    }

    private void delaySelectView(final int position) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectView(position);
            }
        }, 500);
    }

    private void unselectView(int position) {
        if(position < 0 || mItemList.size() <= position) {
            Log.i("selectView", "OutOfBounds:"+position);
            return;
        }
        mItemList.get(position).selected = false;
        View target = getChildAtPosition(position);
        if(target != null) {
            target.findViewById(R.id.parent).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * When you try to retrieve View of 20-th item you cannot use RecyclerView.getChildAt(position).
     * Because RecyclerView.getChildAt(20) causes nullpo.
     * The reason is it tries to access view which is not displayed.
     *
     * So this method scrolls RecyclerView until target view is displayed
     * and returns reference of target view.
     *
     * @param adapter_pos index of target item in ArrayList
     * @return target view
     */
    private View getChildAtPosition(int adapter_pos) {
        int list_pos = getChildDisplayPosition(adapter_pos);
        if(list_pos >= 0) {
            return mRecyclerView.getChildAt(list_pos);
        } else {
            Log.i("getChildAtPosition", "Not displayed so scroll");
            return null;
        }
    }

    private int getChildDisplayPosition(int adapter_pos) {
        int child_num = mRecyclerView.getChildCount();
        for(int i=0;i<child_num;i++) {
            View child = mRecyclerView.getChildAt(i);
            int pos = mRecyclerView.getChildAdapterPosition(child);
            Log.i("getChildDisplayPosition", String.format("adapter pos = %d, rv pos = %d", pos, i));
            if (pos == adapter_pos) {
                Log.i("getChildDisplayPosition", String.format("target item %d is %d th item", adapter_pos, i));
                return i;
            }
        }
        Log.i("getChildDisplayPosition", String.format("target item %d is not displayed", adapter_pos));
        return -1;
    }

    @Subscribe
    public void onItemClicked(ClickEvent ev) {
        unselectView(mCurrentPos);
        mCurrentPos = ev.position;
        selectView(mCurrentPos);
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
