package com.myscrap.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.CompanyImagesSlideshowDialogFragment;
import com.myscrap.R;
import com.myscrap.model.PictureUrl;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ms3 on 7/6/2017.
 */

public class GridLayoutAdapter extends CustomRecyclerViewAdapter {
    private Activity activity;
    private List<PictureUrl> images;

    public GridLayoutAdapter(Activity activity, List<PictureUrl> images) {
        this.activity = activity;
        this.images = images;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display;
        if (wm != null) {
            display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
        }

    }

    @Override
    public CustomRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.grid_images, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final CustomRecycleViewHolder holder, int position) {
        final Holder myHolder = (Holder) holder;
        final PictureUrl mPictureUrl = images.get(position);
        if (mPictureUrl.getImages() != null) {
            Uri uri = Uri.parse(mPictureUrl.getImages());
            myHolder.images.setImageURI(uri);

            myHolder.images.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", (Serializable) images);
                bundle.putInt("position", myHolder.getAdapterPosition());
                FragmentTransaction ft = ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction();
                CompanyImagesSlideshowDialogFragment newFragment = CompanyImagesSlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            });
        }

        if(mPictureUrl.isReported()){
            myHolder.inActiveLayout.setVisibility(View.VISIBLE);
        } else {
            myHolder.inActiveLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class Holder extends CustomRecycleViewHolder {
        private SimpleDraweeView images;
        private RelativeLayout inActiveLayout;

        public Holder(View itemView) {
            super(itemView);
            images = (SimpleDraweeView) itemView.findViewById(R.id.ivItemGridImage);
            inActiveLayout = (RelativeLayout) itemView.findViewById(R.id.overall_active_layout);
        }
    }
}
