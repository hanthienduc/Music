package com.dominionos.music.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.dominionos.music.R;
import com.dominionos.music.items.CheckableSong;

import java.util.ArrayList;
import java.util.List;

public class CheckableSongsAdapter
        extends RecyclerView.Adapter<CheckableSongsAdapter.SimpleItemViewHolder> {
    private final List<CheckableSong> items;
    private final List<CheckableSong> checkedItems;

    public CheckableSongsAdapter(List<CheckableSong> items) {
        this.items = items;
        checkedItems = new ArrayList<>();
    }

    @Override
    public CheckableSongsAdapter.SimpleItemViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.song_list_item_checkable, parent, false);
        return new CheckableSongsAdapter.SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            final CheckableSongsAdapter.SimpleItemViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        holder.title.setText(items.get(position).getName());
        holder.desc.setText(items.get(position).getDesc());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.view.setOnClickListener(v -> holder.checkBox.setChecked(!holder.checkBox.isChecked()));
        final int finalPosition = position;
        holder.checkBox.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    if (b) {
                        if (!checkedItems.contains(items.get(finalPosition))) {
                            checkedItems.add(items.get(finalPosition));
                        }
                    } else {
                        checkedItems.remove(items.get(finalPosition));
                    }
                    items.get(finalPosition).setSelected(b);
                });
        holder.checkBox.setChecked(items.get(position).isSelected);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    public ArrayList<CheckableSong> getCheckedItems() {
        return (ArrayList<CheckableSong>) checkedItems;
    }

    static final class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        final TextView title;
        final TextView desc;
        final CheckBox checkBox;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            checkBox = (CheckBox) itemView.findViewById(R.id.song_checkbox);
        }
    }
}
