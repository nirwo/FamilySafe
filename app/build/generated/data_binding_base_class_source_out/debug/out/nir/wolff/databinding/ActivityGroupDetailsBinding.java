// Generated by view binder compiler. Do not edit!
package nir.wolff.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import nir.wolff.R;

public final class ActivityGroupDetailsBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final ExtendedFloatingActionButton addMemberFab;

  @NonNull
  public final CardView groupInfoCard;

  @NonNull
  public final TextView groupNameText;

  @NonNull
  public final TextView memberCountText;

  @NonNull
  public final RecyclerView membersRecyclerView;

  @NonNull
  public final TabLayout tabLayout;

  @NonNull
  public final Toolbar toolbar;

  @NonNull
  public final ViewPager2 viewPager;

  private ActivityGroupDetailsBinding(@NonNull CoordinatorLayout rootView,
      @NonNull ExtendedFloatingActionButton addMemberFab, @NonNull CardView groupInfoCard,
      @NonNull TextView groupNameText, @NonNull TextView memberCountText,
      @NonNull RecyclerView membersRecyclerView, @NonNull TabLayout tabLayout,
      @NonNull Toolbar toolbar, @NonNull ViewPager2 viewPager) {
    this.rootView = rootView;
    this.addMemberFab = addMemberFab;
    this.groupInfoCard = groupInfoCard;
    this.groupNameText = groupNameText;
    this.memberCountText = memberCountText;
    this.membersRecyclerView = membersRecyclerView;
    this.tabLayout = tabLayout;
    this.toolbar = toolbar;
    this.viewPager = viewPager;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityGroupDetailsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityGroupDetailsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_group_details, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityGroupDetailsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.addMemberFab;
      ExtendedFloatingActionButton addMemberFab = ViewBindings.findChildViewById(rootView, id);
      if (addMemberFab == null) {
        break missingId;
      }

      id = R.id.groupInfoCard;
      CardView groupInfoCard = ViewBindings.findChildViewById(rootView, id);
      if (groupInfoCard == null) {
        break missingId;
      }

      id = R.id.groupNameText;
      TextView groupNameText = ViewBindings.findChildViewById(rootView, id);
      if (groupNameText == null) {
        break missingId;
      }

      id = R.id.memberCountText;
      TextView memberCountText = ViewBindings.findChildViewById(rootView, id);
      if (memberCountText == null) {
        break missingId;
      }

      id = R.id.membersRecyclerView;
      RecyclerView membersRecyclerView = ViewBindings.findChildViewById(rootView, id);
      if (membersRecyclerView == null) {
        break missingId;
      }

      id = R.id.tabLayout;
      TabLayout tabLayout = ViewBindings.findChildViewById(rootView, id);
      if (tabLayout == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      id = R.id.viewPager;
      ViewPager2 viewPager = ViewBindings.findChildViewById(rootView, id);
      if (viewPager == null) {
        break missingId;
      }

      return new ActivityGroupDetailsBinding((CoordinatorLayout) rootView, addMemberFab,
          groupInfoCard, groupNameText, memberCountText, membersRecyclerView, tabLayout, toolbar,
          viewPager);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
