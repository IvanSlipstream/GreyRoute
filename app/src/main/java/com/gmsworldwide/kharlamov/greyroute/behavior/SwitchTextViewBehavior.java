package com.gmsworldwide.kharlamov.greyroute.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Slipstream on 03.01.2017 in GreyRoute.
 */

public class SwitchTextViewBehavior extends CoordinatorLayout.Behavior<TextView> {

    public SwitchTextViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, TextView child, View dependency) {
        return (dependency instanceof Switch) || (dependency instanceof AppBarLayout);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency) {
        int paddingTop = 0;
        int paddingRight = 0;
        int paddingLeft = 0;
        if (dependency instanceof Switch) {
            if (child.getX() > dependency.getX()){
                paddingLeft = dependency.getWidth();
            } else {
                paddingRight = dependency.getWidth();
            }
        }
        if (dependency instanceof AppBarLayout){
            paddingTop = dependency.getHeight();
        }
        child.setPadding(child.getPaddingLeft()+paddingLeft, child.getPaddingTop()+paddingTop,
                child.getPaddingRight()+paddingRight, child.getCompoundPaddingBottom());
        return true;
    }
}
