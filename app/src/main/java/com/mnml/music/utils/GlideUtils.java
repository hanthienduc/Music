package com.mnml.music.utils;

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

}
