// Generated code from Butter Knife. Do not modify!
package com.myscrap;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.myscrap.utils.LinkPreview;
import java.lang.IllegalStateException;
import java.lang.Override;

public class DetailedPostActivity_ViewBinding implements Unbinder {
  private DetailedPostActivity target;

  @UiThread
  public DetailedPostActivity_ViewBinding(DetailedPostActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public DetailedPostActivity_ViewBinding(DetailedPostActivity target, View source) {
    this.target = target;

    target.ivFeedCenter = Utils.findRequiredViewAsType(source, R.id.ivFeedCenter, "field 'ivFeedCenter'", SimpleDraweeView.class);
    target.btnReport = Utils.findRequiredViewAsType(source, R.id.ic_report, "field 'btnReport'", ImageView.class);
    target.btnComments = Utils.findRequiredViewAsType(source, R.id.btnComments, "field 'btnComments'", ImageButton.class);
    target.btnLike = Utils.findRequiredViewAsType(source, R.id.btnLike, "field 'btnLike'", ImageButton.class);
    target.vBgLike = Utils.findRequiredView(source, R.id.vBgLike, "field 'vBgLike'");
    target.ivLike = Utils.findRequiredViewAsType(source, R.id.ivLike, "field 'ivLike'", ImageView.class);
    target.tsLikesCounter = Utils.findRequiredViewAsType(source, R.id.tsLikesCounter, "field 'tsLikesCounter'", TextSwitcher.class);
    target.tsCommentCounter = Utils.findRequiredViewAsType(source, R.id.tsCommentsCounter, "field 'tsCommentCounter'", TextSwitcher.class);
    target.ivwhoLike = Utils.findRequiredViewAsType(source, R.id.whoLike, "field 'ivwhoLike'", ImageView.class);
    target.tsWhoLikes = Utils.findRequiredViewAsType(source, R.id.tsWhoLikes, "field 'tsWhoLikes'", TextSwitcher.class);
    target.tsWhoComments = Utils.findRequiredViewAsType(source, R.id.tsWhoComments, "field 'tsWhoComments'", TextSwitcher.class);
    target.whoLikeLayout = Utils.findRequiredViewAsType(source, R.id.whose_like, "field 'whoLikeLayout'", LinearLayout.class);
    target.commentsLayout = Utils.findRequiredViewAsType(source, R.id.comments, "field 'commentsLayout'", LinearLayout.class);
    target.rootBottomView = Utils.findRequiredViewAsType(source, R.id.root_bottom_view, "field 'rootBottomView'", LinearLayout.class);
    target.commentsRv = Utils.findRequiredViewAsType(source, R.id.comment_rv, "field 'commentsRv'", RecyclerView.class);
    target.nestedScrollView = Utils.findRequiredViewAsType(source, R.id.nested, "field 'nestedScrollView'", NestedScrollView.class);
    target.iconBadge = Utils.findRequiredViewAsType(source, R.id.icon_badge, "field 'iconBadge'", ImageView.class);
    target.top = Utils.findRequiredViewAsType(source, R.id.top, "field 'top'", TextView.class);
    target.points = Utils.findRequiredViewAsType(source, R.id.points, "field 'points'", TextView.class);
    target.vImageRoot = Utils.findRequiredViewAsType(source, R.id.vImageRoot, "field 'vImageRoot'", FrameLayout.class);
    target.hasComments = Utils.findRequiredViewAsType(source, R.id.has_comments, "field 'hasComments'", RelativeLayout.class);
    target.iconFront = Utils.findRequiredViewAsType(source, R.id.icon_front, "field 'iconFront'", RelativeLayout.class);
    target.iconProfile = Utils.findRequiredViewAsType(source, R.id.icon_profile, "field 'iconProfile'", SimpleDraweeView.class);
    target.overflow = Utils.findOptionalViewAsType(source, R.id.overflow, "field 'overflow'", ImageView.class);
    target.favourite = Utils.findRequiredViewAsType(source, R.id.favourite, "field 'favourite'", ImageView.class);
    target.iconText = Utils.findRequiredViewAsType(source, R.id.icon_text, "field 'iconText'", TextView.class);
    target.profileName = Utils.findRequiredViewAsType(source, R.id.profileName, "field 'profileName'", TextView.class);
    target.company = Utils.findRequiredViewAsType(source, R.id.company, "field 'company'", TextView.class);
    target.designation = Utils.findRequiredViewAsType(source, R.id.designation, "field 'designation'", TextView.class);
    target.status = Utils.findRequiredViewAsType(source, R.id.status, "field 'status'", TextView.class);
    target.timeStamp = Utils.findRequiredViewAsType(source, R.id.time, "field 'timeStamp'", TextView.class);
    target.cardViewPreview = Utils.findRequiredViewAsType(source, R.id.cardViewPreview, "field 'cardViewPreview'", CardView.class);
    target.mLinkPreview = Utils.findRequiredViewAsType(source, R.id.preview, "field 'mLinkPreview'", LinkPreview.class);
    target.feedsMainLayout = Utils.findRequiredViewAsType(source, R.id.feeds_main, "field 'feedsMainLayout'", LinearLayout.class);
    target.newJoinLayout = Utils.findRequiredViewAsType(source, R.id.new_join_layout, "field 'newJoinLayout'", LinearLayout.class);
    target.head = Utils.findRequiredViewAsType(source, R.id.head, "field 'head'", LinearLayout.class);
    target.newJoinedTime = Utils.findRequiredViewAsType(source, R.id.joined_time, "field 'newJoinedTime'", TextView.class);
    target.newJoinIconProfile = Utils.findRequiredViewAsType(source, R.id.new_join_icon_profile, "field 'newJoinIconProfile'", SimpleDraweeView.class);
    target.newJoinIconText = Utils.findRequiredViewAsType(source, R.id.new_join_icon_text, "field 'newJoinIconText'", TextView.class);
    target.newJoinTop = Utils.findRequiredViewAsType(source, R.id.new_join_top, "field 'newJoinTop'", TextView.class);
    target.news = Utils.findRequiredViewAsType(source, R.id.news, "field 'news'", TextView.class);
    target.newJoinProfileName = Utils.findRequiredViewAsType(source, R.id.new_join_profile_name, "field 'newJoinProfileName'", TextView.class);
    target.newJoinDesignation = Utils.findRequiredViewAsType(source, R.id.new_join_designation, "field 'newJoinDesignation'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    DetailedPostActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.ivFeedCenter = null;
    target.btnReport = null;
    target.btnComments = null;
    target.btnLike = null;
    target.vBgLike = null;
    target.ivLike = null;
    target.tsLikesCounter = null;
    target.tsCommentCounter = null;
    target.ivwhoLike = null;
    target.tsWhoLikes = null;
    target.tsWhoComments = null;
    target.whoLikeLayout = null;
    target.commentsLayout = null;
    target.rootBottomView = null;
    target.commentsRv = null;
    target.nestedScrollView = null;
    target.iconBadge = null;
    target.top = null;
    target.points = null;
    target.vImageRoot = null;
    target.hasComments = null;
    target.iconFront = null;
    target.iconProfile = null;
    target.overflow = null;
    target.favourite = null;
    target.iconText = null;
    target.profileName = null;
    target.company = null;
    target.designation = null;
    target.status = null;
    target.timeStamp = null;
    target.cardViewPreview = null;
    target.mLinkPreview = null;
    target.feedsMainLayout = null;
    target.newJoinLayout = null;
    target.head = null;
    target.newJoinedTime = null;
    target.newJoinIconProfile = null;
    target.newJoinIconText = null;
    target.newJoinTop = null;
    target.news = null;
    target.newJoinProfileName = null;
    target.newJoinDesignation = null;
  }
}
