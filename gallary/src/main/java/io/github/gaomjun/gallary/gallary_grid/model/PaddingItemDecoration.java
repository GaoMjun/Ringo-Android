package io.github.gaomjun.gallary.gallary_grid.model;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by qq on 12/12/2016.
 */

public class PaddingItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;
    private final int spanCount;

    public PaddingItemDecoration(int space, int spanCount) {
        this.space = space;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);

        outRect.top = 0;
        outRect.left = 0;
        outRect.right = space;
        outRect.bottom = space;

        if (((position+1) % spanCount == 0)) {
            outRect.right = 0;
        }
    }
}
