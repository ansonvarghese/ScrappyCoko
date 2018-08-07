// Generated code from Butter Knife. Do not modify!
package com.myscrap.view;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.myscrap.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoadingFeedItemView_ViewBinding implements Unbinder {
  private LoadingFeedItemView target;

  @UiThread
  public LoadingFeedItemView_ViewBinding(LoadingFeedItemView target) {
    this(target, target);
  }

  @UiThread
  public LoadingFeedItemView_ViewBinding(LoadingFeedItemView target, View source) {
    this.target = target;

    target.vSendingProgress = Utils.findRequiredViewAsType(source, R.id.vSendingProgress, "field 'vSendingProgress'", SendingProgressView.class);
    target.vProgressBg = Utils.findRequiredView(source, R.id.vProgressBg, "field 'vProgressBg'");
  }

  @Override
  @CallSuper
  public void unbind() {
    LoadingFeedItemView target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.vSendingProgress = null;
    target.vProgressBg = null;
  }
}
