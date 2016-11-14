package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
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
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.views.AutoFitRecyclerView;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
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

public class IconsFragment extends Fragment {

    private AutoFitRecyclerView mIconsGrid;
    private RecyclerFastScroller mFastScroll;
    private TextView mSearchResult;

    private IconsAdapter mAdapter;
    private String mTitle;
    private AsyncTask<Void, Void, Boolean> mGetIcons;

    private static final String TITLE = "title";

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

    public static IconsFragment newInstance(String title) {
        IconsFragment fragment = new IconsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getArguments().getString(TITLE);
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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        searchView.setQueryHint(getActivity().getResources().getString(R.string.search_icon));

        ViewHelper.changeSearchViewTextColor(searchView,
                ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon),
                ColorHelper.getAttributeColor(getActivity(), R.attr.hint_text));
        View view = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String string) {
                filterSearch(string);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String string) {
                searchView.clearFocus();
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
                        XmlResourceParser parser = getActivity().getResources().getXml(R.xml.drawable);
                        int eventType = parser.getEventType();
                        String title = "";
                        boolean filled = false;

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (parser.getName().equals("category")) {
                                    title = parser.getAttributeValue(null, "title");
                                } else if (parser.getName().equals("item")) {
                                    if (title.equals(mTitle)) {
                                        String name = parser.getAttributeValue(null, "drawable");
                                        int id = DrawableHelper.getResourceId(getActivity(), name);
                                        if (id > 0) {
                                            name = IconsHelper.replaceIconName(name);
                                            Icon icon = new Icon(name, id);
                                            icons.add(icon);
                                        }
                                        filled = true;
                                    } else {
                                        if (filled) break;
                                    }
                                }
                            }

                            eventType = parser.next();
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
                    boolean globalSearch = getActivity().getResources().getBoolean(
                            R.bool.enable_global_icon_search);
                    setHasOptionsMenu(!globalSearch);

                    mAdapter = new IconsAdapter(getActivity(), icons);
                    mIconsGrid.setAdapter(mAdapter);
                }
                if (typedArray != null) typedArray.recycle();
                mGetIcons = null;
            }

        }.execute();
    }

}
