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

public class FeedAdapter$CellNewUserJoinedViewHolder_ViewBinding implements Unbinder {
  private FeedAdapter.CellNewUserJoinedViewHolder target;

  @UiThread
  public FeedAdapter$CellNewUserJoinedViewHolder_ViewBinding(FeedAdapter.CellNewUserJoinedViewHolder target,
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
    target.inActiveFeedsLayout = Utils.findRequiredViewAsType(source, R.id.feeds_active_layout, "field 'inActiveFeedsLayout'", RelativeLayout.class);
    target.inActiveEntireLayout = Utils.findRequiredViewAsType(source, R.id.overall_active_layout, "field 'inActiveEntireLayout'", RelativeLayout.class);
    target.inActiveLikeLayout = Utils.findRequiredViewAsType(source, R.id.user_status_like_icon_layout, "field 'inActiveLikeLayout'", RelativeLayout.class);
    target.inActiveCommentLayout = Utils.findRequiredViewAsType(source, R.id.user_status_comment_icon_layout, "field 'inActiveCommentLayout'", RelativeLayout.class);
    target.icReportBottom = Utils.findRequiredViewAsType(source, R.id.ic_report_bottom, "field 'icReportBottom'", ImageView.class);
    target.newJoinLayout = Utils.findRequiredViewAsType(source, R.id.new_join_layout, "field 'newJoinLayout'", LinearLayout.class);
    target.newJoinIconProfile = Utils.findRequiredViewAsType(source, R.id.new_join_icon_profile, "field 'newJoinIconProfile'", SimpleDraweeView.class);
    target.newJoinIconText = Utils.findRequiredViewAsType(source, R.id.new_join_icon_text, "field 'newJoinIconText'", TextView.class);
    target.newJoinTop = Utils.findRequiredViewAsType(source, R.id.new_join_top, "field 'newJoinTop'", TextView.class);
    target.newJoinProfileName = Utils.findRequiredViewAsType(source, R.id.new_join_profile_name, "field 'newJoinProfileName'", TextView.class);
    target.newJoinedTime = Utils.findRequiredViewAsType(source, R.id.joined_time, "field 'newJoinedTime'", TextView.class);
    target.head = Utils.findRequiredViewAsType(source, R.id.head, "field 'head'", LinearLayout.class);
    target.newJoinDesignation = Utils.findRequiredViewAsType(source, R.id.new_join_designation, "field 'newJoinDesignation'", TextView.class);
    target.news = Utils.findRequiredViewAsType(source, R.id.news, "field 'news'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FeedAdapter.CellNewUserJoinedViewHolder target = this.target;
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
    target.inActiveFeedsLayout = null;
    target.inActiveEntireLayout = null;
    target.inActiveLikeLayout = null;
    target.inActiveCommentLayout = null;
    target.icReportBottom = null;
    target.newJoinLayout = null;
    target.newJoinIconProfile = null;
    target.newJoinIconText = null;
    target.newJoinTop = null;
    target.newJoinProfileName = null;
    target.newJoinedTime = null;
    target.head = null;
    target.newJoinDesignation = null;
    target.news = null;
  }
}
