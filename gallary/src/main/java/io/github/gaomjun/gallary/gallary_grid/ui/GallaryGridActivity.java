package io.github.gaomjun.gallary.gallary_grid.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.github.gaomjun.gallary.R;
import io.github.gaomjun.gallary.gallary_grid.adapter.GallaryGridRecyclerViewAdapter;
import io.github.gaomjun.gallary.gallary_grid.model.GallaryGridCell;
import io.github.gaomjun.gallary.gallary_grid.model.GallaryGridData;
import io.github.gaomjun.gallary.gallary_grid.model.PaddingItemDecoration;
import io.github.gaomjun.gallary.media_provider.MediaItem;
import io.github.gaomjun.gallary.media_provider.MediaProvider;

/**
 * Created by qq on 12/12/2016.
 */

public class GallaryGridActivity extends Activity {
    private static final int SAPN_COUNT = 5;
    private RecyclerView gallaryGridRecyclerView;
    private GallaryGridRecyclerViewAdapter gallaryGridRecyclerViewAdapter;
    private GallaryGridData gallaryGridData = new GallaryGridData();
    private int gallaryGridCellSpace;
    private MediaProvider mediaProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallary_grid);

        mediaProvider = new MediaProvider(this);

        bindViews();
        setupGallaryGridRecyclerView();
        initDataSource();
    }

    private void bindViews() {
        gallaryGridRecyclerView = (RecyclerView) findViewById(R.id.gallaryGridRecyclerView);

        gallaryGridCellSpace =
                getResources().getDimensionPixelSize(R.dimen.gallaryGridCellSpace);
    }

    private void setupGallaryGridRecyclerView() {
        gallaryGridRecyclerView.setLayoutManager(new GridLayoutManager(this, SAPN_COUNT));

        gallaryGridRecyclerViewAdapter =
                new GallaryGridRecyclerViewAdapter(this, SAPN_COUNT, gallaryGridCellSpace);
        gallaryGridRecyclerViewAdapter.setGallaryGridData(new ArrayList<GallaryGridCell>());

        gallaryGridRecyclerView.setAdapter(gallaryGridRecyclerViewAdapter);
        gallaryGridRecyclerView.addItemDecoration(new PaddingItemDecoration(gallaryGridCellSpace,
                SAPN_COUNT));
    }

    private void initDataSource() {
        mediaProvider.asyncGetMedia(new MediaProvider.ScanStatusCallbak() {
            @Override
            public void scanning(List<MediaItem> media) {
                synchronized (this) {
                    List<GallaryGridCell> data = new ArrayList<>();
                    for (int i = 0; i < media.size(); i++) {
                        GallaryGridCell cell = new GallaryGridCell();

//                        cell.setBitmap(media.get(i).getThumbnail());

                        data.add(cell);
                    }

                    gallaryGridData.setData(data);

                    gallaryGridRecyclerViewAdapter.setGallaryGridData(gallaryGridData.getData());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gallaryGridRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
