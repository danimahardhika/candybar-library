package com.dm.material.dashboard.candybar.fragments;

import android.animation.AnimatorListenerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.TapIntroHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.SearchListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
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

public class IconsBaseFragment extends Fragment {

    private ViewPager mPager;
    private ProgressBar mProgress;
    private TabLayout mTabLayout;

    private AsyncTask<Void, Void, Boolean> mGetIcons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_base, container, false);
        mTabLayout = view.findViewById(R.id.tab);
        mPager = view.findViewById(R.id.pager);
        mProgress = view.findViewById(R.id.progress);
        initTabs();
        mPager.setOffscreenPageLimit(2);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm == null) return false;

                setHasOptionsMenu(false);
                View view = getActivity().findViewById(R.id.shadow);
                if (view != null) view.animate().translationY(-mTabLayout.getHeight())
                        .setDuration(200).start();
                mTabLayout.animate().translationY(-mTabLayout.getHeight()).setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(android.animation.Animator animation) {
                                super.onAnimationEnd(animation);
                                Fragment prev = fm.findFragmentByTag("home");
                                if (prev != null) return;

                                PagerIconsAdapter adapter = (PagerIconsAdapter) mPager.getAdapter();
                                if (adapter == null) return;

                                SearchListener listener = (SearchListener) getActivity();
                                listener.onSearchExpanded(true);

                                FragmentTransaction ft = fm.beginTransaction()
                                        .replace(R.id.container, new IconsSearchFragment(), IconsSearchFragment.TAG)
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        .addToBackStack(null);

                                try {
                                    ft.commit();
                                } catch (Exception e) {
                                    ft.commitAllowingStateLoss();
                                }
                            }
                        }).start();

                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mGetIcons != null) {
            mGetIcons.cancel(true);
        }
        ImageLoader.getInstance().getMemoryCache().clear();
        super.onDestroy();
    }

    private void initTabs() {
        AnimationHelper.slideDownIn(mTabLayout)
                .interpolator(new LinearOutSlowInInterpolator())
                .callback(new AnimationHelper.Callback() {
                    @Override
                    public void onAnimationStart() {

                    }

                    @Override
                    public void onAnimationEnd() {
                        if (getActivity() == null) return;

                        if (Preferences.get(getActivity()).isToolbarShadowEnabled()) {
                            AnimationHelper.fade(getActivity().findViewById(R.id.shadow)).start();
                        }

                        mGetIcons = new IconsLoader().execute();
                    }
                })
                .start();
    }

    private class IconsLoader extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (CandyBarMainActivity.sSections == null) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (CandyBarMainActivity.sSections == null) {
                        CandyBarMainActivity.sSections = IconsHelper.getIconsList(getActivity());

                        for (int i = 0; i < CandyBarMainActivity.sSections.size(); i++) {
                            List<Icon> icons = CandyBarMainActivity.sSections.get(i).getIcons();
                            if (getActivity().getResources().getBoolean(R.bool.show_icon_name)) {
                                for (Icon icon : icons) {
                                    boolean replacer = getActivity().getResources().getBoolean(
                                            R.bool.enable_icon_name_replacer);
                                    String name = IconsHelper.replaceName(getActivity(), replacer, icon.getTitle());
                                    icon.setTitle(name);
                                }
                            }

                            if (getActivity().getResources().getBoolean(R.bool.enable_icons_sort)) {
                                Collections.sort(icons, new AlphanumComparator() {
                                    @Override
                                    public int compare(Object o1, Object o2) {
                                        String s1 = ((Icon) o1).getTitle();
                                        String s2 = ((Icon) o2).getTitle();
                                        return super.compare(s1, s2);
                                    }
                                });

                                CandyBarMainActivity.sSections.get(i).setIcons(icons);
                            }
                        }

                        if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                            List<Icon> icons = IconsHelper.getTabAllIcons();
                            CandyBarMainActivity.sSections.add(new Icon(
                                    CandyBarApplication.getConfiguration().getTabAllIconsTitle(), icons));
                        }
                    }
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
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mGetIcons = null;
            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                setHasOptionsMenu(true);
                PagerIconsAdapter adapter = new PagerIconsAdapter(
                        getChildFragmentManager(), CandyBarMainActivity.sSections);
                mPager.setAdapter(adapter);

                new TabTypefaceChanger().executeOnExecutor(THREAD_POOL_EXECUTOR);

                TapIntroHelper.showIconsIntro(getActivity());
            } else {
                Toast.makeText(getActivity(), R.string.icons_load_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class TabTypefaceChanger extends AsyncTask<Void, Integer, Void> {
        PagerIconsAdapter adapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adapter = (PagerIconsAdapter) mPager.getAdapter();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    for (int i = 0; i < adapter.getCount(); i++) {
                        publishProgress(i);
                    }
                    return null;
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            int position = values[0];
            if (mTabLayout == null) return;

            if (position >= 0 && position < mTabLayout.getTabCount()) {
                TabLayout.Tab tab = mTabLayout.getTabAt(position);
                if (tab != null) {
                    if (position < adapter.getCount()) {
                        tab.setCustomView(R.layout.fragment_icons_base_tab);
                        tab.setText(adapter.getPageTitle(position));
                    }
                }
            }
        }
    }

    private class PagerIconsAdapter extends FragmentStatePagerAdapter {

        private final List<Icon> mIcons;

        PagerIconsAdapter(@NonNull FragmentManager fm, @NonNull List<Icon> icons) {
            super(fm);
            mIcons = icons;
        }

        @Override
        public CharSequence getPageTitle(int position){
            String title = mIcons.get(position).getTitle();
            if (CandyBarApplication.getConfiguration().isShowTabIconsCount()) {
                title += " (" +mIcons.get(position).getIcons().size() +")";
            }
            return title;
        }

        @Override
        public Fragment getItem(int position) {
            return IconsFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mIcons.size();
        }

        public List<Icon> getIcons() {
            return mIcons;
        }
    }
}
