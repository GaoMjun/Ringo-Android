package io.github.gaomjun.gallary.gallary_grid.model;

import android.graphics.Bitmap;

/**
 * Created by qq on 12/12/2016.
 */
public class GallaryGridCell {
    private int imageResId;
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
