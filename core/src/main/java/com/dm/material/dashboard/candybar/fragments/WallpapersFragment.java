package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.WallpapersAdapter;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class WallpapersFragment extends Fragment {

    private RecyclerView mWallpapersGrid;
    private RecyclerFastScroller mFastScroll;
    private SwipeRefreshLayout mSwipe;
    private ProgressBar mProgress;

    private GridLayoutManager mLayoutManager;
    private HttpURLConnection mConnection;
    private Database mDatabase;
    private WallpapersAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mGetCloudWallpapers;
    private AsyncTask<Void, Void, Boolean> mGetWallpapers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);
        mWallpapersGrid = (RecyclerView) view.findViewById(R.id.wallpapers_grid);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mWallpapersGrid, false);
        resetNavigationBarMargin();

        mDatabase = new Database(getActivity());

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);
        mSwipe.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.swipeRefresh));

        mLayoutManager = new GridLayoutManager(getActivity(), getActivity().getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3);
        mWallpapersGrid.setItemAnimator(new DefaultItemAnimator());
        mWallpapersGrid.setHasFixedSize(false);
        mWallpapersGrid.setLayoutManager(mLayoutManager);
        mFastScroll.attachRecyclerView(mWallpapersGrid);

        mSwipe.setOnRefreshListener(() -> {
            if (mProgress.getVisibility() == View.GONE)
                getCloudWallpapers(true);
            else mSwipe.setRefreshing(false);
        });

        if (mDatabase.isWallpapersEmpty()) getCloudWallpapers(false);
        else {
            if (Preferences.getPreferences(getActivity()).isTimeToUpdateWallpaper()) {
                getCloudWallpapers(true);
                return;
            }

            getWallpapers();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutManager.setSpanCount(newConfig.orientation ==
                Configuration.ORIENTATION_PORTRAIT ? 2 : 3);
        resetSpanSizeLookUp(newConfig.orientation);
        mLayoutManager.requestLayout();
        resetNavigationBarMargin();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetCloudWallpapers != null) {
            try {
                if (mConnection != null) mConnection.disconnect();
            } catch (Exception ignored){}
            mGetCloudWallpapers.cancel(true);
        }
        if (mGetWallpapers != null) mGetWallpapers.cancel(true);
    }

    private void resetNavigationBarMargin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int left = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_left);
            int top = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_top);
            int bottom = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
            if (getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                int navbar = ViewHelper.getNavigationBarHeight(getActivity());
                mWallpapersGrid.setPadding(left, top, 0, (bottom + navbar));
            } else mWallpapersGrid.setPadding(left, top, 0, bottom);
        }
    }

    private void resetSpanSizeLookUp(int orientation) {
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                boolean portrait = orientation == Configuration.ORIENTATION_PORTRAIT;
                boolean showTips = Preferences.getPreferences(getActivity()).isShowWallpaperTips();
                if (portrait) return position == 0 && showTips ? 2 : 1;
                else return position == 0 && showTips ? 3 : 1;
            }
        });
    }

    private void getCloudWallpapers(boolean refreshing) {
        final String wallpaperUrl = getActivity().getResources().getString(R.string.wallpaper_json);
        mGetCloudWallpapers = new AsyncTask<Void, Void, Boolean>() {

            WallpaperJSON wallpapers;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (!refreshing) mProgress.setVisibility(View.VISIBLE);
                else mSwipe.setRefreshing(true);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        URL url = new URL(wallpaperUrl);
                        mConnection = (HttpURLConnection) url.openConnection();
                        mConnection.setConnectTimeout(15000);

                        if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream stream = new BufferedInputStream(mConnection.getInputStream());
                            wallpapers = LoganSquare.parse(stream, WallpaperJSON.class);

                            if (wallpapers == null) return false;
                            if (refreshing) {
                                List<Wallpaper> dates = mDatabase.getWallpaperAddedOn();

                                mDatabase.deleteAllWalls();
                                mDatabase.addAllWallpapers(wallpapers);

                                for (Wallpaper date : dates) {
                                    mDatabase.setWallpaperAddedOn(date.getURL(), date.getDate());
                                }
                                return true;
                            }

                            if (!mDatabase.isWallpapersEmpty()) mDatabase.deleteAllWalls();
                            mDatabase.addAllWallpapers(wallpapers);
                            return true;
                        }
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (!refreshing) mProgress.setVisibility(View.GONE);
                else mSwipe.setRefreshing(false);
                if (aBoolean) {
                    Preferences.getPreferences(getActivity()).setWallpaperLastUpdate();
                    getWallpapers();
                } else {
                    Toast.makeText(getActivity(), R.string.connection_failed,
                            Toast.LENGTH_LONG).show();
                }
                mConnection = null;
                mGetCloudWallpapers = null;
            }

        }.execute();
    }

    private void getWallpapers() {
        mGetWallpapers = new AsyncTask<Void, Void, Boolean>() {

            List<Wallpaper> wallpapers;

            @Override
            protected Boolean doInBackground(Void... voids) {
                while(!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        wallpapers = mDatabase.getWallpapers();
                        return true;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    resetSpanSizeLookUp(getActivity().getResources().getConfiguration().orientation);
                    mAdapter = new WallpapersAdapter(getActivity(), wallpapers);
                    mWallpapersGrid.setAdapter(mAdapter);
                }
            }
        }.execute();
    }

}
