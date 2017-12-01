package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.IconsAdapter;
import com.dm.material.dashboard.candybar.items.Icon;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.List;

import static com.dm.material.dashboard.candybar.helpers.ViewHelper.setFastScrollColor;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class IconsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerFastScroller mFastScroll;

    private List<Icon> mIcons;

    private static final String INDEX = "index";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons, container, false);
        mRecyclerView = view.findViewById(R.id.icons_grid);
        mFastScroll = view.findViewById(R.id.fastscroll);
        return view;
    }

    public static IconsFragment newInstance(int index) {
        IconsFragment fragment = new IconsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIcons = new ArrayList<>();
        int index = getArguments().getInt(INDEX);
        if (CandyBarMainActivity.sSections != null)
            mIcons = CandyBarMainActivity.sSections.get(index).getIcons();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.icons_column_count)));
        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        IconsAdapter adapter = new IconsAdapter(getActivity(), mIcons, false);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView,
                getActivity().getResources().getInteger(R.integer.icons_column_count));
    }
}
