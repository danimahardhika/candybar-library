package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
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
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.FAQs;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

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

public class FAQsFragment extends Fragment {

    private RecyclerView mFAQsList;
    private TextView mSearchResult;
    private RecyclerFastScroller mFastScroll;

    private FAQsAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mGetFAQs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faqs, container, false);
        mFAQsList = (RecyclerView) view.findViewById(R.id.faqs_list);
        mSearchResult = (TextView) view.findViewById(R.id.search_result);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mFAQsList, false);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mFAQsList,
                getActivity().getResources().getConfiguration().orientation);

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

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setQueryHint(getActivity().getResources().getString(R.string.search_faqs));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        ViewHelper.changeSearchViewTextColor(searchView, color,
                ColorHelper.setColorAlpha(color, 0.6f));
        View view = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (view != null) view.setBackgroundColor(Color.TRANSPARENT);

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
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mFAQsList, newConfig.orientation);
    }

    @Override
    public void onDestroy() {
        if (mGetFAQs != null) mGetFAQs.cancel(true);
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
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    private void getFAQs() {
        mGetFAQs = new AsyncTask<Void, Void, Boolean>() {

            SparseArrayCompat<FAQs> faqs;
            String[] questions;
            String[] answers;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                faqs = new SparseArrayCompat<>();
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
                                faqs.append(faqs.size(), faq);
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
                    mAdapter = new FAQsAdapter(faqs);
                    mFAQsList.setAdapter(mAdapter);
                }
                mGetFAQs = null;
            }

        }.execute();
    }
}
