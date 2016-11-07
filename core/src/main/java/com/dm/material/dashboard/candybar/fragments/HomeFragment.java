package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.HomeFeaturesAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Feature;
import com.dm.material.dashboard.candybar.preferences.Preferences;

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

public class HomeFragment extends Fragment {

    private RecyclerView mFeatureList;
    private CardView mCardDesc;
    private CardView mCardApps;
    private LinearLayout mMoreApps;
    private ImageView mAppsIcon;
    private NestedScrollView mScrollView;
    private View mShadow;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mFeatureList = (RecyclerView) view.findViewById(R.id.home_feature_list);
        mCardDesc = (CardView) view.findViewById(R.id.card_desc);
        mCardApps = (CardView) view.findViewById(R.id.card_more_apps);
        mMoreApps = (LinearLayout) view.findViewById(R.id.more_apps);
        mAppsIcon = (ImageView) view.findViewById(R.id.more_apps_icon);
        mScrollView = (NestedScrollView) view.findViewById(R.id.scrollview);
        mShadow = view.findViewById(R.id.shadow);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);

        initDescription();
        initFeatures();
        initMoreApps();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(),
                mScrollView, newConfig.orientation);
    }

    public void showToolbarShadow(boolean isTimeToShow) {
        mShadow.setVisibility(isTimeToShow ? View.VISIBLE : View.GONE);
    }

    private void initDescription() {
        String desc = getActivity().getResources().getString(R.string.home_description);
        if (desc.length() == 0) mCardDesc.setVisibility(View.GONE);
        else mCardDesc.setVisibility(View.VISIBLE);
    }

    private void initFeatures() {
        mFeatureList.setHasFixedSize(false);
        mFeatureList.setNestedScrollingEnabled(false);
        mFeatureList.setItemAnimator(new DefaultItemAnimator());
        mFeatureList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFeatureList.setFocusable(false);

        String[] titles = getActivity().getResources().getStringArray(R.array.home_features);
        TypedArray icons = getActivity().getResources().obtainTypedArray(R.array.home_features_icons);
        String wallpaperUrl = getActivity().getResources().getString(R.string.wallpaper_json);
        List<Feature> features = new ArrayList<>();

        for (int i = 0; i < titles.length; i++) {
            int icon;
            if (i < titles.length && i < icons.length())
                icon = icons.getResourceId(i, -1);
            else icon = R.drawable.ic_feature_others;
            Feature feature = new Feature(icon, titles[i]);

            if (i == 2 || i == 3) {
                if (!URLUtil.isValidUrl(wallpaperUrl)) feature = null;
            }

            if (feature != null) features.add(feature);
        }

        icons.recycle();
        mFeatureList.setAdapter(new HomeFeaturesAdapter(getActivity(), features));
    }

    private void initMoreApps() {
        String link = getActivity().getResources().getString(R.string.google_play_dev);
        if (link.length() == 0) mCardApps.setVisibility(View.GONE);
        else {
            int color = ColorHelper.getAttributeColor(getActivity(),
                    android.R.attr.textColorSecondary);
            Drawable drawable = DrawableHelper.getTintedDrawable(getActivity(),
                    R.drawable.ic_google_play_more_apps, color);
            mAppsIcon.setImageDrawable(drawable);
            mMoreApps.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                    R.drawable.card_item_list_dark : R.drawable.card_item_list);
            mMoreApps.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
            });
        }
    }

}
