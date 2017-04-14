package com.daimajia.androidanimations.library.rotating;

import android.view.View;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by qq on 13/4/2017.
 */

public class RotateAnimator extends BaseViewAnimator {

    private float from;
    private float to;

    public RotateAnimator(float from, float to) {
        this.from = from;
        this.to = to;
    }

    @Override
    protected void prepare(View target) {
        getAnimatorAgent().play(
                ObjectAnimator.ofFloat(target, "rotation", from, to)
        );
    }
}
