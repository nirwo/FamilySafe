// Generated by view binder compiler. Do not edit!
package nir.wolff.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import nir.wolff.R;

public final class ActivityMapBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final ExtendedFloatingActionButton refreshButton;

  private ActivityMapBinding(@NonNull CoordinatorLayout rootView,
      @NonNull ExtendedFloatingActionButton refreshButton) {
    this.rootView = rootView;
    this.refreshButton = refreshButton;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMapBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMapBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_map, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMapBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.refreshButton;
      ExtendedFloatingActionButton refreshButton = ViewBindings.findChildViewById(rootView, id);
      if (refreshButton == null) {
        break missingId;
      }

      return new ActivityMapBinding((CoordinatorLayout) rootView, refreshButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
