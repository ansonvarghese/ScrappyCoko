// Generated code from Butter Knife. Do not modify!
package com.myscrap;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SingleNotificationActivity_ViewBinding implements Unbinder {
  private SingleNotificationActivity target;

  @UiThread
  public SingleNotificationActivity_ViewBinding(SingleNotificationActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SingleNotificationActivity_ViewBinding(SingleNotificationActivity target, View source) {
    this.target = target;

    target.ivFeedCenter = Utils.findRequiredViewAsType(source, R.id.ivFeedCenter, "field 'ivFeedCenter'", ImageView.class);
    target.btnComments = Utils.findRequiredViewAsType(source, R.id.btnComments, "field 'btnComments'", ImageButton.class);
    target.btnLike = Utils.findRequiredViewAsType(source, R.id.btnLike, "field 'btnLike'", ImageButton.class);
    target.vBgLike = Utils.findRequiredView(source, R.id.vBgLike, "field 'vBgLike'");
    target.ivLike = Utils.findRequiredViewAsType(source, R.id.ivLike, "field 'ivLike'", ImageView.class);
    target.tsLikesCounter = Utils.findRequiredViewAsType(source, R.id.tsLikesCounter, "field 'tsLikesCounter'", TextSwitcher.class);
    target.tsCommentCounter = Utils.findRequiredViewAsType(source, R.id.tsCommentsCounter, "field 'tsCommentCounter'", TextSwitcher.class);
    target.vImageRoot = Utils.findRequiredViewAsType(source, R.id.vImageRoot, "field 'vImageRoot'", FrameLayout.class);
    target.iconBack = Utils.findRequiredViewAsType(source, R.id.icon_back, "field 'iconBack'", RelativeLayout.class);
    target.iconFront = Utils.findRequiredViewAsType(source, R.id.icon_front, "field 'iconFront'", RelativeLayout.class);
    target.iconProfile = Utils.findRequiredViewAsType(source, R.id.icon_profile, "field 'iconProfile'", ImageView.class);
    target.overflow = Utils.findOptionalViewAsType(source, R.id.overflow, "field 'overflow'", ImageView.class);
    target.iconText = Utils.findRequiredViewAsType(source, R.id.icon_text, "field 'iconText'", TextView.class);
    target.profileName = Utils.findRequiredViewAsType(source, R.id.profileName, "field 'profileName'", TextView.class);
    target.company = Utils.findRequiredViewAsType(source, R.id.company, "field 'company'", TextView.class);
    target.designation = Utils.findRequiredViewAsType(source, R.id.designation, "field 'designation'", TextView.class);
    target.status = Utils.findRequiredViewAsType(source, R.id.status, "field 'status'", TextView.class);
    target.timeStamp = Utils.findRequiredViewAsType(source, R.id.time, "field 'timeStamp'", TextView.class);
    target.cardView = Utils.findRequiredViewAsType(source, R.id.card_view, "field 'cardView'", CardView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SingleNotificationActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.ivFeedCenter = null;
    target.btnComments = null;
    target.btnLike = null;
    target.vBgLike = null;
    target.ivLike = null;
    target.tsLikesCounter = null;
    target.tsCommentCounter = null;
    target.vImageRoot = null;
    target.iconBack = null;
    target.iconFront = null;
    target.iconProfile = null;
    target.overflow = null;
    target.iconText = null;
    target.profileName = null;
    target.company = null;
    target.designation = null;
    target.status = null;
    target.timeStamp = null;
    target.cardView = null;
  }
}
