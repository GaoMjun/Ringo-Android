package com.daimajia.androidanimations.library.translation;

import android.view.View;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by qq on 13/4/2017.
 */

public class TranslationXAnimation extends BaseViewAnimator {
    private float from;
    private float to;

    public TranslationXAnimation(float from, float to) {
        this.from = from;
        this.to = to;
    }

    @Override
    protected void prepare(View target) {
        getAnimatorAgent().play(
                ObjectAnimator.ofFloat(target, "translationX", from, to)
        );
    }
}
