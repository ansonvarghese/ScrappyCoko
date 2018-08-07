// Generated code from Butter Knife. Do not modify!
package com.myscrap;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.commonsware.cwac.camera.CameraView;
import com.myscrap.view.RevealBackgroundView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class TakePhotoActivity_ViewBinding implements Unbinder {
  private TakePhotoActivity target;

  private View view2131296370;

  private View view2131296358;

  @UiThread
  public TakePhotoActivity_ViewBinding(TakePhotoActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TakePhotoActivity_ViewBinding(final TakePhotoActivity target, View source) {
    this.target = target;

    View view;
    target.vRevealBackground = Utils.findRequiredViewAsType(source, R.id.vRevealBackground, "field 'vRevealBackground'", RevealBackgroundView.class);
    target.vTakePhotoRoot = Utils.findRequiredView(source, R.id.vPhotoRoot, "field 'vTakePhotoRoot'");
    target.vShutter = Utils.findRequiredView(source, R.id.vShutter, "field 'vShutter'");
    target.ivTakenPhoto = Utils.findRequiredViewAsType(source, R.id.ivTakenPhoto, "field 'ivTakenPhoto'", ImageView.class);
    target.vUpperPanel = Utils.findRequiredViewAsType(source, R.id.vUpperPanel, "field 'vUpperPanel'", ViewSwitcher.class);
    target.vLowerPanel = Utils.findRequiredViewAsType(source, R.id.vLowerPanel, "field 'vLowerPanel'", ViewSwitcher.class);
    target.cameraView = Utils.findRequiredViewAsType(source, R.id.cameraView, "field 'cameraView'", CameraView.class);
    target.rvFilters = Utils.findRequiredViewAsType(source, R.id.rvFilters, "field 'rvFilters'", RecyclerView.class);
    view = Utils.findRequiredView(source, R.id.btnTakePhoto, "field 'btnTakePhoto' and method 'onTakePhotoClick'");
    target.btnTakePhoto = Utils.castView(view, R.id.btnTakePhoto, "field 'btnTakePhoto'", Button.class);
    view2131296370 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onTakePhotoClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.btnAccept, "method 'onAcceptClick'");
    view2131296358 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onAcceptClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    TakePhotoActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.vRevealBackground = null;
    target.vTakePhotoRoot = null;
    target.vShutter = null;
    target.ivTakenPhoto = null;
    target.vUpperPanel = null;
    target.vLowerPanel = null;
    target.cameraView = null;
    target.rvFilters = null;
    target.btnTakePhoto = null;

    view2131296370.setOnClickListener(null);
    view2131296370 = null;
    view2131296358.setOnClickListener(null);
    view2131296358 = null;
  }
}
