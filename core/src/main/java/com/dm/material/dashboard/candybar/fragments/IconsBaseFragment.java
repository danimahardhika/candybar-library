package com.dm.material.dashboard.candybar.fragments;

import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.IconsSuggestionAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.SoftKeyboardHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.Tag;

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

public class IconsBaseFragment extends Fragment {

    private ViewPager mPager;
    private ProgressBar mProgress;
    private List<Icon> mIcons;

    private AsyncTask<Void, Void, Boolean> mGetIcons;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_base, container, false);
        TabLayout mTabLayout = (TabLayout) view.findViewById(R.id.tab);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        initViewPager();
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                SoftKeyboardHelper.closeKeyboard(getActivity());
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIcons = new ArrayList<>();
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

        SearchView.SearchAutoComplete autoComplete = (SearchView.SearchAutoComplete)
                searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        if (autoComplete != null) {
            autoComplete.setThreshold(1);
            autoComplete.setAdapter(new IconsSuggestionAdapter(getActivity(),
                    R.layout.fragment_icons_suggestion_item_list, mIcons));
        }

        ViewHelper.changeSearchViewTextColor(searchView,
                ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon),
                ColorHelper.getAttributeColor(getActivity(), R.attr.hint_text));
        View view = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (view != null) view.setBackgroundColor(Color.TRANSPARENT);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetIcons != null) mGetIcons.cancel(true);
    }

    private void initViewPager() {
        boolean globalSearch = getActivity().getResources().getBoolean(
                R.bool.enable_global_icon_search);
        getIcons(globalSearch);
    }

    private void getIcons(boolean globalSearch) {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

            List<Icon> icons;
            List<Icon> sections;
            boolean iconReplacer;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                icons = new ArrayList<>();
                sections = new ArrayList<>();
                iconReplacer = getActivity().getResources().getBoolean(
                        R.bool.enable_icon_name_replacer);
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        XmlResourceParser parser = getActivity().getResources().getXml(R.xml.drawable);
                        int eventType = parser.getEventType();
                        String category = "";
                        int count = 0;

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (parser.getName().equals("category")) {
                                    String title = parser.getAttributeValue(null, "title");
                                    if (!category.equals(title)) {
                                        if (category.length() > 0)
                                            sections.add(new Icon(category, count));
                                        category = title;
                                        count = 0;
                                    }
                                } else if (parser.getName().equals("item")) {
                                    String name = parser.getAttributeValue(null, "drawable");
                                    int id = DrawableHelper.getResourceId(getActivity(), name);
                                    if (id > 0) count += 1;
                                    if (globalSearch) {
                                        if (id > 0) {
                                            name = IconsHelper.replaceIconName(
                                                    getActivity(), iconReplacer, name);
                                            Icon icon = new Icon(category, name, id);
                                            icons.add(icon);
                                        }
                                    }
                                }
                            }

                            eventType = parser.next();
                        }

                        sections.add(new Icon(category, count));
                        parser.close();

                        if (globalSearch) {
                            try {
                                Collections.sort(icons, (icon, icon1) -> {
                                    String name = icon.getTitle();
                                    String name1 = icon1.getTitle();
                                    return name.compareTo(name1);
                                });
                            } catch (Exception ignored) {}
                        }

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
                mProgress.setVisibility(View.GONE);
                if (aBoolean) {
                    mPager.setAdapter(new PagerIconsAdapter(getChildFragmentManager(), sections));

                    if (globalSearch) {
                        mIcons.addAll(icons);
                        setHasOptionsMenu(true);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.icons_load_failed,
                            Toast.LENGTH_LONG).show();
                }

                icons = null;
                mGetIcons = null;
            }

        }.execute();
    }

    private class PagerIconsAdapter extends FragmentStatePagerAdapter {

        private List<Icon> mIcons;

        PagerIconsAdapter(FragmentManager fm, List<Icon> icons) {
            super(fm);
            mIcons = icons;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mIcons.get(position).getTitle() +" ("+ mIcons.get(position).getRes() +")";
        }

        @Override
        public Fragment getItem(int position) {
            return IconsFragment.newInstance(mIcons.get(position).getTitle());
        }

        @Override
        public int getCount() {
            return mIcons.size();
        }

    }

}
