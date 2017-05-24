package com.mnml.music.utils;

import android.content.Context;
import android.text.InputType;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mnml.music.R;
import com.mnml.music.adapters.PlaylistAdapter;
import com.mnml.music.models.Playlist;

import java.util.ArrayList;

public class PlaylistUtils {

    public static void createPlaylist(final Context context, final PlaylistAdapter adapter) {
        new MaterialDialog.Builder(context)
                .title(R.string.add_playlist)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(
                        context.getString(R.string.playlist_example),
                        null,
                        (dialog, input) -> {
                            final String inputString = input.toString();
                            if (!inputString.isEmpty()) {
                                PlaylistHelper helper = new PlaylistHelper(context);
                                helper.createNewPlayList(inputString);
                                final ArrayList<Playlist> list = new ArrayList<>();
                                list.addAll(helper.getAllPlaylist());
                                adapter.updateData(list);
                            } else {
                                Toast.makeText(context, R.string.playlist_name_empty_warning, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .positiveText(context.getString(R.string.ok))
                .negativeText(context.getString(R.string.cancel))
                .show();
    }
}
