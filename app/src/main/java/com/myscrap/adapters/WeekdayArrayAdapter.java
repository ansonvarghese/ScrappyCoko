package com.myscrap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.myscrap.R;

import java.util.List;

/**
 * Customize the weekday gridview
 */
public class WeekdayArrayAdapter extends CustomRecyclerViewAdapter  {
    private LayoutInflater localInflater;
    private int columnHeight;
    private Context mContext;
    private List<String> mObjects;
    View view;

    public WeekdayArrayAdapter(Context context,
                               List<String> objects, int size) {
        this.columnHeight = size;
        this.mContext = context;
        this.mObjects = objects;
    }

    /*@Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = view;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.weekday_textview, null);
            v.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, columnHeight));

        }
        v.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, columnHeight));
        TextView textView = (TextView)v.findViewById(R.id.day);
        String item = mObjects.get(position);
        textView.setText(item);

        return v;
    }

    private LayoutInflater getLayoutInflater(Context context, int themeResource) {
        Context wrapped = new ContextThemeWrapper(context, themeResource);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.cloneInContext(wrapped);
    }
*/
    @Override
    public CustomRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext)
                .inflate(R.layout.weekday_textview, parent, false);
        view.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, columnHeight));
        return new GridHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomRecycleViewHolder holder, int position) {
        GridHolder gridHolder = (GridHolder)holder;
        if (mObjects != null){
            String item = mObjects.get(position);
            gridHolder.textView.setText(item);
        }

    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    private class GridHolder extends CustomRecycleViewHolder {
        TextView textView;
        public GridHolder(View view) {
            super(view);
            textView = (TextView)view.findViewById(R.id.day);
        }
    }
}
