package com.myscrap.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.myscrap.R;
import com.myscrap.utils.DeviceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedItemAnimator extends DefaultItemAnimator {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimationsMap = new HashMap<>();
    private Map<RecyclerView.ViewHolder, AnimatorSet> heartAnimationsMap = new HashMap<>();

    private int lastAddAnimatedItem = -2;

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
                                                     @NonNull RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags, @NonNull List<Object> payloads) {
        if (changeFlags == FLAG_CHANGED) {
            for (Object payload : payloads) {
                if (payload instanceof String) {
                    return new FeedItemHolderInfo((String) payload);
                }
            }
        }

        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == FeedsAdapter.VIEW_TYPE_DEFAULT) {
            if (viewHolder.getLayoutPosition() > lastAddAnimatedItem) {
                lastAddAnimatedItem++;
                runEnterAnimation((FeedsAdapter.CellFeedViewHolder) viewHolder);
                return false;
            }
        }

        dispatchAddFinished(viewHolder);
        return false;
    }

    private void runEnterAnimation(final FeedsAdapter.CellFeedViewHolder holder) {
        final int screenHeight = DeviceUtils.getScreenHeight(holder.itemView.getContext());
        holder.itemView.setTranslationY(screenHeight);
        holder.itemView.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(700)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dispatchAddFinished(holder);
                    }
                })
                .start();
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                 @NonNull RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preInfo,
                                 @NonNull ItemHolderInfo postInfo) {
        cancelCurrentAnimationIfExists(newHolder);

        if (preInfo instanceof FeedItemHolderInfo) {
            FeedItemHolderInfo feedItemHolderInfo = (FeedItemHolderInfo) preInfo;
            FeedsAdapter.CellFeedViewHolder holder = (FeedsAdapter.CellFeedViewHolder) newHolder;

            animateHeartButton(holder, holder.getFeedItem().isLikeStatus());
            updateLikesCounter(holder, holder.getFeedItem().getLikeCount());
            if (FeedsAdapter.ACTION_LIKE_IMAGE_CLICKED.equals(feedItemHolderInfo.updateAction)) {
           //     animatePhotoLike(holder);
            }
        }

        return false;
    }

    private void cancelCurrentAnimationIfExists(RecyclerView.ViewHolder item) {
        if (likeAnimationsMap.containsKey(item)) {
            likeAnimationsMap.get(item).cancel();
        }
        if (heartAnimationsMap.containsKey(item)) {
            heartAnimationsMap.get(item).cancel();
        }
    }

    private void animateHeartButton(final FeedsAdapter.CellFeedViewHolder holder, final boolean likeStatus) {
        AnimatorSet animatorSet = new AnimatorSet();

        /*ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.btnLike, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);*/

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.btnLike, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.btnLike, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

                holder.btnLike.post(() -> {
                    if(likeStatus)
                        holder.btnLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                    else
                        holder.btnLike.setImageResource(R.drawable.ic_heart_outline_black);
                });

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                heartAnimationsMap.remove(holder);
                dispatchChangeFinishedIfAllAnimationsEnded(holder);
            }
        });

        //animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();

        heartAnimationsMap.put(holder, animatorSet);
    }

    private void updateLikesCounter(FeedsAdapter.CellFeedViewHolder holder, int toValue) {

        if(toValue != 0){
            String likesCountTextFrom = holder.tsLikesCounter.getResources().getQuantityString(
                    R.plurals.likes_count, toValue, toValue
            );
            holder.tsLikesCounter.setCurrentText(likesCountTextFrom);
            holder.tsLikesCounter.setText(likesCountTextFrom);
        } else {
            holder.tsLikesCounter.setCurrentText("Like");
            holder.tsLikesCounter.setText("Like");
        }

    }

    /*private void animatePhotoLike(final FeedsAdapter.CellFeedViewHolder holder) {
 //       holder.vBgLike.setVisibility(View.VISIBLE);
        //holder.ivLike.setVisibility(View.VISIBLE);

 //       holder.vBgLike.setScaleY(0.1f);
 //       holder.vBgLike.setScaleX(0.1f);
 //       holder.vBgLike.setAlpha(1f);
      //  holder.ivLike.setScaleY(0.1f);
      //  holder.ivLike.setScaleX(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(200);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(200);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(200);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                likeAnimationsMap.remove(holder);
                resetLikeAnimationState(holder);
                dispatchChangeFinishedIfAllAnimationsEnded(holder);
            }
        });
        animatorSet.start();

        likeAnimationsMap.put(holder, animatorSet);
    }*/

    private void dispatchChangeFinishedIfAllAnimationsEnded(FeedsAdapter.CellFeedViewHolder holder) {
        if (likeAnimationsMap.containsKey(holder) || heartAnimationsMap.containsKey(holder)) {
            return;
        }

        dispatchAnimationFinished(holder);
    }

    private void resetLikeAnimationState(FeedsAdapter.CellFeedViewHolder holder) {
      //  holder.vBgLike.setVisibility(View.INVISIBLE);
       // holder.ivLike.setVisibility(View.INVISIBLE);
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        super.endAnimation(item);
        cancelCurrentAnimationIfExists(item);
    }

    @Override
    public void endAnimations() {
        super.endAnimations();
        for (AnimatorSet animatorSet : likeAnimationsMap.values()) {
            animatorSet.cancel();
        }
    }

    public static class FeedItemHolderInfo extends ItemHolderInfo {
        public String updateAction;

        public FeedItemHolderInfo(String updateAction) {
            this.updateAction = updateAction;
        }
    }
}
