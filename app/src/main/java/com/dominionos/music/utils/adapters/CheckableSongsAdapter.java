package com.dominionos.music.utils.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.dominionos.music.R;
import com.dominionos.music.utils.items.CheckableSongListItem;

import java.util.ArrayList;
import java.util.List;

public class CheckableSongsAdapter extends RecyclerView.Adapter<CheckableSongsAdapter.SimpleItemViewHolder> {
    private List<CheckableSongListItem> items;
    private List<CheckableSongListItem> checkedItems;

    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        public View view;
        CheckBox checkBox;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            checkBox = (CheckBox) itemView.findViewById(R.id.song_checkbox);
        }
    }

    public CheckableSongsAdapter(List<CheckableSongListItem> items) {
        this.items = items;
        checkedItems = new ArrayList<>();
    }

    @Override
    public CheckableSongsAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.song_list_item_checkable, parent, false);
        return new CheckableSongsAdapter.SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CheckableSongsAdapter.SimpleItemViewHolder holder, final int position) {
        holder.title.setText(items.get(position).getName());
        holder.desc.setText(items.get(position).getDesc());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    if(!checkedItems.contains(items.get(position))) {
                        checkedItems.add(items.get(position));
                    }
                } else {
                    checkedItems.remove(items.get(position));
                }
                items.get(position).setSelected(b);
            }
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

    public ArrayList<CheckableSongListItem> getCheckedItems() {
        return (ArrayList<CheckableSongListItem>) checkedItems;
    }
}
