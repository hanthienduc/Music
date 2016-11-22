package com.dominionos.music.task;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dominionos.music.R;

public class ColorAnimateAlbumView extends AsyncTask<Void, Void, Void> {

    private Context context;
    private LinearLayout detailHolder;
    private Integer colorFrom, colorFrom1, colorFrom2;
    private Palette palette;
    private ValueAnimator colorAnimation, colorAnimation1, colorAnimation2;

    public ColorAnimateAlbumView(Context musicPlayer, LinearLayout detailHolder, Palette palette) {
        this.context = musicPlayer;
        this.detailHolder = detailHolder;
        this.palette = palette;
        colorFrom = ((ColorDrawable) detailHolder.getBackground()).getColor();
        colorFrom1 = ((TextView) ((Activity) context).findViewById(R.id.player_song_name)).getCurrentTextColor();
        colorFrom2 = ((TextView) ((Activity) context).findViewById(R.id.player_song_artist)).getCurrentTextColor();
    }

    @Override
    protected Void doInBackground(Void... params) {
        int defaultColor = 0x000000;
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        Integer colorTo = vibrantSwatch.getRgb();
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(1000);
        Integer colorTo1 = vibrantSwatch.getTitleTextColor();
        colorAnimation1 = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom1, colorTo1);
        colorAnimation1.setDuration(1000);
        Integer colorTo2 = vibrantSwatch.getBodyTextColor();
        colorAnimation2 = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom2, colorTo2);
        colorAnimation2.setDuration(1000);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                detailHolder.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
        colorAnimation1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ((TextView) ((Activity) context).findViewById(R.id.player_song_name)).setTextColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation1.start();
        colorAnimation2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ((TextView) ((Activity) context).findViewById(R.id.player_song_artist)).setTextColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation2.start();
        super.onPostExecute(aVoid);
    }
}
