package io.github.gaomjun.ringo;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

/**
 * Created by qq on 23/11/2016.
 */

public class ViewUtils {
    private static View view;

    public ViewUtils(View v) {
        view = v;
    }

    public void setX(int x, float duration) {
        if (duration <= 0) {
            view.setTranslationX(x);
        } else {
            TranslateAnimation translateAnimation = new TranslateAnimation(view.getTranslationX(),
                    x, view.getTranslationY(), view.getTranslationY());
            translateAnimation.setDuration((long) duration);
            view.startAnimation(translateAnimation);
        }
    }

    public void setY(int y, float duration) {
        if (duration <= 0) {
            view.setTranslationY(y);
        } else {
            TranslateAnimation translateAnimation = new TranslateAnimation(view.getTranslationX(),
                    view.getTranslationX(), view.getTranslationY(), y);
            translateAnimation.setDuration((long) duration);
            view.startAnimation(translateAnimation);
        }
    }

    public void setWidth(int width, float duration) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (duration <= 0) {
            layoutParams.width = width;
            view.setLayoutParams(layoutParams);
        } else {
            ResizeAnimation resizeAnimation = new ResizeAnimation(view);
            resizeAnimation.setParams(layoutParams.width, width);
            resizeAnimation.setDuration((long) duration);
            view.startAnimation(resizeAnimation);
        }
    }

    public void setHeight(int height, float duration) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (duration <= 0) {
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        } else {
            ResizeAnimation resizeAnimation = new ResizeAnimation(view);
            resizeAnimation.setParams(layoutParams.height, height);
            resizeAnimation.setDuration((long) duration);
            view.startAnimation(resizeAnimation);
        }
    }

    public void setRect(int x, int y, int width, int height, float duration) {
        if (duration <= 0) {
            view.setTranslationX(x);
            view.setTranslationY(y);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        } else {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

            view.setTranslationX(x);
            view.setTranslationY(y);
//            TranslateAnimation translateAnimationX = new TranslateAnimation(view.getTranslationX(),
//                    x, view.getTranslationY(), view.getTranslationY());
//            translateAnimationX.setDuration((long) duration);
//
//            TranslateAnimation translateAnimationY = new TranslateAnimation(view.getTranslationX(),
//                    view.getTranslationX(), view.getTranslationY(), y);
//            translateAnimationY.setDuration((long) duration);

            ResizeAnimation resizeAnimationWidth = new ResizeAnimation(view);
            resizeAnimationWidth.setParams(layoutParams.width, width);
            resizeAnimationWidth.setDuration((long) duration);
            view.startAnimation(resizeAnimationWidth);

            ResizeAnimation resizeAnimationHeight = new ResizeAnimation(view);
            resizeAnimationHeight.setParams(layoutParams.height, height);
            resizeAnimationHeight.setDuration((long) duration);
            view.startAnimation(resizeAnimationHeight);

            AnimationSet animationSet = new AnimationSet(true);
//            animationSet.addAnimation(translateAnimationX);
//            animationSet.addAnimation(translateAnimationY);
            animationSet.addAnimation(resizeAnimationWidth);
            animationSet.addAnimation(resizeAnimationHeight);

            view.startAnimation(animationSet);
        }
    }
}
