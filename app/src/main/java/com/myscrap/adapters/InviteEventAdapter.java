package com.myscrap.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.Contact;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.UserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ms3 on 5/18/2017.
 */

public class InviteEventAdapter extends RecyclerView.Adapter<InviteEventAdapter.ItemViewHolder> implements Filterable {
    private Context mContext;
    private InviteEventAdapter mInviteEventAdapter;
    private InviteEventAdapter.InviteEventAdapterListener mContactsFragmentAdapterListener;
    private List<Contact.ContactData> mMarkersDataListFiltered = new ArrayList<>();

    public InviteEventAdapter(Context context, List<Contact.ContactData> mContactDataList, InviteEventAdapter.InviteEventAdapterListener contactsFragmentAdapterListener){
        this.mInviteEventAdapter = this;
        this.mContext = context;
        this.mContactsFragmentAdapterListener = contactsFragmentAdapterListener;
    }

    @Override
    public InviteEventAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_contacts_list_row, parent, false);
        return new InviteEventAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final InviteEventAdapter.ItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(mMarkersDataListFiltered.isEmpty())
            return;
        final Contact.ContactData contactData = mMarkersDataListFiltered.get(position);
        if(contactData != null) {
            if(contactData.getProfilePic() != null){


                holder.profileImage.post(() -> {
                    String profilePicture = contactData.getProfilePic();
                    if (!profilePicture.equalsIgnoreCase("")){
                        if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            holder.profileImage.setImageResource(R.drawable.bg_circle);
                            if(contactData.getColorCode()!= null && !contactData.getColorCode().equalsIgnoreCase("") && contactData.getColorCode().startsWith("#")){
                                holder.profileImage.setColorFilter(Color.parseColor(contactData.getColorCode()));
                            } else {
                                holder.profileImage.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                            }

                            holder.iconText.setVisibility(View.VISIBLE);
                        } else  {

                            Uri uri = Uri.parse(profilePicture);
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            holder.profileImage.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            holder.profileImage.setImageURI(uri);
                            holder.profileImage.setColorFilter(null);
                            holder.iconText.setVisibility(View.GONE);
                        }
                    } else {
                        holder.profileImage.setImageResource(R.drawable.bg_circle);
                        holder.profileImage.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        holder.iconText.setVisibility(View.VISIBLE);
                    }
                });

            }
            holder.iconStar.setTag("click");
            if(contactData.getName() != null){
                holder.profileName.post(() -> holder.profileName.setText(contactData.getName()));

                holder.iconText.post(() -> {
                    String userName = contactData.getName();
                    if (!userName.equalsIgnoreCase("")){
                        String[] split = userName.split("\\s+");
                        if (split.length > 1){
                            String first = split[0].substring(0,1);
                            String last = split[1].substring(0,1);
                            String initial = first + ""+ last ;
                            holder.iconText.setText(initial.toUpperCase().trim());
                        } else {
                            if (split[0] != null && split[0].trim().length() >= 1) {
                                String first = split[0].substring(0, 1);
                                holder.iconText.setText(first.toUpperCase().trim());
                            }
                        }
                    }
                });


            }


            holder.top.post(() -> {
                if(contactData.getModerator() == 1) {
                    holder.top.setText(R.string.mod);
                    holder.top.setVisibility(View.VISIBLE);
                    holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
                    holder.badge.setVisibility(View.GONE);
                } else {
                    if (contactData.getRank() >= 1 && contactData.getRank() <=10) {
                        holder.badge.setVisibility(View.GONE);
                        holder.top.setVisibility(View.VISIBLE);
                        holder.top.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                        holder.top.setText( "TOP " + contactData.getRank());
                        holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                    } else {
                        if(contactData.isNewJoined()){
                            holder.top.setVisibility(View.VISIBLE);
                            holder.top.setText(R.string.new_user);
                            holder.top.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                            holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                        } else {
                            holder.top.setVisibility(View.GONE);
                            holder.top.setBackground(null);
                        }
                        holder.badge.setVisibility(View.GONE);
                    }
                }
            });


            holder.points.post(() -> {
                if(contactData.getPoints() == 0 || contactData.getPoints() == 1){
                    holder.points.setText(String.valueOf(contactData.getPoints()));
                } else if(contactData.getPoints() > 1){
                    holder.points.setText(String.valueOf(contactData.getPoints()));
                }
            });


            holder.profileRange.setVisibility(View.GONE);

            holder.profileDesignation.post(() -> {
                String userPosition;
                if(contactData.getDesignation() != null && !contactData.getDesignation().equalsIgnoreCase("")){
                    userPosition = contactData.getDesignation().trim();
                } else {
                    userPosition = "Trader";
                }

                String userCompany;
                if(contactData.getUserCompany() != null && !contactData.getUserCompany().equalsIgnoreCase("")){
                    userCompany = contactData.getUserCompany().trim();
                } else {
                    userCompany = "";
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SpannableStringBuilder spannedDetails;
                    if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
                    }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else if(!userPosition.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else if(!userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                    }
                    holder.profileDesignation.setText(spannedDetails);
                    holder.profileDesignation.setVisibility(View.VISIBLE);
                } else {
                    SpannableStringBuilder spannedDetails;
                    if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>"));
                    } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                    }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                    } else if(!userPosition.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                    } else if(!userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                    } else {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                    }
                    holder.profileDesignation.setText(spannedDetails);
                    holder.profileDesignation.setVisibility(View.VISIBLE);
                }
            });

            holder.company.post(() -> {
                if(contactData.getCountry() != null && !contactData.getCountry().equalsIgnoreCase("")){
                    holder.company.setText(contactData.getCountry().trim());
                    holder.company.setVisibility(View.VISIBLE);
                } else {
                    holder.company.setVisibility(View.GONE);
                }
            });


            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;

            holder.iconStar.post(() -> {
                if(!contactData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    if(contactData.isInvited()){
                        holder.iconStar.setTag("clicked");
                        holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.friending_glyph_friends));
                        holder.iconStar.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    } else {
                        holder.iconStar.setTag("click");
                        holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.friending_glyph_add_friend));
                        holder.iconStar.setColorFilter(null);
                    }
                    holder.iconStar.setVisibility(View.VISIBLE);
                } else {
                    holder.iconStar.setVisibility(View.GONE);
                }
            });


            holder.iconStar.setOnClickListener(v -> {
                if(UserUtils.isGuestLoggedIn(mContext)){
                    GuestLoginDialog.show(mContext);
                    return;
                }
                holder.iconStar.post(() -> {
                    if (holder.iconStar.getTag().equals("click")) {
                        holder.iconStar.setTag("clicked");
                        holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.friending_glyph_friends));
                        holder.iconStar.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                        contactData.setInvited(false);
                        mMarkersDataListFiltered.set(position,contactData);
                        mContactsFragmentAdapterListener.onStarClicked(contactData, true);
                    } else {
                        holder.iconStar.setTag("click");
                        holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.friending_glyph_add_friend));
                        holder.iconStar.setColorFilter(null);
                        contactData.setInvited(true);
                        mMarkersDataListFiltered.set(position,contactData);
                        mContactsFragmentAdapterListener.onStarClicked(contactData, false);
                    }
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        if(mMarkersDataListFiltered == null)
            mMarkersDataListFiltered = new ArrayList<>();
        return mMarkersDataListFiltered.size();
    }

    public void swap(List<Contact.ContactData> mContactList) {
            if(mMarkersDataListFiltered != null){
                mMarkersDataListFiltered = mContactList;
                mInviteEventAdapter.notifyDataSetChanged();
            }
    }

    private Filter fRecords;
    @Override
    public Filter getFilter() {
        if( fRecords == null)
            fRecords = new ContactFilter(mInviteEventAdapter, mMarkersDataListFiltered);
        return fRecords;
    }


    private class ContactFilter extends Filter {

        InviteEventAdapter adapter;
        final List<Contact.ContactData> originalList;
        List<Contact.ContactData> filteredList;

        public ContactFilter(InviteEventAdapter adapter, List<Contact.ContactData> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = originalList;
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            if(originalList == null)
                return null;
            filteredList.clear();
            List<Contact.ContactData> dataName = new ArrayList<>();
            List<Contact.ContactData> dataByName;

            for(Contact.ContactData item : filterByName(originalList)){
                if(item.getName() != null && !item.getName().equalsIgnoreCase("")){
                    dataName.add(item);
                }
            }
            dataByName = filterByName(dataName);
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(dataByName);
            } else {
                filteredList = doFiltering(constraint, dataByName);
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, final FilterResults results) {
            if(results.values != null){

                if (filteredList.isEmpty()){
                    adapter.swap(originalList);
                } else {
                    mMarkersDataListFiltered = (List) results.values;
                    adapter.swap(mMarkersDataListFiltered);
                }
            }
        }
    }

    private List<Contact.ContactData> filterByName(List<Contact.ContactData> dataName) {
        Collections.sort(dataName, (o1, o2) -> o1.getUserCompany().trim().compareTo(o2.getUserCompany().trim()));
        return dataName;
    }


    private List<Contact.ContactData> doFiltering(CharSequence constraint, List<Contact.ContactData> dataName) {
        List<Contact.ContactData> filteredList = new ArrayList<>();
        for(final Contact.ContactData item : dataName){
            if(item.getName() != null && !item.getName().equalsIgnoreCase("")){
                final String text = item.getName().toLowerCase().toLowerCase();
                if (text.contains(constraint.toString().toLowerCase())) {
                    filteredList.add(item);
                }
            }

            if(item.getCountry()!= null && !item.getCountry().equalsIgnoreCase("")){
                final String countryName = item.getCountry().toLowerCase();
                if(countryName.startsWith(constraint.toString().toLowerCase())){
                    filteredList.add(item);
                }
            }
        }
        if(filteredList.size() > 0) {
            Collections.sort(filteredList, (itemOne, itemTwo) -> (itemOne.getName()).compareTo((itemTwo.getName())));
        }
        Set<Contact.ContactData> hs = new HashSet<>();
        hs.addAll(filteredList);
        filteredList.clear();
        filteredList.addAll(hs);
        return filteredList;
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SimpleDraweeView profileImage;
        private ImageView iconStar;
        private ImageView badge;
        private TextView company;
        private TextView profileName;
        private TextView profileDesignation;
        private TextView profileRange;
        private TextView iconText;
        private TextView top;
        private TextView points;

        public ItemViewHolder(View itemView) {
            super(itemView);
            profileImage = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            iconStar = (ImageView) itemView.findViewById(R.id.icon_star);
            badge = (ImageView) itemView.findViewById(R.id.icon_badge);
            profileName = (TextView) itemView.findViewById(R.id.name);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            company = (TextView) itemView.findViewById(R.id.company);
            top = (TextView) itemView.findViewById(R.id.top);
            points = (TextView) itemView.findViewById(R.id.points);
            profileDesignation = (TextView) itemView.findViewById(R.id.designation);
            profileRange = (TextView) itemView.findViewById(R.id.from_range);
            profileImage.setOnClickListener(this);
            profileName.setOnClickListener(this);
            profileDesignation.setOnClickListener(this);
            profileRange.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mContactsFragmentAdapterListener != null && !mMarkersDataListFiltered.isEmpty())
                mContactsFragmentAdapterListener.onContactsAdapterClicked(mMarkersDataListFiltered.get(getAdapterPosition()));
        }
    }

    public interface InviteEventAdapterListener {
        void onContactsAdapterClicked(Contact.ContactData mContactData);
        void onStarClicked(Contact.ContactData mContactData, boolean isStarred);
    }

}
