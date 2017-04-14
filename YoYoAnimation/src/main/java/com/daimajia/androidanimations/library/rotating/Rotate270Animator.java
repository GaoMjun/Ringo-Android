package com.daimajia.androidanimations.library.rotating;

import android.view.View;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by qq on 13/4/2017.
 */

public class Rotate270Animator extends BaseViewAnimator {
    @Override
    protected void prepare(View target) {
        getAnimatorAgent().play(
                ObjectAnimator.ofFloat(target, "rotation", 0, 270)
        );
    }
}
