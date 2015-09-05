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
import android.widget.TextView;
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

    // text displayed in list
    private static final String[] TEXTS = {
            "There is always light behind the clouds.",
            "Change before you have to.",
            "If you can dream it, you can do it.",
            "Love the life you live. Live the life you love.",
            "My life didn’t please me, so I created my life.",
            "It always seems impossible until it’s done.",
            "Peace begins with a smile.",
            "Love dies only when growth stops.",
            "There is more to life than increasing its speed.",
            "Everything is practice.",
            "If you want to be happy, be.",
            "Without haste, but without rest.",
            "You’ll never find a rainbow if you’re looking down.",
            "Indecision is often worse than wrong action.",
            "He who has never hoped can never despair.",
            "I will prepare and some day my chance will come.",
            "Do one thing everyday that scares you.",
            "He liked to like people, therefore people liked him.",
            "The only way to have a friend is to be one.",
            "Move fast and break things. "
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

        if(mItemList == null) {  // else orientation change occurred
            mItemList = new ArrayList<>();
            for (String test : TEXTS) {
                mItemList.add(new Item(test));
            }
            mCurrentPos = 0;
            mItemList.get(mCurrentPos).selected = true;
        }

        mRecyclerView.setAdapter(new MyListAdapter(mAppContext, mItemList));

        return root;
    }


    @Subscribe
    public void onReceivedVoiceCommand(VoiceEvent ev) {
        String[] res = ev.query.split(" ");
        String q = res[0].toLowerCase();

        if (UP_WORDS.contains(q)) {
            BaseActivity.sBus.post(new MyListActivity.CAEvent(true));
            if(mCurrentPos==0) return;
            unselectView(mCurrentPos);
            mRecyclerView.scrollToPosition(mCurrentPos - 1);
            delaySelectView(mCurrentPos - 1);
        } else if (DOWN_WORDS.contains(q)) {
            BaseActivity.sBus.post(new MyListActivity.CAEvent(true));
            if(mCurrentPos==mItemList.size()-1) return;
            unselectView(mCurrentPos);
            mRecyclerView.scrollToPosition(mCurrentPos + 1);
            delaySelectView(mCurrentPos + 1);
        } else if(TOP_WORDS.contains(q)) {
            unselectView(mCurrentPos);
            mRecyclerView.scrollToPosition(0);
            delaySelectView(0);
            BaseActivity.sBus.post(new MyListActivity.CAEvent(true));
        } else if(BOTTOM_WORDS.contains(q)) {
            BaseActivity.sBus.post(new MyListActivity.CAEvent(true));
            unselectView(mCurrentPos);
            mRecyclerView.scrollToPosition(mItemList.size() - 1);
            delaySelectView(mItemList.size() - 1);
        } else {
            try {
                int position = Integer.valueOf(q);
                unselectView(mCurrentPos);
                mRecyclerView.scrollToPosition(position);
                delaySelectView(position);
                BaseActivity.sBus.post(new MyListActivity.CAEvent(true));
            } catch (NumberFormatException e) {
                Log.d("Receive Command", e.toString());
                BaseActivity.sBus.post(new MyListActivity.CAEvent(false));
            }
        }
    }

    public static class VoiceEvent {
        String query;
        public VoiceEvent(String query) {
            this.query = query;
        }
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

    public static class ClickEvent {
        public final int position;
        public ClickEvent(int position) {
            this.position = position;
        }
    }


    private void selectView(int position) {
        if(position < 0 || mItemList.size() <= position) {
            Log.i("selectView", "OutOfBounds:"+position);
            return;
        }
        Log.i("selectView", "selectView is called with position:"+position);
        mCurrentPos = position;
        mItemList.get(mCurrentPos).selected = true;
        View target = getChildAtPosition(position);
        if(target != null) {
            target.findViewById(R.id.parent).setBackgroundColor(Color.parseColor("#EEEEEE"));
            String message = ((TextView)target.findViewById(R.id.text)).getText().toString();
            BaseActivity.sBus.post(new MyListActivity.SpeechEvent(message));
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
        }, 500);  // wait scroll
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
     * This method helps you to get View from adapter position.
     * But if target View is not displayed, it's impossible .
     * So you need to scroll and display target view before calling this method.
     *
     * @param adapter_pos index of target item in ArrayList
     * @return target view
     */
    private View getChildAtPosition(int adapter_pos) {
        int list_pos = getChildDisplayPosition(adapter_pos);
        if(list_pos >= 0) {
            return mRecyclerView.getChildAt(list_pos);
        } else {
            Log.e("getChildAtPosition", "Target View is Not displayed ");
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

}
