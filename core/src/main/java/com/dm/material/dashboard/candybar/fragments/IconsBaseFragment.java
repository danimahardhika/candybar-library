package com.dm.material.dashboard.candybar.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.SoftKeyboardHelper;
import com.dm.material.dashboard.candybar.items.Icon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_base, container, false);
        TabLayout mTabLayout = (TabLayout) view.findViewById(R.id.tab);
        mPager = (ViewPager) view.findViewById(R.id.pager);
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

    private void initViewPager() {
        List<Icon> icons = new ArrayList<>();
        String[] titles = getActivity().getResources().getStringArray(R.array.icon_sections);
        for (String title : titles) {
            String arrayName = title.toLowerCase(Locale.getDefault()).replace(" ", "_");
            int res = getActivity().getResources().getIdentifier(
                    arrayName, "array", getActivity().getPackageName());
            if (res > 0) {
                TypedArray typedArray = getActivity().getResources().obtainTypedArray(res);
                Icon icon = new Icon(title, res, typedArray.length());
                icons.add(icon);
                typedArray.recycle();
            }
        }
        mPager.setAdapter(new PagerIconsAdapter(getChildFragmentManager(), icons));
    }

    private class PagerIconsAdapter extends FragmentStatePagerAdapter {

        private List<Icon> mIcons;

        PagerIconsAdapter(FragmentManager fm, List<Icon> icons) {
            super(fm);
            mIcons = icons;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mIcons.get(position).getTitle() +" ("+ mIcons.get(position).getCount() +")";
        }

        @Override
        public Fragment getItem(int position) {
            return IconsFragment.newInstance(mIcons.get(position).getRes());
        }

        @Override
        public int getCount() {
            return mIcons.size();
        }

    }

}
