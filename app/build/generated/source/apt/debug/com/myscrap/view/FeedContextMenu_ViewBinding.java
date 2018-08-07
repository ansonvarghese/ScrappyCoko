// Generated code from Butter Knife. Do not modify!
package com.myscrap.view;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.myscrap.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class FeedContextMenu_ViewBinding implements Unbinder {
  private FeedContextMenu target;

  private View view2131296368;

  private View view2131296369;

  private View view2131296365;

  private View view2131296360;

  @UiThread
  public FeedContextMenu_ViewBinding(FeedContextMenu target) {
    this(target, target);
  }

  @UiThread
  public FeedContextMenu_ViewBinding(final FeedContextMenu target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.btnReport, "method 'onReportClick'");
    view2131296368 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onReportClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.btnSharePhoto, "method 'onSharePhotoClick'");
    view2131296369 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onSharePhotoClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.btnCopyShareUrl, "method 'onCopyShareUrlClick'");
    view2131296365 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onCopyShareUrlClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.btnCancel, "method 'onCancelClick'");
    view2131296360 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onCancelClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    target = null;


    view2131296368.setOnClickListener(null);
    view2131296368 = null;
    view2131296369.setOnClickListener(null);
    view2131296369 = null;
    view2131296365.setOnClickListener(null);
    view2131296365 = null;
    view2131296360.setOnClickListener(null);
    view2131296360 = null;
  }
}
