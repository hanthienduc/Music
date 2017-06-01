package com.mnml.music.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.Aesthetic;
import com.mnml.music.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

public class RVFragment extends Fragment {

    private Context context;
    private Unbinder unbinder;
    @BindView(R.id.recyclerview) FastScrollRecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        unbinder = ButterKnife.bind(this, v);

        recyclerView();

        return v;
    }

    public RecyclerView.LayoutManager layoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.scrollToPosition(0);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return layoutManager;
    }

    public RecyclerView.Adapter adapter() {
        return null;
    }

    private void recyclerView() {
        final RecyclerView.LayoutManager layoutManager = layoutManager();
        final RecyclerView.Adapter adapter = adapter();
        if(adapter != null && layoutManager != null) {
            Aesthetic.get().colorAccent().take(1).subscribe(accent -> {
                rv.setPopupBgColor(accent);
                rv.setThumbColor(accent);
            });
            rv.setLayoutManager(layoutManager);
            rv.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbinder.unbind();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
