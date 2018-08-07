package com.myscrap.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.myscrap.ImagesSlideshowDialogFragment;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.ChatRoom;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.SquaredFrameLayout;
import com.myscrap.webservice.Constants;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ms3 on 3/15/2017.
 */

public class ChatRoomMessageAdapter extends RecyclerView.Adapter {

    private List<ChatRoom> mMessages;
    private List<ChatRoom> mChatRoomImages;
    private Context mContext;
    private String mSearchString="";
    private String mChatRoomImage="";
    public int seen = 0;
    private int displayPosition = 0;

    private static final int MY_MESSAGE = 11;
    private static final int MY_PICTURE_MESSAGE = 12;
    private static final int MY_LINK_PREVIEW = 13;
    private static final int OTHERS_MESSAGE = 21;
    private static final int OTHERS_PICTURE_MESSAGE = 22;
    private static final int OTHERS_LINK_PREVIEW = 23;
    private static final int DATE_FORMATTED = 100;


    public ChatRoomMessageAdapter(Context context, List<ChatRoom> messages, String chatRoomImage, int lastSeen) {
        this.mMessages = messages;
        this.mChatRoomImage = chatRoomImage;
        this.mContext = context;
        this.seen = lastSeen;
    }



    public void setFilter(List<ChatRoom> messagesFilter, String searchString) {
        this.mMessages = messagesFilter;
        this.mSearchString = searchString;
        this.notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        if(viewType == MY_MESSAGE)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_message, parent, false);
            return new TextViewHolder(v);
        }
        else if(viewType == MY_PICTURE_MESSAGE)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_pic_message, parent, false);
            return new ImageViewHolder(v);
        }
        else if(viewType == MY_LINK_PREVIEW)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_link_preview, parent, false);
            return new LinkPreviewViewHolder(v);
        }
        if(viewType == OTHERS_MESSAGE)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_message_other, parent, false);
            return new OtherTextViewHolder(v);
        } else if(viewType == OTHERS_PICTURE_MESSAGE){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_pic_message_other, parent, false);
            return new OtherImageViewHolder(v);
        } else if(viewType == OTHERS_LINK_PREVIEW){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_link_preview_other, parent, false);
            return new OtherLinkPreviewViewHolder(v);
        } else if(viewType == DATE_FORMATTED){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_date, parent, false);
            return new DateFormatViewHolder(v);
        } else {
            return null;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ChatRoom message = mMessages.get(position);

        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (holder instanceof  TextViewHolder){

            final TextViewHolder textViewHolder = (TextViewHolder) holder;

            textViewHolder.setMessage(message.getMessage(),mSearchString, "self");

            if (textViewHolder.getAdapterPosition() == seen){
                textViewHolder.setChattedUserImage(mChatRoomImage);
                textViewHolder.messageStatus.setVisibility(View.VISIBLE);
                if (message.getSeenT() != null && !message.getSeenT().equalsIgnoreCase("")){
                    if(!message.getSeenT().equalsIgnoreCase("false") && !message.getSeenT().equalsIgnoreCase("true") ){
                        long dateVal = Long.parseLong(UserUtils.parsingLong(message.getSeenT()));
                        textViewHolder.setSeenTime(getChatRoomTime(dateVal));
                    } else {
                        textViewHolder.setSeenTime("");
                    }
                }
                textViewHolder.messageTime.setVisibility(View.GONE);
            } else if (holder.getAdapterPosition() > seen){
                if (message.getStatus().equalsIgnoreCase("1") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    textViewHolder.messageStatus.setImageResource(R.drawable.msgr_ic_message_state_sent);
                    textViewHolder.messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    textViewHolder.messageStatus.setVisibility(View.VISIBLE);
                } else if (message.getStatus().equalsIgnoreCase("2") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    textViewHolder.messageStatus.setImageResource(R.drawable.msgr_ic_message_state_delivered);
                    textViewHolder.messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    textViewHolder.messageStatus.setVisibility(View.VISIBLE);
                } else {
                    textViewHolder.messageStatus.setVisibility(View.INVISIBLE);
                    textViewHolder.messageStatus.setColorFilter(null);
                }

                if(!message.getTimeStamp().equalsIgnoreCase("") && !message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                    long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                    textViewHolder.setTime(getChatRoomTime(dateVal));
                    textViewHolder.messageTime.setVisibility(View.GONE);
                } else {
                    textViewHolder.messageTime.setVisibility(View.GONE);
                }

            } else {
                textViewHolder.messageStatus.setVisibility(View.INVISIBLE);
                textViewHolder.messageStatus.setColorFilter(null);

                if(!message.getTimeStamp().equalsIgnoreCase("") && !message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                    long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                    textViewHolder.setTime(getChatRoomTime(dateVal));
                    textViewHolder.messageTime.setVisibility(View.GONE);
                } else {
                    textViewHolder.messageTime.setVisibility(View.GONE);
                }
            }

            textViewHolder.containerLayout.setOnClickListener(v -> {
                if (textViewHolder.messageTime.isShown()){
                    textViewHolder.messageTime.setVisibility(View.GONE);
                } else {
                    textViewHolder.messageTime.setVisibility(View.VISIBLE);
                }
            });

            textViewHolder.containerLayout.setOnLongClickListener(v -> {
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                final android.content.ClipData clipData = android.content.ClipData
                        .newPlainText("COPY", textViewHolder.message.getText().toString());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                }
                Toast.makeText(mContext, "Text copied into clipboard", Toast.LENGTH_SHORT).show();
                return true;
            });

        } else if (holder instanceof  OtherTextViewHolder){
            final OtherTextViewHolder otherTextViewHolder = (OtherTextViewHolder) holder;

            otherTextViewHolder.setMessage(message.getMessage(),mSearchString, "other");

            if(message.getTimeStamp() != null && !message.getTimeStamp().equalsIgnoreCase("") && !message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                otherTextViewHolder.setTime(getChatRoomTime(dateVal));
            }

            otherTextViewHolder.messageTime.setVisibility(View.GONE);


            if (otherTextViewHolder.getAdapterPosition() == seen){
                otherTextViewHolder.setChattedUserImage(mChatRoomImage);
                otherTextViewHolder.messageStatus.setVisibility(View.VISIBLE);
            } else {
                otherTextViewHolder.messageStatus.setVisibility(View.INVISIBLE);
            }

            otherTextViewHolder.containerLayout.setOnClickListener(v -> {
                if (otherTextViewHolder.messageTime.isShown()){
                    otherTextViewHolder.messageTime.setVisibility(View.GONE);
                } else {
                    otherTextViewHolder.messageTime.setVisibility(View.VISIBLE);
                }
            });

            otherTextViewHolder.containerLayout.setOnLongClickListener(v -> {
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                final android.content.ClipData clipData = android.content.ClipData
                        .newPlainText("COPY", otherTextViewHolder.message.getText().toString());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                }
                Toast.makeText(mContext, "Text copied into clipboard", Toast.LENGTH_SHORT).show();
                return true;
            });

        } else if (holder instanceof  ImageViewHolder){
            final ImageViewHolder imageViewHolder = (ImageViewHolder)holder;

            imageViewHolder.setMessagePicture(message);

            if (imageViewHolder.getAdapterPosition() == seen){
                imageViewHolder.setChattedUserImage(mChatRoomImage);
                imageViewHolder.messageStatus.setVisibility(View.VISIBLE);
                if (message.getSeenT() != null && !message.getSeenT().equalsIgnoreCase("")){
                    if(!message.getSeenT().equalsIgnoreCase("false") && !message.getSeenT().equalsIgnoreCase("true") ){
                        long dateVal = Long.parseLong(UserUtils.parsingLong(message.getSeenT()));
                        imageViewHolder.setSeenTime(getChatRoomTime(dateVal));
                    } else {
                        imageViewHolder.setSeenTime("");
                    }
                }
            } else if (holder.getAdapterPosition() > seen){
                if (message.getStatus().equalsIgnoreCase("0") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    imageViewHolder.messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    imageViewHolder.messageStatus.setImageResource(R.drawable.msgr_ic_message_state_sending);
                    imageViewHolder.messageStatus.setVisibility(View.VISIBLE);
                    imageViewHolder.messageStatus.setTag("sending");
                } else if (message.getStatus().equalsIgnoreCase("1") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    imageViewHolder.messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    imageViewHolder.messageStatus.setImageResource(R.drawable.msgr_ic_message_state_sent);
                    imageViewHolder.messageStatus.setVisibility(View.VISIBLE);
                    imageViewHolder.messageStatus.setTag("sent");
                } else if (message.getStatus().equalsIgnoreCase("2") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    imageViewHolder.messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    imageViewHolder.messageStatus.setImageResource(R.drawable.msgr_ic_message_state_delivered);
                    imageViewHolder.messageStatus.setVisibility(View.VISIBLE);
                    imageViewHolder.messageStatus.setTag("delivered");
                } else {
                    imageViewHolder.messageStatus.setVisibility(View.INVISIBLE);
                    imageViewHolder.messageStatus.setColorFilter(null);
                    imageViewHolder.messageStatus.setTag("");
                }
                if(message.getTimeStamp() != null && !message.getTimeStamp().equals("") && !message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                    long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                    imageViewHolder.setTime(getChatRoomTime(dateVal));
                }  else {
                    imageViewHolder.setTime(null);
                }

            } else {
                imageViewHolder.messageStatus.setVisibility(View.INVISIBLE);
                imageViewHolder.messageStatus.setColorFilter(null);
                if(message.getTimeStamp() != null && !message.getTimeStamp().equals("") && !message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                    long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                    imageViewHolder.setTime(getChatRoomTime(dateVal));
                }  else {
                    imageViewHolder.setTime(null);
                }
            }

            imageViewHolder.messageTime.setVisibility(View.VISIBLE);
            imageViewHolder.messagePicture.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                if(mMessages != null && mMessages.size() > 0){
                    mChatRoomImages = new ArrayList<>();
                    for(ChatRoom chatRoom : mMessages){
                        if(chatRoom != null){
                            if(chatRoom.getMessageChatImage() != null && !TextUtils.isEmpty(chatRoom.getMessageChatImage())){
                                mChatRoomImages.add(chatRoom);
                            }
                        }
                    }
                    int i = 0;
                    for(ChatRoom roomPosition : mChatRoomImages){
                        if(roomPosition.equals(message)){
                            displayPosition = i;
                        }
                        i++;
                    }
                    bundle.putSerializable("images",  (Serializable)mChatRoomImages);
                    bundle.putInt("displayPosition", displayPosition);
                    FragmentTransaction ft = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction();
                    ImagesSlideshowDialogFragment newFragment = ImagesSlideshowDialogFragment.newInstance();
                    newFragment.setArguments(bundle);
                    newFragment.show(ft, "slideshow");
                } else {
                    mChatRoomImages = new ArrayList<>();
                    mChatRoomImages.add(mMessages.get(imageViewHolder.getAdapterPosition()));
                    bundle.putSerializable("images", (Serializable)mChatRoomImages );
                    bundle.putInt("displayPosition",  0);
                    FragmentTransaction ft = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction();
                    ImagesSlideshowDialogFragment newFragment = ImagesSlideshowDialogFragment.newInstance();
                    newFragment.setArguments(bundle);
                    newFragment.show(ft, "slideshow");
                }


            });

            imageViewHolder.messagePicture.setOnLongClickListener(v -> true);

        } else if (holder instanceof  OtherImageViewHolder){
            final OtherImageViewHolder otherImageViewHolder = (OtherImageViewHolder)holder;

            otherImageViewHolder.setMessagePicture(message);

            if (message.getTimeStamp() != null && !message.getTimeStamp().equalsIgnoreCase("")){
                if(!message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                    long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                    otherImageViewHolder.setTime(getChatRoomTime(dateVal));
                } else {
                    otherImageViewHolder.setTime("");
                }
            }
            otherImageViewHolder.messageTime.setVisibility(View.VISIBLE);


            if (otherImageViewHolder.getAdapterPosition() == seen){
                otherImageViewHolder.setChattedUserImage(mChatRoomImage);
                otherImageViewHolder.messageStatus.setVisibility(View.VISIBLE);
            } else {
                otherImageViewHolder.messageStatus.setVisibility(View.INVISIBLE);
            }

            otherImageViewHolder.messagePicture.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                if(mMessages != null && mMessages.size() > 0){
                    mChatRoomImages = new ArrayList<>();
                    for(ChatRoom chatRoom : mMessages){
                        if(chatRoom != null){
                            if(chatRoom.getMessageChatImage() != null && !TextUtils.isEmpty(chatRoom.getMessageChatImage())){
                                mChatRoomImages.add(chatRoom);
                            }
                        }
                    }
                    int i = 0;
                    for(ChatRoom roomPosition : mChatRoomImages){
                        if(roomPosition.equals(message)){
                            displayPosition = i;
                        }
                        i++;
                    }
                    bundle.putSerializable("images",  (Serializable)mChatRoomImages);
                    bundle.putInt("displayPosition", displayPosition);
                    FragmentTransaction ft = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction();
                    ImagesSlideshowDialogFragment newFragment = ImagesSlideshowDialogFragment.newInstance();
                    newFragment.setArguments(bundle);
                    newFragment.show(ft, "slideshow");
                } else {
                    mChatRoomImages = new ArrayList<>();
                    mChatRoomImages.add(mMessages.get(otherImageViewHolder.getAdapterPosition()));
                    bundle.putSerializable("images", (Serializable)mChatRoomImages );
                    bundle.putInt("displayPosition",  0);
                    FragmentTransaction ft = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction();
                    ImagesSlideshowDialogFragment newFragment = ImagesSlideshowDialogFragment.newInstance();
                    newFragment.setArguments(bundle);
                    newFragment.show(ft, "slideshow");
                }
            });

            otherImageViewHolder.messagePicture.setOnLongClickListener(v -> true);
        } else if (holder instanceof  DateFormatViewHolder) {
            final DateFormatViewHolder dateFormatViewHolder = (DateFormatViewHolder)holder;
            if (message.getMessageDateFormatted() != null && !message.getMessageDateFormatted().equalsIgnoreCase("")){
                dateFormatViewHolder.date.setText(message.getMessageDateFormatted());
                dateFormatViewHolder.itemView.setVisibility(View.VISIBLE);
            } else {
                dateFormatViewHolder.itemView.setVisibility(View.GONE);
            }
            dateFormatViewHolder.itemView.setClickable(false);
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return -1;
        if (mMessages != null && mMessages.get(position).getFrom() != null && mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("1")){
            return MY_MESSAGE;
        } else if (mMessages != null && mMessages.get(position).getFrom() != null && !mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("1")){
            return OTHERS_MESSAGE;
        } else if (mMessages != null && mMessages.get(position).getFrom() != null && mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("2")){
            return MY_PICTURE_MESSAGE;
        } else if (mMessages != null && mMessages.get(position).getFrom() != null && mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("12")){
            return MY_PICTURE_MESSAGE;
        } else if (mMessages != null && mMessages.get(position).getFrom() != null && !mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("2")){
            return OTHERS_PICTURE_MESSAGE;
        } else if (mMessages != null && mMessages.get(position).getFrom() != null && mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("3")){
            return MY_LINK_PREVIEW;
        } else if (mMessages != null && mMessages.get(position).getFrom() != null && !mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("3")){
            return OTHERS_LINK_PREVIEW;
        }else if (mMessages != null &&  !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("100")){
            return DATE_FORMATTED;
        } else {
            if (mMessages != null && mMessages.get(position).getFrom() != null &&  mMessages.get(position).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && !mMessages.get(position).getMessageType().equalsIgnoreCase("") && mMessages.get(position).getMessageType().equalsIgnoreCase("1")){
                return MY_MESSAGE;
            } else {
                return OTHERS_MESSAGE;
            }
        }
    }

    private static String getChatRoomTime(long time)
    {
        if (time < 1000000000000L)
        {
            time *= 1000;
        }
        SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        //mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        return getChatTimeStamp(dateString);
    }

    private static String getChatTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat formatTime;
        String timestamp = "";
        String dateString;
        String day;
        String dayTime;
        String weekOne;
        String weekTwo;
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String dateToday = todayFormat.format(date);
            if (isDateInCurrentWeek(date)) {
                if (dateToday.equals(today)) {
                    format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    dateString = format.format(date);
                    timestamp = dateString;
                } else {
                    format = new SimpleDateFormat("EEE ", Locale.getDefault());
                    formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    day = format.format(date);
                    dayTime = formatTime.format(date);
                    timestamp = day + " at "+dayTime;
                }
            } else if (isDateInCurrentYear(date)) {
                SimpleDateFormat sdfOne = new SimpleDateFormat("dd MMM", Locale.getDefault());
                SimpleDateFormat sdfTwo = new SimpleDateFormat("HH:mm", Locale.getDefault());
                weekOne = sdfOne.format(date);
                weekTwo = sdfTwo.format(date);
                timestamp = weekOne + " at " + weekTwo;
            } else {
                SimpleDateFormat sdfOne = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                SimpleDateFormat sdfTwo = new SimpleDateFormat("HH:mm", Locale.getDefault());
                weekOne = sdfOne.format(date);
                weekTwo = sdfTwo.format(date);
                timestamp = weekOne + " at " + weekTwo;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    private static boolean isDateInCurrentWeek(Date date)
    {
        long dateDifference = date.getTime() - new Date().getTime();
        int diffDays = (int) (dateDifference / (24 * 60 * 60 * 1000));
        return diffDays >= 0 && diffDays <= 7;
    }

    private static boolean isDateInCurrentYear(Date date)
    {
        Date currentDate = new Date();
        boolean isInYear ;
        long dateDifference = getDiffYears(currentDate,date);
        int diffYears = (int) (dateDifference);
        isInYear = diffYears == 0;
        return  isInYear;
    }


    private static int getDiffYears(Date first, Date last)
    {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        return a.get(Calendar.YEAR) - b.get(Calendar.YEAR);
    }

    private static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public void swap(List<ChatRoom> mChatRoomMessages, int lastSeen)
    {
        notifyItemRangeRemoved(0, this.mMessages.size());
        this.mMessages = mChatRoomMessages;
        this.seen = lastSeen;
        preLoadImages();
        this.notifyDataSetChanged();
    }

    public void preLoadImages(){
        if(mMessages != null && mMessages.size() > 0){
            List<Uri> mPreloadImages = new ArrayList<>();
            for(ChatRoom chatRoom : mMessages){
                if(chatRoom != null){
                    if(chatRoom.getMessageChatImage() != null && !TextUtils.isEmpty(chatRoom.getMessageChatImage())){
                        Uri uri = Uri.parse(Constants.CHAT_IMAGE_URL_PREFIX+chatRoom.getMessageChatImage());
                        mPreloadImages.add(uri);
                    }
                }
            }
            if(mPreloadImages.size() > 0){
                final PrefetchSubscriber subscriber = new PrefetchSubscriber();
                for (Uri uri : mPreloadImages) {
                    final DataSource<Void> ds =
                            Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(uri), null);
                    ds.subscribe(subscriber, UiThreadImmediateExecutorService.getInstance());
                }
            }

        }
    }


    public class DateFormatViewHolder extends  RecyclerView.ViewHolder{
        TextView date;
        public DateFormatViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }


    public class TextViewHolder extends  RecyclerView.ViewHolder{
        TextView message, messageTime;
        SimpleDraweeView messageStatus;
        RelativeLayout containerLayout;
        TextViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.message);
            messageTime = (TextView) itemView.findViewById(R.id.timestamp);
            messageStatus = (SimpleDraweeView) itemView.findViewById(R.id.user_reply_status);
            containerLayout = (RelativeLayout) itemView.findViewById(R.id.chat);
        }


        void setMessage(String incomingMessage, String mSearchString, String selectColor) {
            if (null == message) return;

            if(!incomingMessage.trim().equalsIgnoreCase("")){
                message.setText(incomingMessage);
                message.setVisibility(View.VISIBLE);
            } else {
                message.setVisibility(View.GONE);
            }

            if (incomingMessage.trim().length() <= 3) {
                containerLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_view_self_hangout_single));
            } else {
                containerLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_view_self_hangout));
            }

            String search = null;
            String msg = incomingMessage.toLowerCase();
            if(mSearchString != null)
                search = mSearchString.toLowerCase();

            // displaying text view data
            if (search != null && !search.equalsIgnoreCase("") && msg.contains(search)) {
                Log.d("msg", ""+msg);
                int startPos = msg.indexOf(search);
                Log.d("startPos", ""+startPos);
                int endPos = startPos + search.length();
                Log.d("endPos", ""+endPos);
                Spannable spanString = Spannable.Factory.getInstance().newSpannable(message.getText());
                if (startPos >=0 && endPos != 0){
                    if (selectColor.equalsIgnoreCase("self")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight,mContext.getTheme())), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight,mContext.getTheme())), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                message.setText(spanString);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    message.setText(Html.fromHtml(incomingMessage,  Html.FROM_HTML_MODE_COMPACT));
                } else {
                    message.setText(Html.fromHtml(incomingMessage));
                }
            }
        }

        void setChattedUserImage(final String image) {
            if (null == messageStatus) return;
            if (image != null ){
                if(image.equalsIgnoreCase("") ||
                        image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                    messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                } else {
                    Uri uri = Uri.parse(image);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    messageStatus.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    messageStatus.setImageURI(uri);
                    messageStatus.setColorFilter(null);
                }
            } else {
                messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

        }

        private void setSeenTime(String time) {
            if (null == messageTime) return;
            String seenTime = "Seen "+time;
            messageTime.setText(seenTime);
            messageTime.setVisibility(View.VISIBLE);
        }

        private void setTime(String time) {
            if (null == messageTime) return;
            messageTime.setText(time);
            messageTime.setVisibility(View.VISIBLE);
        }

    }

    private class PrefetchSubscriber extends BaseDataSubscriber<Void> {

        private int mSuccessful = 0;
        private int mFailed = 0;

        @Override
        protected void onNewResultImpl(DataSource<Void> dataSource) {
            mSuccessful++;
        }

        @Override
        protected void onFailureImpl(DataSource<Void> dataSource) {
            mFailed++;
        }

        private void updateDisplay() {
        }
    }

    public class OtherTextViewHolder extends  RecyclerView.ViewHolder{
        TextView message, messageTime;
        RelativeLayout containerLayout;
        SimpleDraweeView messageStatus;

        OtherTextViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.message);
            messageTime = (TextView) itemView.findViewById(R.id.timestamp);
            containerLayout = (RelativeLayout) itemView.findViewById(R.id.chat);
            messageStatus = (SimpleDraweeView) itemView.findViewById(R.id.user_reply_status);
        }

        void setMessage(String incomingMessage, String mSearchString, String selectColor) {
            if (null == message) return;
            if(!incomingMessage.trim().equalsIgnoreCase("")){
                message.setText(incomingMessage);
                message.setVisibility(View.VISIBLE);
            } else {
                message.setVisibility(View.GONE);
            }

            if (incomingMessage.trim().length() <= 3) {
                containerLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_view_other_hangout_single));
            } else {
                containerLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_view_other_hangout));
            }

            String search = null;
            String msg = incomingMessage.toLowerCase();
            if(mSearchString != null)
                search = mSearchString.toLowerCase();

            // displaying text view data
            if (search != null && !search.equalsIgnoreCase("") && msg.contains(search)) {
                Log.d("msg", ""+msg);
                int startPos = msg.indexOf(search);
                Log.d("startPos", ""+startPos);
                int endPos = startPos + search.length();
                Log.d("endPos", ""+endPos);
                Spannable spanString = Spannable.Factory.getInstance().newSpannable(message.getText());
                if (startPos >=0 && endPos != 0){
                    if (selectColor.equalsIgnoreCase("self")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight,mContext.getTheme())), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight,mContext.getTheme())), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_text_highlight)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                message.setText(spanString);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    message.setText(Html.fromHtml(incomingMessage,  Html.FROM_HTML_MODE_COMPACT));
                } else {
                    message.setText(Html.fromHtml(incomingMessage));
                }
            }
        }

        void setChattedUserImage(final String image) {
            if (null == messageStatus) return;
            if (image != null ){
                if(image.equalsIgnoreCase("") ||
                        image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){

                    messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                    messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                } else {
                    Uri uri = Uri.parse(image);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    messageStatus.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    messageStatus.setImageURI(uri);
                    messageStatus.setColorFilter(null);
                }
            }  else {
                messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

        }

        private void setSeenTime(String time)
        {
            if (null == messageTime) return;
            String seenTime = "Seen "+time;
            messageTime.setText(seenTime);
            messageTime.setVisibility(View.VISIBLE);
        }

        private void setTime(String time)
        {
            if (null == messageTime) return;
            messageTime.setText(time);
            messageTime.setVisibility(View.VISIBLE);
        }

    }

    public class ImageViewHolder extends  RecyclerView.ViewHolder{
        TextView messageTime;
        SimpleDraweeView messageStatus;
        SimpleDraweeView messagePicture;
        SquaredFrameLayout messageSquaredFrameLayout;
        ProgressBar  mProgressBar;

        ImageViewHolder(View itemView) {
            super(itemView);
            messageTime = (TextView) itemView.findViewById(R.id.timestamp);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
            messageStatus = (SimpleDraweeView) itemView.findViewById(R.id.user_reply_status);
            messagePicture = (SimpleDraweeView) itemView.findViewById(R.id.squaredImageView);
            messageSquaredFrameLayout = (SquaredFrameLayout) itemView.findViewById(R.id.message_image_layout);
        }

        private void setChattedUserImage(final String image) {
            if (null == messageStatus) return;
            if (image != null ){
                if(image.equalsIgnoreCase("") ||
                        image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                    messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                } else {
                    Uri uri = Uri.parse(image);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    messageStatus.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    messageStatus.setImageURI(uri);
                    messageStatus.setColorFilter(null);
                }
            } else {
                messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

        }

        private void setSeenTime(String time) {
            if (null == messageTime) return;
            String seenTime = "Seen "+time;
            messageTime.setText(seenTime);
            messageTime.setVisibility(View.VISIBLE);
        }

        private void setTime(String time) {
            if (null == messageTime) return;
            messageTime.setText(time);
            messageTime.setVisibility(View.VISIBLE);
        }

        private void setMessagePicture(final ChatRoom message){

            AppController.runOnUIThread(() -> {
                Timer timer = new Timer();
                if (message.getMessageChatImage() != null && !message.getMessageChatImage().equalsIgnoreCase("")
                        && message.getMessageType() != null && !message.getMessageType().equalsIgnoreCase("") && message.getMessageType().equalsIgnoreCase("2")){
                    Uri uri = Uri.parse(Constants.CHAT_IMAGE_URL_PREFIX+message.getMessageChatImage());
                    ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setLocalThumbnailPreviewsEnabled(true)
                            .setProgressiveRenderingEnabled(true)
                            .build();
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imgReq)
                            .setTapToRetryEnabled(true)
                            .setOldController(messagePicture.getController())
                            .build();
                    messagePicture.setController(controller);
                    messagePicture.setVisibility(View.VISIBLE);
                    messageSquaredFrameLayout.setVisibility(View.VISIBLE);
                    timer.purge();
                    timer.cancel();
                    mProgressBar.post(() -> mProgressBar.setVisibility(View.GONE));
                } else if(message.getMessageChatImage() != null && !message.getMessageChatImage().equalsIgnoreCase("")
                        && message.getMessageType() != null && !message.getMessageType().equalsIgnoreCase("") && message.getMessageType().equalsIgnoreCase("12")){
                    messagePicture.setVisibility(View.VISIBLE);
                    messagePicture.setImageResource(R.drawable.border_view_image);
                    long delayInMillis = 10 * 1000;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mProgressBar.post(() -> {
                                if(mProgressBar.isShown())
                                    mProgressBar.setVisibility(View.GONE);
                            });
                        }
                    }, delayInMillis);
                    mProgressBar.post(() -> mProgressBar.setVisibility(View.VISIBLE));
                    messageSquaredFrameLayout.setVisibility(View.VISIBLE);
                } else {
                    timer.purge();
                    timer.cancel();
                    messagePicture.setVisibility(View.GONE);
                    messageSquaredFrameLayout.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                }
            });


        }
    }

    public class OtherImageViewHolder extends  RecyclerView.ViewHolder{
        TextView messageTime;
        SimpleDraweeView messagePicture;
        SimpleDraweeView messageStatus;
        SquaredFrameLayout messageSquaredFrameLayout;
        OtherImageViewHolder(View itemView) {
            super(itemView);
            messageStatus = (SimpleDraweeView) itemView.findViewById(R.id.user_reply_status);
            messageTime = (TextView) itemView.findViewById(R.id.timestamp);
            messagePicture = (SimpleDraweeView) itemView.findViewById(R.id.squaredImageView);
            messageSquaredFrameLayout = (SquaredFrameLayout) itemView.findViewById(R.id.message_image_layout);
        }

        private void setChattedUserImage(final String image) {
            if (null == messageStatus) return;
            if (image != null ){
                if(image.equalsIgnoreCase("") ||
                        image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                    messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                } else {
                    Uri uri = Uri.parse(image);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    messageStatus.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    messageStatus.setImageURI(uri);
                    messageStatus.setColorFilter(null);
                }
            } else {
                messageStatus.setImageResource(R.drawable.msgr_ic_message_state_seen);
                messageStatus.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                messageStatus.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }

        private void setTime(String time) {
            if (null == messageTime) return;
            messageTime.setText(time);
            messageTime.setVisibility(View.VISIBLE);
        }

        private void setMessagePicture(final ChatRoom message){

            AppController.runOnUIThread(() -> {
                if(message.getMessageChatImage() != null && !message.getMessageChatImage().equalsIgnoreCase("")
                        && message.getMessageType() != null && !message.getMessageType().equalsIgnoreCase("") && message.getMessageType().equalsIgnoreCase("2")){
                    Uri uri = Uri.parse(Constants.CHAT_IMAGE_URL_PREFIX+message.getMessageChatImage());
                    ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setProgressiveRenderingEnabled(true)
                            .setLocalThumbnailPreviewsEnabled(true)
                            .build();
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imgReq)
                            .setTapToRetryEnabled(true)
                            .setOldController(messagePicture.getController())
                            .build();
                    messagePicture.setController(controller);
                    messagePicture.setVisibility(View.VISIBLE);
                    messageSquaredFrameLayout.setVisibility(View.VISIBLE);
                } else {
                    messagePicture.setVisibility(View.GONE);
                    messageSquaredFrameLayout.setVisibility(View.GONE);
                }
            });
        }

    }

    public class LinkPreviewViewHolder extends  RecyclerView.ViewHolder{
        TextView linkTitle;
        TextView link;
        TextView messageTime;
        SimpleDraweeView messageStatus;
        SimpleDraweeView linkPreviewImage;
        ProgressBar linkProgressBar;
        SquaredFrameLayout LinkSquaredFrameLayout;
        LinkPreviewViewHolder(View itemView) {
            super(itemView);
            linkTitle = (TextView) itemView.findViewById(R.id.title);
            link = (TextView) itemView.findViewById(R.id.link);
            messageTime = (TextView) itemView.findViewById(R.id.timestamp);
            messageStatus = (SimpleDraweeView) itemView.findViewById(R.id.user_reply_status);
            linkPreviewImage = (SimpleDraweeView) itemView.findViewById(R.id.link_preview_image);
            linkProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            LinkSquaredFrameLayout = (SquaredFrameLayout) itemView.findViewById(R.id.message_link_preview_layout);
        }
    }

    public class OtherLinkPreviewViewHolder extends  RecyclerView.ViewHolder{
        TextView linkTitle;
        TextView link;
        TextView messageTime;
        SimpleDraweeView linkPreviewImage;
        ProgressBar linkProgressBar;
        SquaredFrameLayout LinkSquaredFrameLayout;
        OtherLinkPreviewViewHolder(View itemView) {
            super(itemView);
            linkTitle = (TextView) itemView.findViewById(R.id.title);
            link = (TextView) itemView.findViewById(R.id.link);
            messageTime = (TextView) itemView.findViewById(R.id.timestamp);
            linkPreviewImage = (SimpleDraweeView) itemView.findViewById(R.id.link_preview_image);
            linkProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            LinkSquaredFrameLayout = (SquaredFrameLayout) itemView.findViewById(R.id.message_link_preview_layout);
        }
    }

}
