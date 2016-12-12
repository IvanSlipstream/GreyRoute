package com.gmsworldwide.kharlamov.greyroute.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;

/**
 * Created by Slipstream on 09.12.2016 in GreyRoute.
 */
public class SwitchFrameLayoutBehavior extends CoordinatorLayout.Behavior<FrameLayout> {
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return (dependency instanceof Switch) || (dependency instanceof AppBarLayout);
    }

    public SwitchFrameLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        int paddingTop = child.getPaddingTop() + dependency.getHeight();
        child.setPadding(child.getPaddingLeft(), paddingTop, child.getPaddingRight(), child.getPaddingBottom());
        return true;
    }


}
