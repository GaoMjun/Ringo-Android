package io.github.gaomjun.extensions

import android.os.Handler
import android.widget.Toast

/**
 * Created by qq on 24/3/2017.
 */

fun Handler.postDelayedR(delayMillis: Long, r: (() -> Unit)) {
    postDelayed(r, delayMillis)
}
