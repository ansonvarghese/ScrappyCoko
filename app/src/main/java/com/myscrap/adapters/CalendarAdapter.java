package com.myscrap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.myscrap.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends CustomRecyclerViewAdapter {
	private final GregorianCalendar selectedDate;
	private Context mContext;
    public CalendarAdapter.CalendarAdapterListener mCalendarAdapterListener;
	private static java.util.Calendar month;
	private GregorianCalendar previousMonth; // calendar instance for previous month
    private int firstDay;
	private String currentDateString;
	private DateFormat df;

	private ArrayList<String> items;
	public static List<String> dayString;
	private View previousView;
    private int columnHeight;
	private String itemValue;
	private int maxWeekNumber;
	private int monthLength;
	private int maxP;
	private int calMaxP;
	private GregorianCalendar pMonthMaxSet;

    View view;

    public CalendarAdapter(Context c, GregorianCalendar monthCalendar, int size, CalendarAdapter.CalendarAdapterListener calendarAdapterListener) {
		dayString = new ArrayList<>();
		 Locale.setDefault( Locale.US );
		month = monthCalendar;
        columnHeight = size;
		selectedDate = (GregorianCalendar) monthCalendar.clone();
		mContext = c;
		month.set(GregorianCalendar.DAY_OF_MONTH, 1);
		this.items = new ArrayList<>();
		this.mCalendarAdapterListener = calendarAdapterListener;
		df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		currentDateString = df.format(selectedDate.getTime());
		refreshDays();

	}

	public void setItems(ArrayList<String> items) {
		if(items == null)
			return;
		for (int i = 0; i != items.size(); i++) {
			if (items.get(i).length() == 1) {
				items.set(i, "0" + items.get(i));
			}
		}
		this.items = items;
	}

	public int getCount() {
		return dayString.size();
	}

	public Object getItem(int position) {
		return dayString.get(position);
	}

	@Override
	public CustomRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext)
                .inflate(R.layout.calendar_item, parent, false);
        view.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, columnHeight));
        return new GridHolder(view);
	}

	@Override
	public void onBindViewHolder(CustomRecycleViewHolder holder, int position) {
          GridHolder gridHolder = (GridHolder)holder;
        String[] separatedTime = dayString.get(position).split("-");
        String gridValue = separatedTime[2].replaceFirst("^0*", "");
        if ((Integer.parseInt(gridValue) > 1) && (position < firstDay)) {
            gridHolder.dayView.setTextColor(Color.WHITE);
            gridHolder.dayView.setClickable(false);
            gridHolder.dayView.setFocusable(false);
        } else if ((Integer.parseInt(gridValue) < 7) && (position > 28)) {
            gridHolder.dayView.setTextColor(Color.WHITE);
            gridHolder.dayView.setClickable(false);
            gridHolder.dayView.setFocusable(false);
        } else {
            gridHolder.dayView.setTextColor(Color.parseColor("#232223"));
        }


        gridHolder.dayView.setText(gridValue);

        String date = dayString.get(position);

        if (date.length() == 1) {
            date = "0" + date;
        }

        if (dayString.get(position).equals(currentDateString)) {
            setSelected(view);
            previousView = view;
        } else {
            view.setBackgroundResource(R.drawable.list_item_background);
        }

        if (date.length() > 0 && items != null && items.contains(date)) {
            view.setBackgroundResource(R.drawable.calendar_cel_selected);
            gridHolder.dayView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        } else {
            if (dayString.get(position).equals(currentDateString)) {
                setSelected(view);
                previousView = view;
            } else {
                view.setBackgroundResource(R.drawable.list_item_background);
            }
            if ((Integer.parseInt(gridValue) > 1) && (position < firstDay)) {
                gridHolder.dayView.setTextColor(Color.WHITE);
                gridHolder.dayView.setClickable(false);
                gridHolder.dayView.setFocusable(false);
            } else if ((Integer.parseInt(gridValue) < 7) && (position > 28)) {
                gridHolder.dayView.setTextColor(Color.WHITE);
                gridHolder.dayView.setClickable(false);
                gridHolder.dayView.setFocusable(false);
            } else {
                gridHolder.dayView.setTextColor(Color.parseColor("#232223"));
            }
        }

        gridHolder.dayView.setOnClickListener(v -> {
            if(dayString != null && isValidPos(gridHolder.getAdapterPosition())) {
                mCalendarAdapterListener.onItemClickListener(gridHolder.getAdapterPosition());
            } else {
                Log.e("Position ", "INVALID " + gridHolder.getAdapterPosition());
            }
        }
		);
        gridHolder.itemView.setOnClickListener(v -> {
            if(dayString != null && isValidPos(gridHolder.getAdapterPosition())) {
                mCalendarAdapterListener.onItemClickListener(gridHolder.getAdapterPosition());
            }  else {
                Log.e("Position ", "INVALID " + gridHolder.getAdapterPosition());
            }
        } );

	}

	private boolean isValidPos(int position){
		return position >= 0 && position < dayString.size();
	}

	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getItemCount() {
		return dayString.size();
	}

	private void setSelected(View view) {
		if (previousView != null) {
			previousView.setBackgroundResource(R.drawable.list_item_background);
		}
		previousView = view;
		view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.calendar_cell_select));
	}

	public void refreshDays() {
		items.clear();
		dayString.clear();
		Locale.setDefault( Locale.US );
		previousMonth = (GregorianCalendar) month.clone();
		firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
		// finding number of weeks in current month.
		maxWeekNumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
		// allocating maximum row number for the gridview.
		monthLength = maxWeekNumber * 7;
		maxP = getMaxP();
		calMaxP = maxP - (firstDay - 1);
		/**
		 * Calendar instance for getting a complete gridview including the three
		 * month's (previous,current,next) dates.
		 */
		/* calendar instance for previous month for getting complete view */
		pMonthMaxSet = (GregorianCalendar) previousMonth.clone();
		/**
		 * setting the start date as previous month's required date.
		 */
		pMonthMaxSet.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);

		/**
		 * filling calendar gridview.
		 */
		for (int n = 0; n < monthLength; n++) {
			itemValue = df.format(pMonthMaxSet.getTime());
			pMonthMaxSet.add(GregorianCalendar.DATE, 1);
			dayString.add(itemValue);
		}
	}

	private int getMaxP() {
		int maxP;
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMinimum(GregorianCalendar.MONTH)) {
			previousMonth.set((month.get(GregorianCalendar.YEAR) - 1),
					month.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			previousMonth.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) - 1);
		}
		maxP = previousMonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		return maxP;
	}

    private class GridHolder extends CustomRecycleViewHolder {
        TextView dayView;
	    public GridHolder(View view) {
            super(view);
            dayView = (TextView) view.findViewById(R.id.date);
        }
    }

    public interface CalendarAdapterListener {
        void onItemClickListener(int position);
    }
}