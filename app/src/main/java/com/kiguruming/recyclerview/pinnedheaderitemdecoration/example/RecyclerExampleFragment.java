package com.kiguruming.recyclerview.pinnedheaderitemdecoration.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kiguruming.recyclerview.itemdecoration.PinnedHeaderItemDecoration;

/**
 * @author takahr@gmail.com
 */
public class RecyclerExampleFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ExampleRecyclerAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration());

        mAdapter = new ExampleRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_recycler, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_section:
                mAdapter.addSection();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
