// Generated code from Butter Knife. Do not modify!
package com.myscrap;

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
import com.myscrap.view.MultiTouchViewPager;
import java.lang.IllegalStateException;
import java.lang.Override;

public class NewsViewActivity_ViewBinding implements Unbinder {
  private NewsViewActivity target;

  @UiThread
  public NewsViewActivity_ViewBinding(NewsViewActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public NewsViewActivity_ViewBinding(NewsViewActivity target, View source) {
    this.target = target;

    target.btnComments = Utils.findRequiredViewAsType(source, R.id.btnComments, "field 'btnComments'", ImageButton.class);
    target.btnLike = Utils.findRequiredViewAsType(source, R.id.btnLike, "field 'btnLike'", ImageButton.class);
    target.vBgLike = Utils.findRequiredView(source, R.id.vBgLike, "field 'vBgLike'");
    target.ivFeedBottom = Utils.findRequiredViewAsType(source, R.id.ivFeedBottom, "field 'ivFeedBottom'", ImageView.class);
    target.ivLike = Utils.findRequiredViewAsType(source, R.id.ivLike, "field 'ivLike'", ImageView.class);
    target.like = Utils.findRequiredViewAsType(source, R.id.like_text, "field 'like'", TextView.class);
    target.comment = Utils.findRequiredViewAsType(source, R.id.comment, "field 'comment'", TextView.class);
    target.vImageRoot = Utils.findRequiredViewAsType(source, R.id.vImageRoot, "field 'vImageRoot'", FrameLayout.class);
    target.tsLikesCounter = Utils.findRequiredViewAsType(source, R.id.tsLikesCounter, "field 'tsLikesCounter'", TextSwitcher.class);
    target.tsLikesCommentDot = Utils.findRequiredViewAsType(source, R.id.dot, "field 'tsLikesCommentDot'", TextView.class);
    target.tsCommentCounter = Utils.findRequiredViewAsType(source, R.id.tsCommentsCounter, "field 'tsCommentCounter'", TextSwitcher.class);
    target.viewPager = Utils.findRequiredViewAsType(source, R.id.view_pager, "field 'viewPager'", MultiTouchViewPager.class);
    target.leftRightLayout = Utils.findRequiredViewAsType(source, R.id.left_right_layout, "field 'leftRightLayout'", RelativeLayout.class);
    target.viewPagerLayout = Utils.findRequiredViewAsType(source, R.id.view_pager_layout, "field 'viewPagerLayout'", FrameLayout.class);
    target.left = Utils.findRequiredViewAsType(source, R.id.left, "field 'left'", ImageView.class);
    target.right = Utils.findRequiredViewAsType(source, R.id.right, "field 'right'", ImageView.class);
    target.dotsLayout = Utils.findRequiredViewAsType(source, R.id.layoutDots, "field 'dotsLayout'", LinearLayout.class);
    target.heading = Utils.findRequiredViewAsType(source, R.id.heading, "field 'heading'", TextView.class);
    target.subHeading = Utils.findRequiredViewAsType(source, R.id.sub_head_lines, "field 'subHeading'", TextView.class);
    target.status = Utils.findRequiredViewAsType(source, R.id.status, "field 'status'", TextView.class);
    target.time = Utils.findRequiredViewAsType(source, R.id.time, "field 'time'", TextView.class);
    target.author = Utils.findRequiredViewAsType(source, R.id.author, "field 'author'", TextView.class);
    target.newsLink = Utils.findRequiredViewAsType(source, R.id.news_link, "field 'newsLink'", TextView.class);
    target.authorCompany = Utils.findRequiredViewAsType(source, R.id.author_company, "field 'authorCompany'", TextView.class);
    target.location = Utils.findRequiredViewAsType(source, R.id.location, "field 'location'", TextView.class);
    target.rootBottomView = Utils.findRequiredViewAsType(source, R.id.root_bottom_view, "field 'rootBottomView'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    NewsViewActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnComments = null;
    target.btnLike = null;
    target.vBgLike = null;
    target.ivFeedBottom = null;
    target.ivLike = null;
    target.like = null;
    target.comment = null;
    target.vImageRoot = null;
    target.tsLikesCounter = null;
    target.tsLikesCommentDot = null;
    target.tsCommentCounter = null;
    target.viewPager = null;
    target.leftRightLayout = null;
    target.viewPagerLayout = null;
    target.left = null;
    target.right = null;
    target.dotsLayout = null;
    target.heading = null;
    target.subHeading = null;
    target.status = null;
    target.time = null;
    target.author = null;
    target.newsLink = null;
    target.authorCompany = null;
    target.location = null;
    target.rootBottomView = null;
  }
}
