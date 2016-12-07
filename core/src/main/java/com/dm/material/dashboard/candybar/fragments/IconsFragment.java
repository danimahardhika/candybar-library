package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.IconsAdapter;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.SparseArrayUtils;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.views.AutoFitRecyclerView;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.xmlpull.v1.XmlPullParser;

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

    private AutoFitRecyclerView mIconsGrid;
    private RecyclerFastScroller mFastScroll;

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomMargin(
                getActivity(), mIconsGrid, newConfig.orientation);
    }

    @Override
    public void onDestroy() {
        if (mGetIcons != null) mGetIcons.cancel(true);
        super.onDestroy();
    }

    private void getIcons() {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

            SparseArrayCompat<Icon> icons;
            boolean iconReplacer;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                icons = new SparseArrayCompat<>();
                iconReplacer = getActivity().getResources().getBoolean(
                        R.bool.enable_icon_name_replacer);
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
                                            name = IconsHelper.replaceIconName(
                                                    getActivity(), iconReplacer, name);
                                            Icon icon = new Icon(name, id);
                                            icons.append(icons.size(), icon);
                                        }
                                        filled = true;
                                    } else {
                                        if (filled) break;
                                    }
                                }
                            }

                            eventType = parser.next();
                        }

                        parser.close();

                        try {
                            SparseArrayUtils utils = new SparseArrayUtils();
                            utils.sort(icons);
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
                    IconsAdapter adapter = new IconsAdapter(getActivity(), icons);
                    mIconsGrid.setAdapter(adapter);
                }
                mGetIcons = null;
            }

        }.execute();
    }

}
