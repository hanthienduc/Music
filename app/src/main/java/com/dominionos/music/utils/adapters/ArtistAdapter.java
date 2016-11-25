package com.dominionos.music.utils.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dominionos.music.R;
import com.dominionos.music.utils.ArtistImgHandler;
import com.dominionos.music.utils.items.ArtistListItem;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.SimpleItemViewHolder> {

    private final List<ArtistListItem> items;
    private Context context;


    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        TextView artistName, artistDesc;
        RoundedImageView artistImg;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            artistName = (TextView) itemView.findViewById(R.id.artist_name);
            artistDesc = (TextView) itemView.findViewById(R.id.artist_desc);
            artistImg = (RoundedImageView) itemView.findViewById(R.id.artist_image);
        }
    }

    public ArtistAdapter(Context context, List<ArtistListItem> items) {
        this.items = items;
        this.context = context;
    }

    @Override
    public ArtistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.artist_list_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        int albumCount = items.get(position).getNumOfAlbums();
        int songCount = items.get(position).getNumOfTracks();
        String artistItemsCount;
        holder.artistName.setText(items.get(position).getName());
        getArtistImg(holder, position);

        if(albumCount == 1 && songCount == 1) {
            artistItemsCount = (albumCount + " Album • " + songCount + " Song");
        } else if (albumCount == 1 && songCount != 1) {
            artistItemsCount = (albumCount + " Album • " + songCount + " Songs");
        } else if (albumCount != 1 && songCount == 1) {
            artistItemsCount = (albumCount + " Albums • " + songCount + " Song");
        } else {
            artistItemsCount = (albumCount + " Albums • " + songCount + " Songs");
        }
        holder.artistDesc.setText(artistItemsCount);
    }

    private void getArtistImg(final SimpleItemViewHolder holder, int position) {
        ArtistImgHandler imgHandler = new ArtistImgHandler(context) {
            @Override
            public void onDownloadComplete(final String url) {
                if (url != null)
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.with(context).load(new File(url)).into(holder.artistImg);
                            setImageToView(url, holder);
                        }
                    });
            }
        };
        String path = imgHandler.getArtistImgFromDB("name");
        if (path != null && !path.matches("")) {
            setImageToView(path, holder);
        } else {
            String urlIfAny = imgHandler.getArtistArtWork(items.get(position).getName(), position);
            if (urlIfAny != null)
                setImageToView(urlIfAny, holder);
        }
    }

    private void setImageToView(String url, final SimpleItemViewHolder holder) {
        Picasso.with(context).load(new File(url)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                holder.artistImg.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}
