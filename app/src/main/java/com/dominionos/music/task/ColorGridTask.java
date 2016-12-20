package com.dominionos.music.task;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;

import com.dominionos.music.R;
import com.dominionos.music.utils.adapters.AlbumsAdapter;

public class ColorGridTask extends AsyncTask<Object, Object, Void> {

    private final Context context;
    private final String artPath;
    private final AlbumsAdapter.SimpleItemViewHolder holder;

    public ColorGridTask(Context context, String artPath, AlbumsAdapter.SimpleItemViewHolder holder) {
        this.context = context;
        this.artPath = artPath;
        this.holder = holder;
        holder.textHolder.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.card_background, null));
    }

    @Override
    protected Void doInBackground(Object... params) {
        Palette.PaletteAsyncListener paletteAsyncListener = new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (palette.getVibrantSwatch() != null) {
                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                    int colorFrom = ResourcesCompat.getColor(context.getResources(), R.color.card_background, null);
                    int colorTo = vibrantSwatch.getRgb();
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    int duration = 800;
                    colorAnimation.setDuration(duration);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            holder.textHolder.setBackgroundColor((Integer) animator.getAnimatedValue());
                        }

                    });
                    colorAnimation.start();
                    colorFrom = ResourcesCompat.getColor(context.getResources(), android.R.color.primary_text_dark, null);
                    colorTo = vibrantSwatch.getBodyTextColor();
                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    colorAnimation.setDuration(duration);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            holder.albumName.setTextColor((Integer) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();
                    colorFrom = ResourcesCompat.getColor(context.getResources(), android.R.color.secondary_text_dark, null);
                    colorTo = vibrantSwatch.getTitleTextColor();
                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    colorAnimation.setDuration(duration);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            holder.albumDesc.setTextColor((Integer) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();
                }
            }
        };
        Bitmap image = BitmapFactory.decodeFile(artPath);
        if(image != null) {
            Palette.from(image).generate(paletteAsyncListener);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
