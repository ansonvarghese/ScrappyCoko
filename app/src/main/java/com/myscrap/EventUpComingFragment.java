package com.myscrap;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
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
import java.util.List;
import java.util.Locale;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventUpComingFragment extends Fragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    //private TextView noUpcomingEvent;
    private EventUpComingAdapter mEventUpComingAdapter;
    private SimpleDateFormat sDFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat sDTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private List<Event.EventData> mEventDataList = new ArrayList<>();
    private String mEventId;
    private View emptyView;
    private Subscription getEventsSubscription;
    int currentScrollPosition = 0;
    public EventUpComingFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_up_coming, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
        emptyView = v.findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_no_event, "No Upcoming Events", true);
        //noUpcomingEvent = (TextView) v.findViewById(R.id.no_event);
        if (getArguments() != null)
            mEventId = getArguments().getString("eventId");

        mRecyclerView = (RecyclerView) v.findViewById(R.id.up_coming_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mEventUpComingAdapter = new EventUpComingAdapter(mRecyclerView, mEventDataList);
        mRecyclerView.setAdapter(mEventUpComingAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentScrollPosition += dy;
                if( currentScrollPosition == 0 ) {
                    mSwipeRefreshLayout.setEnabled(true);
                } else {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                getEvents();
            } else {
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
            }

        });

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            getEvents();
        } else {
            SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }
    }

    @Override
    public void onDestroy() {
       if (getEventsSubscription != null && !getEventsSubscription.isUnsubscribed())
           getEventsSubscription.unsubscribe();

        super.onDestroy();
    }

    private void getEvents(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
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
                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                        emptyView.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
                        Log.d("mEventList", "onFailure");
                    }

                    @Override
                    public void onNext(Event mEventList) {
                        if(mEventList != null && !mEventList.isErrorStatus()){
                            mEventDataList.clear();
                            if(mEventList.getEventDataList() != null && mEventList.getEventDataList().size() > 0){
                                emptyView.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                                mEventDataList = mEventList.getEventDataList();
                                mEventUpComingAdapter.notifyDataSetChanged();
                                int i = 0;
                                for (Event.EventData data : mEventDataList){
                                    if(data != null && mEventId != null && !mEventId.equalsIgnoreCase("")){
                                        if(data.getEventId().equalsIgnoreCase(mEventId)){
                                            mEventUpComingAdapter.scrollTo(i);
                                            break;
                                        }
                                    }
                                    i++;
                                }
                            } else {
                                emptyView.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                                mSwipeRefreshLayout.setVisibility(View.GONE);
                            }

                            Log.d("mEventList", "onSuccess");
                        }
                    }
                });
    }

    private class EventUpComingAdapter extends RecyclerView.Adapter<EventUpComingAdapter.ViewHolder> {

        private RecyclerView mRecyclerView;

        EventUpComingAdapter(RecyclerView recyclerView, List<Event.EventData> eventDataList) {
            mRecyclerView = recyclerView;
            mEventDataList = eventDataList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list, parent, false);
            return new ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final Event.EventData eventData = mEventDataList.get(position);
            if(eventData != null) {

                if(eventData.getEventPicture() != null && !eventData.getEventPicture().equalsIgnoreCase("")) {
                    Uri uri = Uri.parse(eventData.getEventPicture());
                    com.facebook.imagepipeline.request.ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
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

                holder.eventImage.setOnClickListener(v -> {
                    if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
                        eventDetailActivity(eventData.getEventId());
                });

                holder.eventName.setOnClickListener(v -> {
                    if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
                        eventDetailActivity(eventData.getEventId());
                });

                holder.eventTime.setOnClickListener(v -> {
                    if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
                        eventDetailActivity(eventData.getEventId());
                });

                holder.eventPlace.setOnClickListener(v -> {
                    if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
                        eventDetailActivity(eventData.getEventId());
                });

                holder.itemView.setOnClickListener(v -> {
                    if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
                        eventDetailActivity(eventData.getEventId());
                });

                String formatDate = eventData.getStartDate();
                String formatStartTime = eventData.getStartTime();
                String formatEndTime = eventData.getEndTime();
                holder.eventName.setText(eventData.getEventName());
                holder.eventTime.setText(formatDate + " at " + formatStartTime+ " to "+formatEndTime);
                holder.eventPlace.setText(eventData.getEventLocation());

            }
        }


        @Override
        public int getItemCount() {
            return mEventDataList.size();
        }


        private void scrollTo(final int position) {
            if(mEventUpComingAdapter != null){
                if (mEventUpComingAdapter.getItemCount() > 1) {
                    mRecyclerView.postDelayed(() -> mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, position), 1000);

                }
            }
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {

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

    private String convertToDate(String startDay){
        Date date;
        String strDate = null;
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strDate = sDFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    private String convertToTime(String startDay){
        Date date;
        String strTime = null;
        DateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strTime = sDTimeFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strTime;
    }

    private void eventDetailActivity(String eventId) {
        Intent i = new Intent(getContext(), EventDetailActivity.class);
        i.putExtra("eventId", eventId);
        startActivity(i);
        if(getActivity() != null){
            if(CheckOsVersion.isPreLollipop()){
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        }
    }


}
