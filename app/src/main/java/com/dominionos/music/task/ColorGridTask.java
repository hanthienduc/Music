package com.dominionos.music.task;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.dominionos.music.R;
import com.dominionos.music.utils.adapters.AlbumsAdapter;

public class ColorGridTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final String artPath;
    private final AlbumsAdapter.SimpleItemViewHolder holder;
    private ValueAnimator colorAnimation;

    public ColorGridTask(Context context, String artPath, AlbumsAdapter.SimpleItemViewHolder holder) {
        this.context = context;
        this.artPath = artPath;
        this.holder = holder;
        holder.textHolder.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.card_background, null));
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.v("artPath", artPath);
        Palette.generateAsync(BitmapFactory.decodeFile(artPath),
                new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(final Palette palette) {
                        Integer colorFrom = ResourcesCompat.getColor(context.getResources(), R.color.card_background, null);
                        Integer colorTo = palette.getVibrantColor(ResourcesCompat.getColor(context.getResources(), R.color.card_background, null));
                        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(1000);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                holder.textHolder.setBackgroundColor((Integer) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();
                        try {
                            Integer colorFrom1 = Color.parseColor("#ffffff");
                            Integer colorTo1 = palette.getVibrantSwatch().getTitleTextColor();
                            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom1, colorTo1);
                            colorAnimation.setDuration(800);
                            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                    holder.albumName.setTextColor((Integer) animator.getAnimatedValue());
                                }
                            });
                            colorAnimation.start();
                            Integer colorFrom2 = Color.parseColor("#adb2bb");
                            Integer colorTo2 = palette.getVibrantSwatch().getTitleTextColor();
                            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom2, colorTo2);
                            colorAnimation.setDuration(800);
                            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                    holder.albumDesc.setTextColor((Integer) animator.getAnimatedValue());
                                }
                            });
                            colorAnimation.start();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
