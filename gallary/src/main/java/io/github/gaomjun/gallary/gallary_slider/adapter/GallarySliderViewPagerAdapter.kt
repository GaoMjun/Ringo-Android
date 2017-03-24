package io.github.gaomjun.gallary.gallary_slider.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.github.gaomjun.gallary.R
import io.github.gaomjun.gallary.gallary_slider.model.GallarySliderItem
import kotlinx.android.synthetic.main.viewpager_image_view.*
import java.io.File

class GallarySliderViewPagerAdapter(val context: Context, val data: List<GallarySliderItem>) :
        PagerAdapter() {

    val layoutInflator: LayoutInflater

    init {
        layoutInflator = LayoutInflater.from(context)
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val page = layoutInflator.inflate(R.layout.viewpager_image_view, container, false)

        val photoView = page.findViewById(R.id.photoView) as SubsamplingScaleImageView
        val playImageView = page.findViewById(R.id.playImageView) as ImageView
        val shareImageView = page.findViewById(R.id.shareImageView) as ImageView
        if (data[position].path.endsWith(".mp4")) {
            shareImageView.visibility = View.GONE
        } else {
            shareImageView.visibility = View.VISIBLE
            shareImageView.setOnClickListener {

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent?.type = "image/*"
                shareIntent?.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(data[position].path)))

                context.startActivity(shareIntent)
            }
        }

        loadImage(photoView, position, playImageView)

        container?.addView(page)

        return page
    }

    private fun loadImage(photoView: SubsamplingScaleImageView, position: Int, playImageView: ImageView) {
        val item = data[position]

        if (item.path.endsWith(".mp4")) {
            val image = File(item.path)
            if (image.exists()) {
                Glide.with(context)
                        .load(image)
                        .asBitmap()
                        .into(object : SimpleTarget<Bitmap>(1024, 1024) {
                            override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                                photoView.setImage(ImageSource.bitmap(resource))
                                if (image.toString().endsWith(".mp4")) {
                                    photoView.isPanEnabled = false
                                    photoView.isZoomEnabled = false
                                    playImageView.visibility = View.VISIBLE
                                    playImageView.setOnClickListener {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.fromFile(image)))
                                    }
                                }
                            }
                        })
            }
        } else {
            photoView.isPanEnabled = true
            photoView.isZoomEnabled = true
            playImageView.visibility = View.GONE
            playImageView.setOnClickListener(null)
            photoView.setImage(ImageSource.uri(item.path))
        }
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as RelativeLayout)
    }
}