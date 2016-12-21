package com.dominionos.music.task;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.widget.LinearLayout;

import com.dominionos.music.R;

public class ColorAnimateAlbumView extends AsyncTask<Void, Void, Void> {

    private final LinearLayout detailHolder;
    private final Integer colorFrom;
    private final Palette palette;
    private ValueAnimator colorAnimation;
    private boolean isVibrantSwatchNull = false;

    public ColorAnimateAlbumView(LinearLayout detailHolder, Palette palette) {
        this.detailHolder = detailHolder;
        this.palette = palette;
        colorFrom = ((ColorDrawable) detailHolder.getBackground()).getColor();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Integer colorTo;
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        if (vibrantSwatch != null) {
            colorTo = vibrantSwatch.getRgb();
            isVibrantSwatchNull = false;
        } else {
            colorTo = R.color.colorPrimary;
            isVibrantSwatchNull = true;
        }
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(1000);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!isVibrantSwatchNull) {
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    detailHolder.setBackgroundColor((Integer) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        }

        super.onPostExecute(aVoid);
    }
}
