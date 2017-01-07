package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.WallpapersAdapter;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.WallpapersListener;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;
import com.rafakob.drawme.DrawMeButton;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

public class WallpapersFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mWallpapersGrid;
    private SwipeRefreshLayout mSwipe;
    private ProgressBar mProgress;
    private RecyclerFastScroller mFastScroll;

    private HttpURLConnection mConnection;
    private AsyncTask<Void, Void, Boolean> mGetWallpapers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);
        mWallpapersGrid = (RecyclerView) view.findViewById(R.id.wallpapers_grid);
        mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);

        if (Preferences.getPreferences(getActivity()).isShowWallpaperTips()) {
            LinearLayout wallpaperTips = (LinearLayout) view.findViewById(
                    R.id.wallpaper_tips_bar);
            wallpaperTips.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mWallpapersGrid, false);
        resetNavigationBarMargin();

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);
        mSwipe.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.swipeRefresh));

        mWallpapersGrid.setItemAnimator(new DefaultItemAnimator());
        mWallpapersGrid.setHasFixedSize(false);
        mWallpapersGrid.setLayoutManager(new GridLayoutManager(getActivity(), getActivity().getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3));
        mFastScroll.attachRecyclerView(mWallpapersGrid);

        mSwipe.setOnRefreshListener(() -> {
            if (mProgress.getVisibility() == View.GONE)
                getWallpapers(true);
            else mSwipe.setRefreshing(false);
        });

        initWallpaperTips();
        getWallpapers(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetSpanCount(newConfig);
        resetNavigationBarMargin();
    }

    @Override
    public void onDestroy() {
        if (mGetWallpapers != null) {
            try {
                if (mConnection != null) mConnection.disconnect();
            } catch (Exception ignored){}
            mGetWallpapers.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.wallpaper_tips_fab) {
            Animator.hideFab((FloatingActionButton) getActivity()
                    .findViewById(R.id.wallpaper_tips_fab));

            LinearLayout wallpaperTips = (LinearLayout) getActivity()
                    .findViewById(R.id.wallpaper_tips_bar);
            wallpaperTips.setVisibility(View.GONE);
            Preferences.getPreferences(getActivity()).showWallpaperTips(false);
        } else if (id == R.id.popup_bubble) {
            Animator.startAlphaAnimation(getActivity().findViewById(R.id.popup_bubble), 200, View.GONE);
            getWallpapers(true);
        }
    }

    private void resetSpanCount(Configuration configuration) {
        try {
            GridLayoutManager manager = (GridLayoutManager) mWallpapersGrid.getLayoutManager();
            manager.setSpanCount(configuration.orientation ==
                    Configuration.ORIENTATION_PORTRAIT ? 2 : 3);
            manager.requestLayout();
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    private void initWallpaperTips() {
        if (!Preferences.getPreferences(getActivity()).isShowWallpaperTips()) return;

        int toolbarIcon = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        HtmlTextView desc = (HtmlTextView) getActivity().findViewById(R.id.wallpaper_tips_desc);
        desc.setTextColor(ColorHelper.setColorAlpha(toolbarIcon, 0.6f));
        desc.setHtml(getActivity().getResources().getString(R.string.tips_wallpaper));
    }

    private void initWallpaperTipsFab() {
        if (!Preferences.getPreferences(getActivity()).isShowWallpaperTips()) return;

        int accent = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
        int textColor = ColorHelper.getTitleTextColor(getActivity(), accent);
        FloatingActionButton fab = (FloatingActionButton) getActivity()
                .findViewById(R.id.wallpaper_tips_fab);
        fab.setImageDrawable(DrawableHelper.getTintedDrawable(getActivity(),
                R.drawable.ic_fab_check, textColor));
        fab.setOnClickListener(this);
        Animator.showFab(fab);
    }

    private void initPopupBubble() {
        int wallpapersCount = new Database(getActivity()).getWallpapersCount();
        if (wallpapersCount == 0) return;

        if (Preferences.getPreferences(getActivity()).getAvailableWallpapersCount() > wallpapersCount) {
            int color = ContextCompat.getColor(getActivity(), R.color.popupBubbleText);
            DrawMeButton popupBubble = (DrawMeButton) getActivity().findViewById(R.id.popup_bubble);
            popupBubble.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    getActivity(), R.drawable.ic_toolbar_arrow_up, color), null, null, null);
            popupBubble.setOnClickListener(this);
            Animator.startSlideDownAnimation(getActivity(), popupBubble, null);
        }
    }

    private void resetNavigationBarMargin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int paddingTop = getActivity().getResources().getDimensionPixelOffset(R.dimen.card_margin_top);
            int paddingLeft = getActivity().getResources().getDimensionPixelOffset(R.dimen.card_margin_left);
            int paddingBottom = getActivity().getResources().getDimensionPixelOffset(R.dimen.card_margin_bottom);
            if (getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                int navbar = ViewHelper.getNavigationBarHeight(getActivity());
                mWallpapersGrid.setPadding(paddingLeft, paddingTop, 0, (paddingBottom + navbar));
                return;
            }
            mWallpapersGrid.setPadding(paddingLeft, paddingTop, 0, paddingBottom);
        }
    }

    private void getWallpapers(boolean refreshing) {
        final String wallpaperUrl = getActivity().getResources().getString(R.string.wallpaper_json);
        mGetWallpapers = new AsyncTask<Void, Void, Boolean>() {

            WallpaperJSON wallpapersJSON;
            SparseArrayCompat<Wallpaper> wallpapers;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (!refreshing) mProgress.setVisibility(View.VISIBLE);
                else mSwipe.setRefreshing(true);

                DrawMeButton popupBubble = (DrawMeButton) getActivity().findViewById(R.id.popup_bubble);
                if (popupBubble.getVisibility() == View.VISIBLE) popupBubble.setVisibility(View.GONE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Database database = new Database(getActivity());
                        if (!refreshing && (database.getWallpapersCount() > 0)) {
                            wallpapers = database.getWallpapers();
                            return true;
                        }

                        URL url = new URL(wallpaperUrl);
                        mConnection = (HttpURLConnection) url.openConnection();
                        mConnection.setConnectTimeout(15000);

                        if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream stream = mConnection.getInputStream();
                            wallpapersJSON = LoganSquare.parse(stream, WallpaperJSON.class);

                            if (wallpapersJSON == null) return false;
                            if (refreshing) {
                                SparseArrayCompat<Wallpaper> dates = database.getWallpaperAddedOn();

                                database.deleteAllWalls();
                                database.addAllWallpapers(wallpapersJSON);

                                for (int i = 0; i < dates.size(); i++) {
                                    database.setWallpaperAddedOn(
                                            dates.get(i).getURL(),
                                            dates.get(i).getDate());
                                }
                            } else {
                                if (database.getWallpapersCount() > 0) database.deleteAllWalls();
                                database.addAllWallpapers(wallpapersJSON);
                            }

                            wallpapers = database.getWallpapers();
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
                initWallpaperTipsFab();
                if (!refreshing) mProgress.setVisibility(View.GONE);
                else mSwipe.setRefreshing(false);
                if (aBoolean) {
                    mWallpapersGrid.setAdapter(new WallpapersAdapter(getActivity(), wallpapers));

                    WallpapersListener listener = (WallpapersListener) getActivity();
                    listener.OnWallpapersChecked(new Intent().putExtra("size",
                            Preferences.getPreferences(getActivity()).getAvailableWallpapersCount()));
                } else {
                    Toast.makeText(getActivity(), R.string.connection_failed,
                            Toast.LENGTH_LONG).show();
                }
                initPopupBubble();
                mConnection = null;
                mGetWallpapers = null;
            }

        }.execute();
    }

}
