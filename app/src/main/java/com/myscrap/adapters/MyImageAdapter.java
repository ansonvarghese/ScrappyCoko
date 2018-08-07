package com.myscrap.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.myscrap.MultiPhotoSelectActivity;
import com.myscrap.R;
import com.myscrap.model.GalleryModel;
import com.myscrap.utils.Helper;

import java.io.File;
import java.util.ArrayList;

public class MyImageAdapter extends RecyclerView.Adapter<MyImageAdapter.MyViewHolder> {
	private ArrayList<String> mList;
	private ArrayList<GalleryModel> galleryModels;
	private MultiPhotoSelectActivity activity;
	private int mode;
    private SparseBooleanArray mSparseBooleanArray;
    private int numberOfCheckboxesChecked = 0;
    private boolean isImageChecked = false;
    private String strCount;
    private String strName;

	public MyImageAdapter(MultiPhotoSelectActivity activity, ArrayList<String> imageList, ArrayList<GalleryModel> galleryModels) {
		this.activity = activity;
        mSparseBooleanArray = new SparseBooleanArray();
		mList = new ArrayList<>();
		this.mList = imageList;
		this.galleryModels = galleryModels;

		if (galleryModels == null) {
			mode = Helper.TAG_MODE_FILES;
		} else {
			mode = Helper.TAG_MODE_DIRECTORY;
		}


	}

    public ArrayList<String> getCheckedItems() {
        ArrayList<String> selectedImageArray = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            if (mSparseBooleanArray.get(i)) {
                selectedImageArray.add(mList.get(i));
            }
        }
        return selectedImageArray;
    }

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflater_show_image, parent, false);
		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {
        if (mode == Helper.TAG_MODE_FILES) {
            Glide.with(holder.imgShow.getContext()).load(new File(mList.get(holder.getAdapterPosition()))).into(holder.imgShow);
            holder.imageView.setBackgroundResource(R.drawable.deselect_image);
            holder.mCheckBox.setVisibility(View.VISIBLE);
        } else {
            final GalleryModel gm = galleryModels.get(position);
            Glide.with(holder.imgShow.getContext()).load(new File(gm.folderImagePath)).into(holder.imgShow);
            //Uri uri = Uri.parse("file://" + gm.folderImagePath);
            //holder.imgShow.setImageURI(uri);
            String strName = gm.folderName;
            String strCount;
            if (strName.length() > 20) {
                strName = strName.substring(0, 20)+"...";
            }
            strCount = ""+gm.folderImages.size();
            holder.txtFolderName.setVisibility(View.VISIBLE);
            if (position == 0){
                strCount = String.valueOf(gm.totalCount);
                if (!strCount.equalsIgnoreCase("0")){
                    holder.txtFolderImageCount.setText(strCount);
                    holder.txtFolderImageCount.setVisibility(View.VISIBLE);
                } else {
                    holder.txtFolderImageCount.setVisibility(View.GONE);
                }
                holder.txtFolderImageCount.setText(strCount);
            } else {
                holder.txtFolderImageCount.setText(strCount);
            }

            holder.txtFolderName.setText(strName);
            holder.mCheckBox.setVisibility(View.GONE);
        }
        holder.imageView.setTag(position);
        holder.imageView.setOnClickListener(clickListener);
        holder.mCheckBox.setTag(position);
        holder.mCheckBox.setChecked(mSparseBooleanArray.get(position));
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            isImageChecked = isChecked;

            if (isChecked && numberOfCheckboxesChecked >= 5) {
                holder.mCheckBox.setChecked(false);
                Toast.makeText(activity, "You reached maximum.", Toast.LENGTH_SHORT).show();
            } else {
                if (isChecked && numberOfCheckboxesChecked <= 5) {
                    numberOfCheckboxesChecked++;
                    mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
                } else {
                    numberOfCheckboxesChecked--;
                    mSparseBooleanArray.delete((Integer) buttonView.getTag());
                }
            }
            ArrayList<String> selectedItems = getCheckedItems();
            if (selectedItems.size() > 0) {
                activity.isShowCheckBox = true;
                activity.invalidateOptionsMenu();
                if (selectedItems.size() == 1) {
                    if (activity != null && activity.getSupportActionBar() != null) {
                        String count = selectedItems.size()+" selected";
                        activity.getSupportActionBar().setTitle(count);
                    }
                }  else {
                    if (activity != null && activity.getSupportActionBar() != null) {
                        String count = selectedItems.size()+" selected";
                        activity.getSupportActionBar().setTitle(count);
                    }
                }
            } else {
                activity.isShowCheckBox = false;
                activity.invalidateOptionsMenu();
                if (activity != null && activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().setTitle("");
                }
            }

        });
	}


    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            if (mode == Helper.TAG_MODE_DIRECTORY) {
                activity.setFilesFromFolder(position);
            }
        }
    };

	@Override
	public int getItemCount() {
		if (mode == Helper.TAG_MODE_DIRECTORY) {
			return galleryModels.size();
		} else {
			return mList.size();
		}
	}


    class MyViewHolder extends RecyclerView.ViewHolder {
		ImageView  imageView;
        ImageView imgShow;
		TextView txtFolderName,txtFolderImageCount;
        CheckBox mCheckBox;

		MyViewHolder(View convertView) {
			super(convertView);
			imgShow = (ImageView) convertView.findViewById(R.id.imgShowing);
			imageView = (ImageView) convertView.findViewById(R.id.imgSelector);
			txtFolderName = (TextView) convertView.findViewById(R.id.txtFolderName);
			txtFolderImageCount = (TextView) convertView.findViewById(R.id.txtFolderImageCount);
            mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
		}
    }
}