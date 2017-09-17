package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.IconsAdapter;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Collections;
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

public class IconsSearchFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerFastScroller mFastScroll;
    private TextView mSearchResult;
    private SearchView mSearchView;

    private IconsAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mGetIcons;

    public static final String TAG = "icons_search";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_search, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.icons_grid);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mSearchResult = (TextView) view.findViewById(R.id.search_result);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.icons_column_count)));
        mFastScroll.attachRecyclerView(mRecyclerView);
        setFastScrollColor(mFastScroll);

        getIcons();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_icons_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setQueryHint(getActivity().getResources().getString(R.string.search_icon));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        MenuItemCompat.expandActionView(search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.clearFocus();

        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        ViewHelper.setSearchViewTextColor(mSearchView, color);
        ViewHelper.setSearchViewBackgroundColor(mSearchView, Color.TRANSPARENT);
        ViewHelper.setSearchViewCloseIcon(mSearchView, R.drawable.ic_toolbar_close);
        ViewHelper.setSearchViewSearchIcon(mSearchView, null);

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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, getActivity().getResources().getInteger(R.integer.icons_column_count));
    }

    @Override
    public void onDestroy() {
        if (mGetIcons != null) mGetIcons.cancel(true);
        super.onDestroy();
    }

    private void filterSearch(String query) {
        try {
            mAdapter.search(query);
            if (mAdapter.getItemCount()==0) {
                String text = String.format(getActivity().getResources().getString(
                        R.string.search_noresult), query);
                mSearchResult.setText(text);
                mSearchResult.setVisibility(View.VISIBLE);
            }
            else mSearchResult.setVisibility(View.GONE);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    private void getIcons() {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

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
                        if (CandyBarMainActivity.sSections == null) {
                            CandyBarMainActivity.sSections = IconsHelper.getIconsList(getActivity());

                            for (Icon section : CandyBarMainActivity.sSections) {
                                if (getActivity().getResources().getBoolean(R.bool.show_icon_name)) {
                                    for (Icon icon : section.getIcons()) {
                                        String name = IconsHelper.replaceName(getActivity(),
                                                getActivity().getResources().getBoolean(R.bool.enable_icon_name_replacer),
                                                icon.getTitle());
                                        icon.setTitle(name);
                                    }
                                }
                            }

                            if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                                List<Icon> icons = IconsHelper.getTabAllIcons();
                                CandyBarMainActivity.sSections.add(new Icon(
                                        CandyBarApplication.getConfiguration().getTabAllIconsTitle(), icons));
                            }
                        }

                        for (Icon icon : CandyBarMainActivity.sSections) {
                            if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                                if (!icon.getTitle().equals(CandyBarApplication.getConfiguration().getTabAllIconsTitle())) {
                                    icons.addAll(icon.getIcons());
                                }
                            } else {
                                icons.addAll(icon.getIcons());
                            }
                        }

                        Collections.sort(icons, new AlphanumComparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                String s1 = ((Icon) o1).getTitle();
                                String s2 = ((Icon) o2).getTitle();
                                return super.compare(s1, s2);
                            }
                        });
                        return true;
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                mGetIcons = null;

                if (getActivity() == null) return;
                if (getActivity().isFinishing()) return;

                if (aBoolean) {
                    mAdapter = new IconsAdapter(getActivity(), icons, true);
                    mRecyclerView.setAdapter(mAdapter);
                    mSearchView.requestFocus();
                    SoftKeyboardHelper.openKeyboard(getActivity());
                } else {
                    //Unable to load all icons
                    Toast.makeText(getActivity(), R.string.icons_load_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}
