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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.SearchListener;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_base, container, false);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
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

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
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
                                listener.OnSearchExpanded(true);

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
        if (mGetIcons != null) mGetIcons.cancel(true);
        super.onDestroy();
    }

    private void initTabs() {
        Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_from_top);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (getActivity() == null) return;

                Animator.startAlphaAnimation(getActivity().findViewById(R.id.shadow), 200, View.VISIBLE);
                getIcons();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTabLayout.startAnimation(slideDown);
        mTabLayout.setVisibility(View.VISIBLE);
    }

    private void getIcons() {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (CandyBarMainActivity.sSections == null)
                    mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (CandyBarMainActivity.sSections == null) {
                            CandyBarMainActivity.sSections = IconsHelper.getIconsList(getActivity());
                            CandyBarMainActivity.sIconsCount = 0;

                            for (int i = 0; i < CandyBarMainActivity.sSections.size(); i++) {
                                List<Icon> icons = CandyBarMainActivity.sSections.get(i).getIcons();
                                CandyBarMainActivity.sIconsCount += icons.size();

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
                    setHasOptionsMenu(true);
                    PagerIconsAdapter adapter = new PagerIconsAdapter(
                            getChildFragmentManager(), CandyBarMainActivity.sSections);
                    mPager.setAdapter(adapter);

                    updateTabTypeface();
                } else {
                    Toast.makeText(getActivity(), R.string.icons_load_failed,
                            Toast.LENGTH_LONG).show();
                }

                mGetIcons = null;
            }
        }.execute();
    }

    private void updateTabTypeface() {
        new AsyncTask<Void, Integer, Void>() {

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
        }.execute();
    }

    private class PagerIconsAdapter extends FragmentStatePagerAdapter {

        private final List<Icon> mIcons;

        PagerIconsAdapter(@NonNull FragmentManager fm, @NonNull List<Icon> icons) {
            super(fm);
            mIcons = icons;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mIcons.get(position).getTitle();
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
