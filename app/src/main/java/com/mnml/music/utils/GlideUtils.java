package com.mnml.music.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.request.RequestOptions;

public class GlideUtils {

    public static RequestOptions glideOptions(final int size, final boolean circle, final int alternateDrawableRes) {
        final RequestOptions options = new RequestOptions()
                .centerCrop()
                .override(size, size)
                .placeholder(alternateDrawableRes)
                .error(alternateDrawableRes);
        if(circle) options.circleCrop();
        return options;
    }

    public static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

}
