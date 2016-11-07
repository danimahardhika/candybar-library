package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.FAQsAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.FAQs;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
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

public class FAQsFragment extends Fragment {

    private RecyclerView mFAQsList;
    private RecyclerFastScroller mFastScroll;
    private TextView mSearchResult;

    private FAQsAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mGetFAQs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faqs, container, false);
        mFAQsList = (RecyclerView) view.findViewById(R.id.faqs_list);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mSearchResult = (TextView) view.findViewById(R.id.search_result);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mFAQsList, false);
        resetNavigationBarMargin();

        mFAQsList.setItemAnimator(new DefaultItemAnimator());
        mFAQsList.setHasFixedSize(false);
        mFAQsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFastScroll.attachRecyclerView(mFAQsList);

        getFAQs();
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
        mSearchView.setQueryHint(getActivity().getResources().getString(R.string.search_faqs));

        ViewHelper.changeSearchViewTextColor(mSearchView,
                ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon),
                ColorHelper.getAttributeColor(getActivity(), R.attr.hint_text));
        View view = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (view != null) view.setBackgroundColor(Color.TRANSPARENT);

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
        resetNavigationBarMargin();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetFAQs != null) mGetFAQs.cancel(true);
    }

    private void resetNavigationBarMargin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);
            if (getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                int navbar = ViewHelper.getNavigationBarHeight(getActivity());
                mFAQsList.setPadding(padding, padding, padding, (padding + navbar));
            } else mFAQsList.setPadding(padding, padding, padding, padding);
        }
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

    private void getFAQs() {
        mGetFAQs = new AsyncTask<Void, Void, Boolean>() {

            List<FAQs> faqs;
            String[] questions;
            String[] answers;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                faqs = new ArrayList<>();
                questions = getActivity().getResources().getStringArray(R.array.questions);
                answers = getActivity().getResources().getStringArray(R.array.answers);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        for (int i = 0; i < questions.length; i++) {
                            if (i < answers.length) {
                                FAQs faq = new FAQs(questions[i], answers[i]);
                                faqs.add(faq);
                            }
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
                if (aBoolean) {
                    setHasOptionsMenu(true);
                    mAdapter = new FAQsAdapter(getActivity(), faqs);
                    mFAQsList.setAdapter(mAdapter);
                }
                mGetFAQs = null;
            }

        }.execute();
    }

}
