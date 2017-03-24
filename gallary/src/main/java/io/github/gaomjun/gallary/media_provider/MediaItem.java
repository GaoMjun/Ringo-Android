package io.github.gaomjun.gallary.media_provider;

import android.graphics.Bitmap;

/**
 * Created by qq on 12/12/2016.
 */

public class MediaItem {
    private Bitmap thumbnail;
    private String path;
    private boolean isVideo;

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }
}
