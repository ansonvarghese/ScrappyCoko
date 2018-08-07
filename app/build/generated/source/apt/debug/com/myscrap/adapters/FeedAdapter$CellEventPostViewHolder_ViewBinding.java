// Generated code from Butter Knife. Do not modify!
package com.myscrap.adapters;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
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
import java.lang.IllegalStateException;
import java.lang.Override;

public class FeedAdapter$CellEventPostViewHolder_ViewBinding implements Unbinder {
  private FeedAdapter.CellEventPostViewHolder target;

  @UiThread
  public FeedAdapter$CellEventPostViewHolder_ViewBinding(FeedAdapter.CellEventPostViewHolder target,
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
    target.date = Utils.findRequiredViewAsType(source, R.id.date, "field 'date'", TextView.class);
    target.month = Utils.findRequiredViewAsType(source, R.id.month, "field 'month'", TextView.class);
    target.eventName = Utils.findRequiredViewAsType(source, R.id.event_name, "field 'eventName'", TextView.class);
    target.description = Utils.findRequiredViewAsType(source, R.id.description, "field 'description'", TextView.class);
    target.interestTv = Utils.findRequiredViewAsType(source, R.id.interestTv, "field 'interestTv'", TextView.class);
    target.interestIv = Utils.findRequiredViewAsType(source, R.id.interestIv, "field 'interestIv'", ImageView.class);
    target.eventLayout = Utils.findRequiredViewAsType(source, R.id.event_layout, "field 'eventLayout'", LinearLayout.class);
    target.mSimpleDraweeView = Utils.findRequiredViewAsType(source, R.id.event_image, "field 'mSimpleDraweeView'", SimpleDraweeView.class);
    target.news = Utils.findOptionalViewAsType(source, R.id.news, "field 'news'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FeedAdapter.CellEventPostViewHolder target = this.target;
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
    target.date = null;
    target.month = null;
    target.eventName = null;
    target.description = null;
    target.interestTv = null;
    target.interestIv = null;
    target.eventLayout = null;
    target.mSimpleDraweeView = null;
    target.news = null;
  }
}
