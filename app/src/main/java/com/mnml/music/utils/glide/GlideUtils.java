package com.mnml.music.utils.glide;

import com.bumptech.glide.request.RequestOptions;

public class GlideUtils {

    public static RequestOptions glideOptions(int size, boolean circle, int alternateDrawableRes) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .override(size, size)
                .placeholder(alternateDrawableRes)
                .error(alternateDrawableRes);
        if(circle) options.circleCrop();
        return options;
    }

}
