package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.IconsAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.views.AutoFitRecyclerView;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
 *
 * Licensed under the Apache LicenseHelper, Version 2.0 (the "LicenseHelper");
 * you may not use this file except in compliance with the LicenseHelper.
 * You may obtain a copy of the LicenseHelper at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseHelper is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseHelper for the specific language governing permissions and
 * limitations under the LicenseHelper.
 */

public class IconsFragment extends Fragment {

    private AutoFitRecyclerView mIconsGrid;
    private RecyclerFastScroller mFastScroll;
    private TextView mSearchResult;

    private IconsAdapter mAdapter;
    private int mResource;
    private AsyncTask<Void, Void, Boolean> mGetIcons;

    private static final String RES = "res";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons, container, false);
        mIconsGrid = (AutoFitRecyclerView) view.findViewById(R.id.icons_grid);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mSearchResult = (TextView) view.findViewById(R.id.search_result);
        return view;
    }

    public static IconsFragment newInstance(int res) {
        IconsFragment fragment = new IconsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(RES, res);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResource = getArguments().getInt(RES, -1);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mIconsGrid, false);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(), mIconsGrid,
                getActivity().getResources().getConfiguration().orientation);

        mIconsGrid.setHasFixedSize(true);
        mIconsGrid.setItemAnimator(new DefaultItemAnimator());
        mFastScroll.attachRecyclerView(mIconsGrid);

        getIcons();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        search.setIcon(DrawableHelper.getTintedDrawable(getActivity(),
                R.drawable.ic_toolbar_search, color));
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSearchView.setQueryHint(getActivity().getResources().getString(R.string.search_icon));

        ViewHelper.changeSearchViewTextColor(mSearchView,
                ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon),
                ColorHelper.getAttributeColor(getActivity(), R.attr.hint_text));
        View view = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (view != null) view.setBackgroundColor(Color.TRANSPARENT);

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mAdapter.initSearch();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String string) {
                filterSearch(string);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String string) {
                mSearchView.clearFocus();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomMargin(
                getActivity(), mIconsGrid, newConfig.orientation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetIcons != null) mGetIcons.cancel(true);
    }

    @Override
    public void onDetach() {
        if (mAdapter != null) mAdapter.resetSearch();
        setHasOptionsMenu(false);
        super.onDetach();
    }

    private String replaceIconName (String string) {
        char character = Character.toUpperCase(string.charAt(0));
        String finalString = character + string.substring(1);
        return finalString.replace("_", " ");
    }

    private void filterSearch(String query) {
        try {
            mAdapter.search(query);
            if (mAdapter.getItemCount()==0) {
                String text = getActivity().getResources().getString(R.string.search_noresult) + " " +
                        "\"" +query+ "\"";
                mSearchResult.setText(text);
                mSearchResult.setVisibility(View.VISIBLE);
            }
            else mSearchResult.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    private void getIcons() {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

            TypedArray typedArray;
            List<Icon> icons;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                icons = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        typedArray = getActivity().getResources().obtainTypedArray(mResource);

                        for (int i = 0; i < typedArray.length(); i++) {
                            int id = typedArray.getResourceId(i, -1);
                            if (id != -1) {
                                String name = getActivity().getResources().getResourceEntryName(id);
                                name = replaceIconName(name);
                                Icon icon = new Icon(name, id);
                                icons.add(icon);
                            }
                        }

                        try {
                            Collections.sort(icons, (icon, icon1) -> {
                                String name = icon.getTitle();
                                String name1 = icon1.getTitle();
                                return name.compareTo(name1);
                            });
                        } catch (Exception ignored) {}

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
                    setHasOptionsMenu(true);
                    mAdapter = new IconsAdapter(getActivity(), icons);
                    mIconsGrid.setAdapter(mAdapter);
                }
                if (typedArray != null) typedArray.recycle();
                mGetIcons = null;
            }

        }.execute();
    }

}
