package io.github.gaomjun.gallary.gallary_grid.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.gaomjun.gallary.R;
import io.github.gaomjun.gallary.gallary_grid.model.GallaryGridCell;
import io.github.gaomjun.gallary.gallary_slider.ui.GallarySliderActivity;
import io.github.gaomjun.gallary.media_provider.MediaProvider;

/**
 * Created by qq on 12/12/2016.
 */
public class GallaryGridRecyclerViewAdapter extends android.support.v7.widget.RecyclerView.Adapter
        <GallaryGridRecyclerViewAdapter.GallaryGridCellHolder> {

    private final Context context;
    private List<GallaryGridCell> gallaryGridData;
    private final int spanCount;
    private LayoutInflater layoutInflater;
    private int cellSpace;
    private int itemWidth;
    private int itemHeight;

    public GallaryGridRecyclerViewAdapter(Context context,
                                          int spanCount, int cellSpace) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

        this.spanCount = spanCount;
        this.cellSpace = cellSpace;
    }

    public void setGallaryGridData(List<GallaryGridCell> gallaryGridData) {
        this.gallaryGridData = gallaryGridData;
    }

    @Override
    public GallaryGridCellHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View gallaryGridCell = layoutInflater.inflate(R.layout.gallary_grid_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = gallaryGridCell.getLayoutParams();
        int width = (parent.getWidth()-cellSpace*(spanCount-1)) / spanCount;
        layoutParams.height = width;
        itemWidth = itemWidth != width ? width : itemWidth;
        itemHeight = itemWidth;
//        layoutParams.width = width;
        gallaryGridCell.setLayoutParams(layoutParams);
        return new GallaryGridCellHolder(gallaryGridCell);
    }

    @Override
    public void onBindViewHolder(GallaryGridCellHolder holder, int position) {
//        GallaryGridCell gallaryGridCell = gallaryGridData.get(position);
//
//        holder.imageView.setImageBitmap(gallaryGridCell.getBitmap());
        final String path = MediaProvider.Instance.getMediaPaths()[position];
        final File image = new File(path);
        if (image.exists()) {
            Glide.with(context)
                    .load(image)
                    .thumbnail(.2f)
                    .override(itemWidth, itemHeight)
                    .into(holder.imageView);
        }
        if (path.endsWith(".mp4")) {
            holder.playVideoImageView.setVisibility(View.VISIBLE);
        } else {
            holder.playVideoImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return gallaryGridData.size();
    }

    class GallaryGridCellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View gallaryCell;
        private ImageView imageView;
        private ImageView playVideoImageView;

        public GallaryGridCellHolder(View itemView) {
            super(itemView);

            gallaryCell = itemView.findViewById(R.id.gallaryGridCell);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            playVideoImageView = (ImageView) itemView.findViewById(R.id.playVideoImageView);

            gallaryCell.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if (viewId == R.id.gallaryGridCell) {
                Log.d("onClick", getAdapterPosition()+"");

                Intent intent = new Intent(context, GallarySliderActivity.class);
                intent.putExtra("position", getAdapterPosition());
                intent.putExtra("pathArray", MediaProvider.Instance.getMediaPaths());
                intent.putExtra("typeArray", MediaProvider.Instance.getMediaTypes());
                context.startActivity(intent);
            }
        }
    }
}
