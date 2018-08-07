package com.myscrap;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.myscrap.adapters.CalendarAdapter;
import com.myscrap.adapters.WeekdayArrayAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Event;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment implements CalendarAdapter.CalendarAdapterListener{

    public GregorianCalendar month, itemMonth;
    public CalendarAdapter adapter;
    public Handler handler;
    public ArrayList<String> items;
    private TextView title;
    private TextView scheduledEvent;
    private RecyclerView.Adapter mEventUpComingAdapter;
    private List<Event.EventData> mEventDataList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private SimpleDateFormat sDFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat sDTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private Subscription getEventsSubscription;

    public EventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View eventView = inflater.inflate(R.layout.fragment_event, container, false);
        Locale.setDefault( Locale.US );
        mRecyclerView = (RecyclerView) eventView.findViewById(R.id.event_list);
        mProgressBar = (ProgressBar) eventView.findViewById(R.id.event_load);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        mRecyclerView.setLayoutManager(linearLayoutManager);
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mRecyclerView);
        mEventUpComingAdapter = new EventUpComingAdapter(mEventDataList);
        mRecyclerView.setAdapter(mEventUpComingAdapter);
        CalendarAdapter.CalendarAdapterListener listener = this;
        month = (GregorianCalendar) GregorianCalendar.getInstance();
        itemMonth = (GregorianCalendar) month.clone();

        DisplayMetrics dm = new DisplayMetrics();
        if (getActivity() != null)
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int size = dm.widthPixels / 7;

        items = new ArrayList<>();
        adapter = new CalendarAdapter(getContext(), month, size, listener);

        final FloatingActionButton fab = (FloatingActionButton) eventView.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                createEventActivity();
            else
                SnackBarDialog.showNoInternetError(fab);
        });

        RecyclerView gridViewTitle = (RecyclerView) eventView.findViewById(R.id.weekday_grid_view);
        WeekdayArrayAdapter weekdaysAdapter = getNewWeekdayAdapter(size);
        gridViewTitle.setHasFixedSize(true);
        gridViewTitle.setNestedScrollingEnabled(false);
        GridLayoutManager mLayoutManagerTitle = new GridLayoutManager(getActivity(), 7);
        gridViewTitle.setLayoutManager(mLayoutManagerTitle);
        gridViewTitle.setAdapter(weekdaysAdapter);

        RecyclerView gridView = (RecyclerView) eventView.findViewById(R.id.grid_view);
        gridView.setHasFixedSize(true);
        gridView.setNestedScrollingEnabled(false);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 7);
        gridView.setLayoutManager(mLayoutManager);
        gridView.setAdapter(adapter);

        handler = new Handler();
        handler.post(calendarUpdater);

        title = (TextView) eventView.findViewById(R.id.title);
        scheduledEvent = (TextView) eventView.findViewById(R.id.scheduled_event);
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

        RelativeLayout previous = (RelativeLayout) eventView.findViewById(R.id.previous);
        previous.setOnClickListener(v -> setPreviousMonth());

        RelativeLayout next = (RelativeLayout) eventView.findViewById(R.id.next);
        next.setOnClickListener(v -> setNextMonth());
        return eventView;
    }

    private void createEventActivity() {
            Intent i = new Intent(getContext(), EventCreateActivity.class);
            startActivity(i);
            if(getActivity() != null){
                if(CheckOsVersion.isPreLollipop()){
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            }
    }

    private void eventActivity(String eventId) {
        Intent i = new Intent(getContext(), EventActivity.class);
        i.putExtra("scrollTo", eventId);
        startActivity(i);
        if(getActivity() != null){
            if(CheckOsVersion.isPreLollipop()){
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        }
    }

    protected void setNextMonth() {
        if (month.get(GregorianCalendar.MONTH) == month
                .getActualMaximum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) + 1),
                    month.getActualMinimum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH,
                    month.get(GregorianCalendar.MONTH) + 1);
        }
        refreshCalendar();
    }

    protected void setPreviousMonth() {
        if (month.get(GregorianCalendar.MONTH) == month
                .getActualMinimum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) - 1),
                    month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH,
                    month.get(GregorianCalendar.MONTH) - 1);
        }
        refreshCalendar();
    }

    public void refreshCalendar() {
        adapter.refreshDays();
        adapter.notifyDataSetChanged();
        handler.post(calendarUpdater); // generate some calendar items
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
    }

    public Runnable calendarUpdater = new Runnable() {

        @Override
        public void run() {
            items.clear();
            // Print dates of the current week
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
            String itemvalue;
            for (int i = 0; i < 7; i++) {
                itemvalue = df.format(itemMonth.getTime());
                itemMonth.add(GregorianCalendar.DATE, 1);
            }
            splitEventData(mEventDataList);
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };

    public WeekdayArrayAdapter getNewWeekdayAdapter(int size) {
        return new WeekdayArrayAdapter(getActivity(), getDaysOfWeek(), size);
    }

    protected ArrayList<String> getDaysOfWeek() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Sun");
        list.add("Mon");
        list.add("Tue");
        list.add("Wed");
        list.add("Thu");
        list.add("Fri");
        list.add("Sat");
        return list;
    }

    private void getEvents(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(mProgressBar != null){
            mProgressBar.setVisibility(View.VISIBLE);
        }
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        getEventsSubscription = apiService.getEventList(userId,apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        if(mProgressBar != null){
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("mEventList", "onFailure");
                        if(mProgressBar != null){
                            mProgressBar.setVisibility(View.GONE);
                            showAlert();
                        }
                    }

                    @Override
                    public void onNext(Event mEventList) {
                        if(mEventList != null && !mEventList.isErrorStatus()){
                            if (mEventDataList != null)
                                mEventDataList.clear();
                            if(mEventList.getEventDataList() != null && mEventList.getEventDataList().size() > 0){
                                scheduledEvent.setVisibility(View.VISIBLE);
                                mEventDataList = mEventList.getEventDataList();
                                splitEventData(mEventDataList);
                                refreshCalendar();
                                mEventUpComingAdapter.notifyDataSetChanged();
                            } else {
                                scheduledEvent.setVisibility(View.GONE);
                                SnackBarDialog.show(mProgressBar, "No Events");
                            }
                            Log.d("mEventList", "onSuccess");
                        }
                    }
                });

    }

    private void showAlert() {
        if (mProgressBar != null) {
            Snackbar snackbar = Snackbar
                    .make(mProgressBar,  "FAILED TO LOAD", Snackbar.LENGTH_LONG)
                    .setAction("TRY AGAIN", view -> event());
            snackbar.setActionTextColor(Color.RED);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onDestroy() {
        if (getEventsSubscription != null && !getEventsSubscription.isUnsubscribed())
            getEventsSubscription.unsubscribe();
        super.onDestroy();
    }

    private void splitEventData(List<Event.EventData> mEventDataList) {
        if (mEventDataList != null && !mEventDataList.isEmpty()) {
            for (Event.EventData item : mEventDataList) {
                if (item != null) {
                    if (item.getStartDate() != null && !item.getStartDate().equalsIgnoreCase("")) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date date = formatter.parse(item.getStartDate());
                            items.add(dateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mRecyclerView != null){
            mRecyclerView.post(this::event);
        }
    }

    private void event() {
        if(CheckNetworkConnection.isConnectionAvailable(getContext())){
            getEvents();
        } else {
            SnackBarDialog.showNoInternetError(mRecyclerView);
        }
    }

    @Override
    public void onItemClickListener(int position) {

        if (position == -1)
            return;

        if (CalendarAdapter.dayString != null && CalendarAdapter.dayString.get(position) != null){
            String list = CalendarAdapter.dayString.get(position);
            if (list != null && list.length() > 0) {
                String[] separatedTime = list.split("-");
                String gridValueString = separatedTime[2].replaceFirst("^0*",
                        "");
                int gridValue = Integer.parseInt(UserUtils.parsingInteger(gridValueString));
                if ((gridValue > 10) && (position < 8)) {
                    setPreviousMonth();
                    refreshCalendar();
                } else if ((gridValue < 7) && (position > 28)) {
                    setNextMonth();
                    refreshCalendar();
                }
                if (items != null && !items.isEmpty() && items.contains(list)) {
                    if (mEventDataList != null && !mEventDataList.isEmpty()){
                        for (Event.EventData data : mEventDataList) {
                            if(data != null) {
                                if (data.getStartDate() != null && !data.getStartDate().equalsIgnoreCase("")) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    try {
                                        Date date = formatter.parse(data.getStartDate());
                                        if(dateFormat.format(date).contains(list)){
                                            eventActivity(data.getEventId());
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private class EventUpComingAdapter extends RecyclerView.Adapter<EventUpComingAdapter.ViewHolder> {


        EventUpComingAdapter(List<Event.EventData> eventDataList) {
            mEventDataList = eventDataList;
        }

        @Override
        public EventUpComingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list, parent, false);
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.width = DeviceUtils.getScreenWidth(getActivity()) - 200;
            itemView.setLayoutParams(params);
            return new EventUpComingAdapter.ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Event.EventData mData = mEventDataList.get(position);
            if(mData != null) {

                if(mData.getEventPicture() != null && !mData.getEventPicture().equalsIgnoreCase("")) {
                    Uri uri = Uri.parse(mData.getEventPicture());
                    ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setProgressiveRenderingEnabled(true)
                            .build();
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imgReq)
                            .setTapToRetryEnabled(true)
                            .setOldController(holder.eventImage.getController())
                            .build();
                    //holder.eventImage.getHierarchy().setPlaceholderImage(R.drawable.chat_heads_interstitial_map, ScalingUtils.ScaleType.CENTER_CROP);
                    holder.eventImage.getHierarchy().setPlaceholderImage(R.drawable.no_events_image_pink_blue_cover, ScalingUtils.ScaleType.CENTER_CROP);
                    holder.eventImage.setController(controller);
                    holder.eventImage.setVisibility(View.VISIBLE);
                } else {
                    Uri uri = new Uri.Builder()
                            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                            //.path(String.valueOf(R.drawable.chat_heads_interstitial_map))
                            .path(String.valueOf(R.drawable.no_events_image_pink_blue_cover))
                            .build();
                    holder.eventImage.setImageURI(uri);
                }
                holder.eventImage.setOnClickListener(v -> eventActivity(mData.getEventId()));

                holder.eventName.setOnClickListener(v -> eventActivity(mData.getEventId()));

                holder.eventTime.setOnClickListener(v -> eventActivity(mData.getEventId()));

                holder.eventPlace.setOnClickListener(v -> eventActivity(mData.getEventId()));

                holder.itemView.setOnClickListener(v -> eventActivity(mData.getEventId()));

                //String formatDate = convertToDate(mData.getStartDate());
                String formatDate = mData.getStartDate();
                //String formatStartTime = convertToTime(mData.getStartTime());
                String formatStartTime = mData.getStartTime();
                //String formatEndTime = convertToTime(mData.getEndTime());
                String formatEndTime = mData.getEndTime();
                holder.eventName.setText(mData.getEventName());
                holder.eventTime.setText(formatDate + " at " + formatStartTime+ " to "+formatEndTime);
                holder.eventPlace.setText(mData.getEventLocation());


            }
        }


        @Override
        public int getItemCount() {
            return mEventDataList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView eventDate;
            private final TextView eventName;
            private final TextView eventPlace;
            private final TextView eventTime;
            private final SimpleDraweeView eventImage;

            public ViewHolder(View view) {
                super(view);
                eventDate = (TextView) view.findViewById(R.id.date);
                eventName = (TextView) view.findViewById(R.id.event_name);
                eventPlace = (TextView) view.findViewById(R.id.event_place);
                eventTime = (TextView) view.findViewById(R.id.event_time);
                eventImage = (SimpleDraweeView) view.findViewById(R.id.event_image);
            }
        }

    }

}
