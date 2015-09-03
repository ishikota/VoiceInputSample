package com.ikota.voiceinputsample;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    private Context mContext;
    private List<Item> mDataSet;
    private final LayoutInflater mInflater;

    public MyListAdapter(Context context, List<Item> myDataset) {
        mContext = context;
        mDataSet = myDataset;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = mInflater.inflate(R.layout.row_my_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.num.setText(String.valueOf(position));
        holder.text.setText(mDataSet.get(position).content);
        int color = mDataSet.get(position).selected ? Color.parseColor("#EEEEEE") : Color.TRANSPARENT;
        holder.parent.setBackgroundColor(color);
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.sBus.post(new MyListFragment.ClickEvent(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View parent;
        public TextView num, text;

        public ViewHolder(View v) {
            super(v);
            parent = v.findViewById(R.id.parent);
            num = (TextView)v.findViewById(R.id.num);
            text = (TextView)v.findViewById(R.id.text);
        }
    }

}
