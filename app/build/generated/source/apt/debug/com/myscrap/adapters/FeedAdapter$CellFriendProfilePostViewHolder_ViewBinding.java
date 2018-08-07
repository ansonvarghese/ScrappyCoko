// Generated code from Butter Knife. Do not modify!
package com.myscrap.adapters;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.view.MultiTouchViewPager;
import java.lang.IllegalStateException;
import java.lang.Override;

public class FeedAdapter$CellFriendProfilePostViewHolder_ViewBinding implements Unbinder {
  private FeedAdapter.CellFriendProfilePostViewHolder target;

  @UiThread
  public FeedAdapter$CellFriendProfilePostViewHolder_ViewBinding(FeedAdapter.CellFriendProfilePostViewHolder target,
      View source) {
    this.target = target;

    target.btnReport = Utils.findRequiredViewAsType(source, R.id.ic_report, "field 'btnReport'", ImageView.class);
    target.btnComments = Utils.findRequiredViewAsType(source, R.id.btnComments, "field 'btnComments'", ImageButton.class);
    target.btnLike = Utils.findRequiredViewAsType(source, R.id.btnLike, "field 'btnLike'", ImageButton.class);
    target.vBgLike = Utils.findRequiredView(source, R.id.vBgLike, "field 'vBgLike'");
    target.tsLikesCounter = Utils.findRequiredViewAsType(source, R.id.tsLikesCounter, "field 'tsLikesCounter'", TextSwitcher.class);
    target.likeText = Utils.findRequiredViewAsType(source, R.id.like_text, "field 'likeText'", TextView.class);
    target.tsLikesCommentDot = Utils.findRequiredViewAsType(source, R.id.dot, "field 'tsLikesCommentDot'", TextView.class);
    target.tsCommentCounter = Utils.findRequiredViewAsType(source, R.id.tsCommentsCounter, "field 'tsCommentCounter'", TextSwitcher.class);
    target.comment = Utils.findRequiredViewAsType(source, R.id.comment, "field 'comment'", TextView.class);
    target.vImageRoot = Utils.findRequiredViewAsType(source, R.id.vImageRoot, "field 'vImageRoot'", LinearLayout.class);
    target.viewPager = Utils.findRequiredViewAsType(source, R.id.view_pager, "field 'viewPager'", MultiTouchViewPager.class);
    target.leftRightLayout = Utils.findRequiredViewAsType(source, R.id.left_right_layout, "field 'leftRightLayout'", RelativeLayout.class);
    target.viewPagerLayout = Utils.findRequiredViewAsType(source, R.id.view_pager_layout, "field 'viewPagerLayout'", FrameLayout.class);
    target.left = Utils.findRequiredViewAsType(source, R.id.left, "field 'left'", ImageView.class);
    target.right = Utils.findRequiredViewAsType(source, R.id.right, "field 'right'", ImageView.class);
    target.dotsLayout = Utils.findRequiredViewAsType(source, R.id.layoutDots, "field 'dotsLayout'", LinearLayout.class);
    target.iconFront = Utils.findRequiredViewAsType(source, R.id.icon_front, "field 'iconFront'", RelativeLayout.class);
    target.iconProfile = Utils.findRequiredViewAsType(source, R.id.icon_profile, "field 'iconProfile'", SimpleDraweeView.class);
    target.iconBadge = Utils.findRequiredViewAsType(source, R.id.icon_badge, "field 'iconBadge'", ImageView.class);
    target.overflow = Utils.findRequiredViewAsType(source, R.id.overflow, "field 'overflow'", ImageView.class);
    target.favourite = Utils.findRequiredViewAsType(source, R.id.favourite, "field 'favourite'", ImageView.class);
    target.iconText = Utils.findRequiredViewAsType(source, R.id.icon_text, "field 'iconText'", TextView.class);
    target.points = Utils.findRequiredViewAsType(source, R.id.points, "field 'points'", TextView.class);
    target.profileName = Utils.findRequiredViewAsType(source, R.id.profileName, "field 'profileName'", TextView.class);
    target.company = Utils.findRequiredViewAsType(source, R.id.company, "field 'company'", TextView.class);
    target.designation = Utils.findRequiredViewAsType(source, R.id.designation, "field 'designation'", TextView.class);
    target.timeStamp = Utils.findRequiredViewAsType(source, R.id.time, "field 'timeStamp'", TextView.class);
    target.inActiveFeedsLayout = Utils.findRequiredViewAsType(source, R.id.feeds_active_layout, "field 'inActiveFeedsLayout'", RelativeLayout.class);
    target.inActiveEntireLayout = Utils.findRequiredViewAsType(source, R.id.overall_active_layout, "field 'inActiveEntireLayout'", RelativeLayout.class);
    target.inActiveLikeLayout = Utils.findRequiredViewAsType(source, R.id.user_status_like_icon_layout, "field 'inActiveLikeLayout'", RelativeLayout.class);
    target.inActiveCommentLayout = Utils.findRequiredViewAsType(source, R.id.user_status_comment_icon_layout, "field 'inActiveCommentLayout'", RelativeLayout.class);
    target.icReportBottom = Utils.findRequiredViewAsType(source, R.id.ic_report_bottom, "field 'icReportBottom'", ImageView.class);
    target.news = Utils.findRequiredViewAsType(source, R.id.news, "field 'news'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FeedAdapter.CellFriendProfilePostViewHolder target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnReport = null;
    target.btnComments = null;
    target.btnLike = null;
    target.vBgLike = null;
    target.tsLikesCounter = null;
    target.likeText = null;
    target.tsLikesCommentDot = null;
    target.tsCommentCounter = null;
    target.comment = null;
    target.vImageRoot = null;
    target.viewPager = null;
    target.leftRightLayout = null;
    target.viewPagerLayout = null;
    target.left = null;
    target.right = null;
    target.dotsLayout = null;
    target.iconFront = null;
    target.iconProfile = null;
    target.iconBadge = null;
    target.overflow = null;
    target.favourite = null;
    target.iconText = null;
    target.points = null;
    target.profileName = null;
    target.company = null;
    target.designation = null;
    target.timeStamp = null;
    target.inActiveFeedsLayout = null;
    target.inActiveEntireLayout = null;
    target.inActiveLikeLayout = null;
    target.inActiveCommentLayout = null;
    target.icReportBottom = null;
    target.news = null;
  }
}
