// Generated by view binder compiler. Do not edit!
package com.partko.matchthree.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.partko.matchthree.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityArcadeLevelsBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final AppBarLayout appBar;

  @NonNull
  public final Toolbar toolbar;

  @NonNull
  public final CollapsingToolbarLayout toolbarLayout;

  private ActivityArcadeLevelsBinding(@NonNull CoordinatorLayout rootView,
      @NonNull AppBarLayout appBar, @NonNull Toolbar toolbar,
      @NonNull CollapsingToolbarLayout toolbarLayout) {
    this.rootView = rootView;
    this.appBar = appBar;
    this.toolbar = toolbar;
    this.toolbarLayout = toolbarLayout;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityArcadeLevelsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityArcadeLevelsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_arcade_levels, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityArcadeLevelsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.app_bar;
      AppBarLayout appBar = ViewBindings.findChildViewById(rootView, id);
      if (appBar == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      id = R.id.toolbar_layout;
      CollapsingToolbarLayout toolbarLayout = ViewBindings.findChildViewById(rootView, id);
      if (toolbarLayout == null) {
        break missingId;
      }

      return new ActivityArcadeLevelsBinding((CoordinatorLayout) rootView, appBar, toolbar,
          toolbarLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}