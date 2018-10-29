package com.techbeloved.funmusicplayer.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.techbeloved.funmusicplayer.R;
import com.techbeloved.funmusicplayer.ui.AudioMedia;
import com.techbeloved.funmusicplayer.ui.LetterTileProvider;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MediaListingAdapter extends RecyclerView.Adapter<MediaListingAdapter.ViewHolder> {

    private List<AudioMedia> mMediaList;

    private Context mContext;

    public MediaListingAdapter(Context context) {
        mMediaList = new ArrayList<>();
        mContext = context;
    }

    public void setMediaList(List<AudioMedia> mediaList) {
        if (mediaList != null) {
            mMediaList.clear();
            mMediaList.addAll(mediaList);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioMedia media = getItem(position);
        holder.title.setText(media.title);
        holder.artist.setText(media.artist);
        holder.filePath.setText(media.filePath);
        if (media.albumArt != null) {
            Glide.with(holder.itemView.getContext())
                    .load(media.albumArt)
                    .into(holder.albumArt);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(getAwesomeTileImage(media.title))
                    .into(holder.albumArt);
        }
    }

    @Override
    public int getItemCount() {
        return mMediaList != null ? mMediaList.size() : 0;
    }

    private AudioMedia getItem(int position) {
        return mMediaList.get(position);
    }

    private Bitmap getAwesomeTileImage(String key) {
        final Resources res = mContext.getResources();
        final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        final LetterTileProvider tileProvider = new LetterTileProvider(mContext);

        return tileProvider.getLetterTile(key, key, tileSize, tileSize);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView artist;
        TextView filePath;
        ImageView albumArt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textview_media_item_title);
            artist = itemView.findViewById(R.id.textview_media_item_artist);
            filePath = itemView.findViewById(R.id.textview_file_path);
            albumArt = itemView.findViewById(R.id.imageview_media_item_album_art);
        }
    }
}
