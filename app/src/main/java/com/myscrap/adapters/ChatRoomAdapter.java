package com.myscrap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.NearFriends;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FlipAnimator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.MyViewHolder> {
    private Context mContext;
    private List<ChatRoom> messages;
    private List<NearFriends.NearFriendsData> activeUsersLists = new ArrayList<>();
    private MessageAdapterListener listener;
    private SparseBooleanArray selectedItems;
    private String searchString="";
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;
    private static int currentSelectedIndex = -1;


    public void swap(List<ChatRoom> chatRooms) {
        this.messages = chatRooms;
        this.notifyDataSetChanged();
    }

    public void setFilter(List<ChatRoom> filteredModelList, String searchQueryString) {
        this.searchString = searchQueryString;
        this.messages = filteredModelList;
        this.notifyDataSetChanged();
    }

    public void showTyping(List<ChatRoom> chatRooms, String typingFrom)
    {
        if (chatRooms == null)
        {
            return;
        }

        for (int i=0; i <chatRooms.size(); i++)
        {
            if (chatRooms.get(i) == null)
                return;

            if ((chatRooms.get(i).getFrom().equalsIgnoreCase(typingFrom) || chatRooms.get(i).getTo().equalsIgnoreCase(typingFrom)))
            {
                chatRooms.get(i).setTyping(true);
            }
        }
        this.notifyDataSetChanged();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
    {

        private TextView from, count,online, subject, message, iconText, timestamp;
        private SimpleDraweeView iconImp;
        private SimpleDraweeView imgProfile;
        private LinearLayout messageContainer;
        private RelativeLayout rootView,iconContainer, iconBack, iconFront;

        private MyViewHolder(View view)
        {

            super(view);
            from = (TextView) view.findViewById(R.id.from);
            online = (TextView) view.findViewById(R.id.online);
            count = (TextView) view.findViewById(R.id.count);
            subject = (TextView) view.findViewById(R.id.txt_primary);

            timestamp = (TextView) view.findViewById(R.id.timestamp);
            iconBack = (RelativeLayout) view.findViewById(R.id.icon_back);
            iconFront = (RelativeLayout) view.findViewById(R.id.icon_front);
            iconImp = (SimpleDraweeView) view.findViewById(R.id.icon_star);
            imgProfile = (SimpleDraweeView) view.findViewById(R.id.icon_profile);
            iconText = (TextView) view.findViewById(R.id.icon_text);
            messageContainer = (LinearLayout) view.findViewById(R.id.message_container);
            iconContainer = (RelativeLayout) view.findViewById(R.id.icon_container);
            rootView = (RelativeLayout) view.findViewById(R.id.root_view);
            view.setOnLongClickListener(this);

        }

        @Override
        public boolean onLongClick(View view) {
            listener.onRowLongClicked(getAdapterPosition());
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }


    public ChatRoomAdapter(Context mContext, List<ChatRoom> messages, List<NearFriends.NearFriendsData> mActiveUsersLists, MessageAdapterListener listener) {
        this.mContext = mContext;
        this.messages = messages;
        this.listener = listener;
        this.activeUsersLists = mActiveUsersLists;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position)
    {

        if (messages.get(position) == null)
            return;
        ChatRoom message = messages.get(position);
        String search = null;
        if(message != null)
        {
            if(message.getMessageFrom() != null && !message.getMessageFrom().equalsIgnoreCase(""))
                holder.from.setText(UserUtils.capitalizeFirst(message.getMessageFrom()));

            String msg = message.getMessageFrom().toLowerCase();
            if(searchString != null)
                search = searchString.toLowerCase();

            // displaying text view data
            if (search != null && !search.equalsIgnoreCase("") && msg.contains(search)) {
                Log.d("msg", ""+msg);
                int startPos = msg.indexOf(search);
                Log.d("startPos", ""+startPos);
                int endPos = startPos + search.length();
                Log.d("endPos", ""+endPos);
                Spannable spanString = Spannable.Factory.getInstance().newSpannable(holder.from.getText());
                if (startPos >=0 &&endPos != 0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorPrimary,mContext.getTheme())), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        spanString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorPrimary)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                holder.from.setText(spanString);
            } else {
                holder.from.setText(message.getMessageFrom());
            }



            if(message.getTimeStamp() != null && !message.getTimeStamp().equalsIgnoreCase("")&& !message.getTimeStamp().equalsIgnoreCase("true") && !message.getTimeStamp().equalsIgnoreCase("false")){
                long time = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                if (time < 1000000000000L) {
                    time *= 1000;
                }
                holder.timestamp.setText(getChatRoomTime(time));
            }

            // displaying the first letter of From in icon text
            if (message.getMessageFrom() != null && !message.getMessageFrom().equalsIgnoreCase("")){
                String[] split = message.getMessageFrom().split("\\s+");
                if(split.length == 0)
                    return;
                if (split.length > 1){
                    String first = split[0].substring(0,1);
                    String last = split[1].substring(0,1);
                    String initial = first + ""+ last ;
                    holder.iconText.setText(initial.toUpperCase());
                } else {
                    if (split[0] != null && split[0].trim().length() >= 1) {
                        String first = split[0].substring(0, 1);
                        holder.iconText.setText(first.toUpperCase());
                    }
                }
            }

            holder.online.setVisibility(View.GONE);

            if(activeUsersLists != null && activeUsersLists.size() > 0){
                for(NearFriends.NearFriendsData mayKnowItem : activeUsersLists){
                    if(mayKnowItem.getUserid().equalsIgnoreCase(String.valueOf(message.getFrom())) || mayKnowItem.getUserid().equalsIgnoreCase(String.valueOf(message.getTo()))){
                        if (mayKnowItem.isOnline()){
                            holder.online.setVisibility(View.VISIBLE);
                        } else {
                            holder.online.setVisibility(View.GONE);
                        }
                    }
                }
            }

            if(message.getUnReadMessageCount() != 0) {
                holder.iconImp.setVisibility(View.GONE);
                holder.count.setVisibility(View.VISIBLE);
                holder.count.setText(String.valueOf(message.getUnReadMessageCount()));
            } else {
                holder.iconImp.setVisibility(View.VISIBLE);
                holder.count.setVisibility(View.GONE);
                // handle message seen
                applyMessageSeen(holder, message);
            }

            // change the row state to activated
            holder.itemView.setActivated(selectedItems.get(position, false));

            if (message.isTyping()){
                holder.subject.setText("is typing..");
                holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            } else {
                if(message.getMessageType() != null){
                    if(message.getMessageType().equalsIgnoreCase("1")){
                        holder.subject.setText(message.getMessage());
                    } else if(message.getMessageType().equalsIgnoreCase("2")){
                        if(message.getFrom() != null && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            holder.subject.setText("You sent an image.");
                        } else {
                            if(message.getMessageFrom() != null && !message.getMessageFrom().equalsIgnoreCase("")){
                                String name = UserUtils.capitalizeFirst(message.getMessageFrom());
                                holder.subject.setText(name+" sent an image.");
                            } else {
                                holder.subject.setText("sent an image.");
                            }
                        }
                    } else if(message.getMessageType().equalsIgnoreCase("12")){
                        if(message.getFrom() != null && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            holder.subject.setText("You sent an image.");
                        } else {
                            if(message.getMessageFrom() != null && !message.getMessageFrom().equalsIgnoreCase("")){
                                String name = UserUtils.capitalizeFirst(message.getMessageFrom());
                                holder.subject.setText(name+" sent an image.");
                            } else {
                                holder.subject.setText("sent an image.");
                            }
                        }
                    }
                }
                applyReadStatus(holder, message);
            }

            // handle icon animation
            applyIconAnimation(holder, position);

            // display profile image
            applyProfilePicture(holder, message);

            // apply click events
            applyClickEvents(holder, position);

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
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        return getChatTimeStamp(dateString);
    }

    private static String getChatTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = "";
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("dd MMM", Locale.getDefault());
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    private void applyClickEvents(MyViewHolder holder, final int position)
    {
        holder.iconContainer.setOnClickListener(view -> listener.onIconClicked(position));

        holder.messageContainer.setOnClickListener(view -> listener.onMessageRowClicked(position));

        holder.from.setOnClickListener(view -> listener.onMessageRowClicked(position));
        holder.subject.setOnClickListener(view -> listener.onMessageRowClicked(position));

        holder.rootView.setOnLongClickListener(view -> {
            listener.onRowLongClicked(position);
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        });
        holder.rootView.setOnLongClickListener(view -> {
            listener.onRowLongClicked(position);
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        });
    }

    private void applyProfilePicture(final MyViewHolder holder, final ChatRoom message)
    {
        if (message.getProfilePic() != null) {
            if(message.getProfilePic().equalsIgnoreCase("") ||
                message.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png") ||
                message.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")) {
                holder.imgProfile.setImageResource(R.drawable.bg_circle);
                if(message.getColor() != null && !message.getColor().equalsIgnoreCase("") && message.getColor().startsWith("#")){
                    holder.imgProfile.setColorFilter(Color.parseColor(message.getColor()));
                } else {
                    holder.imgProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                }
                holder.iconText.setVisibility(View.VISIBLE);
            } else {
                Uri uri = Uri.parse(message.getProfilePic());
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                holder.imgProfile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                holder.imgProfile.setImageURI(uri);
                holder.imgProfile.setColorFilter(null);
                holder.iconText.setVisibility(View.GONE);
            }
        }
    }

    private void applyIconAnimation(MyViewHolder holder, int position)
    {
        if (selectedItems.get(position, false)) {
            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }


    private void resetIconYAxis(View view)
    {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    public void resetAnimationIndex()
    {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    private void applyMessageSeen(MyViewHolder holder, ChatRoom message)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if ((message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) && message.getStatus().equalsIgnoreCase("3")) {
            String image =  message.getProfilePic();
            if (image != null ){
                if(image.equalsIgnoreCase("") ||
                        image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    holder.iconImp.setImageResource(R.drawable.msgr_ic_message_state_seen);
                    holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                } else {
                    Uri uri = Uri.parse(image);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    holder.iconImp.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    holder.iconImp.setImageURI(uri);
                    holder.iconImp.setColorFilter(null);
                }
            } else {
                holder.iconImp.setImageResource(R.drawable.msgr_ic_message_state_seen);
                holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            }
        } else if (message.getStatus().equalsIgnoreCase("1") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            holder.iconImp.setImageResource(R.drawable.msgr_ic_message_state_sent);
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            holder.iconImp.setVisibility(View.VISIBLE);
        } else if (message.getStatus().equalsIgnoreCase("2") && message.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            holder.iconImp.setImageResource(R.drawable.msgr_ic_message_state_delivered);
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            holder.iconImp.setVisibility(View.VISIBLE);
        } else {
            holder.iconImp.setVisibility(View.GONE);
            holder.iconImp.setColorFilter(null);
        }
    }


    private void applyReadStatus(MyViewHolder holder, ChatRoom message)
    {
        Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        if (message.isRead()) {
            holder.from.setTextColor(ContextCompat.getColor(mContext, R.color.message));
            holder.from.setTypeface(normalTypeface);
            holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.message));
            holder.subject.setTypeface(normalTypeface);
            holder.timestamp.setTextColor(ContextCompat.getColor(mContext, R.color.message));
            holder.timestamp.setTypeface(normalTypeface);
            holder.rootView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_unselected_list_row));
        } else {
            holder.from.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            holder.from.setTypeface(boldTypeface);
            holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            holder.subject.setTypeface(boldTypeface);
            holder.timestamp.setTextColor(ContextCompat.getColor(mContext, R.color.online));
            holder.timestamp.setTypeface(boldTypeface);
            holder.rootView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_selected_list_row));
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void toggleSelection(int pos)
    {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections()
    {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount()
    {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems()
    {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeData(int position)
    {
        if (!messages.isEmpty())
            messages.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex()
    {
        currentSelectedIndex = -1;
    }


    public interface MessageAdapterListener
    {
        void onIconClicked(int position);
        void onIconImportantClicked(int position);
        void onMessageRowClicked(int position);
        void onRowLongClicked(int position);
    }
}