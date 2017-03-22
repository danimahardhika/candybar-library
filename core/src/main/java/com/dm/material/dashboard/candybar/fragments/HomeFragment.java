package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.HomeAdapter;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Home;
import com.dm.material.dashboard.candybar.utils.listeners.HomeListener;

import java.util.ArrayList;
import java.util.List;

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

public class HomeFragment extends Fragment implements HomeListener{

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getActivity().getResources().getInteger(R.integer.home_column_count),
                StaggeredGridLayoutManager.VERTICAL));

        initHome();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HomeAdapter adapter = (HomeAdapter) mRecyclerView.getAdapter();
        adapter.setOrientation(newConfig.orientation);
    }

    @Override
    public void onHomeDataUpdated(Home home) {
        if (mRecyclerView == null) return;
        if (mRecyclerView.getAdapter() == null) return;

        if (home != null) {
            HomeAdapter adapter = (HomeAdapter) mRecyclerView.getAdapter();
            adapter.addNewContent(home);
            return;
        }

        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter.getItemCount() > 8) {
            //Probably the original adapter already modified
            adapter.notifyDataSetChanged();
            return;
        }

        int index = adapter.getItemCount() - 3;

        if (WallpaperHelper.getWallpaperType(getActivity()) != WallpaperHelper.CLOUD_WALLPAPERS) {
            index += 1;
        }

        if (index >= 0 && index < adapter.getItemCount()) {
            adapter.notifyItemChanged(index);
        }
    }

    private void initHome() {
        List<Home> homes = new ArrayList<>();

        homes.add(new Home(
                R.drawable.ic_toolbar_apply_launcher,
                String.format(getActivity().getResources().getString(R.string.home_apply_icon_pack),
                        getActivity().getResources().getString(R.string.app_name)),
                "",
                Home.Type.APPLY));

        if (getActivity().getResources().getBoolean(R.bool.enable_donation)) {
            homes.add(new Home(
                    R.drawable.ic_toolbar_donate,
                    getActivity().getResources().getString(R.string.home_donate),
                    getActivity().getResources().getString(R.string.home_donate_desc),
                    Home.Type.DONATE));
        }

        homes.add(new Home(
                -1,
                String.valueOf(CandyBarMainActivity.sIconsCount),
                getActivity().getResources().getString(R.string.home_icons),
                Home.Type.ICONS));

        if (CandyBarMainActivity.sHomeIcon != null) {
            homes.add(CandyBarMainActivity.sHomeIcon);
        }

        mRecyclerView.setAdapter(new HomeAdapter(getActivity(), homes,
                getActivity().getResources().getConfiguration().orientation));
    }

    public void resetWallpapersCount() {
        if (WallpaperHelper.getWallpaperType(getActivity()) == WallpaperHelper.CLOUD_WALLPAPERS) {
            if (mRecyclerView == null) return;
            if (mRecyclerView.getAdapter() == null) return;

            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (adapter.getItemCount() > 8) {
                //Probably the original adapter already modified
                adapter.notifyDataSetChanged();
                return;
            }

            int index = adapter.getItemCount() - 2;
            if (index >= 0 && index < adapter.getItemCount()) {
                adapter.notifyItemChanged(index);
            }
        }
    }
}
