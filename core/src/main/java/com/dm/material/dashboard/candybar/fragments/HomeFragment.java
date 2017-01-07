package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.HomeFeaturesAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.LauncherHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;

import org.sufficientlysecure.htmltextview.HtmlTextView;

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

public class HomeFragment extends Fragment {

    private RecyclerView mFeatureList;
    private NestedScrollView mScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mFeatureList = (RecyclerView) view.findViewById(R.id.home_feature_list);
        mScrollView = (NestedScrollView) view.findViewById(R.id.scrollview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_rate) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        initQuickApply();
    }

    public void showOptionsMenu(boolean show) {
        setHasOptionsMenu(show);
    }

    private void initDescription() {
        String desc = getActivity().getResources().getString(R.string.home_description);
        CardView cardDesc = (CardView) getActivity().findViewById(R.id.card_desc);
        if (desc.length() == 0) {
            cardDesc.setVisibility(View.GONE);
            return;
        }

        cardDesc.setVisibility(View.VISIBLE);
        HtmlTextView description = (HtmlTextView) getActivity().findViewById(R.id.home_description);
        description.setHtml(desc);
    }

    private void initQuickApply() {
        if (getActivity().getResources().getBoolean(R.bool.enable_quick_apply)) {
            String[] packageInfo = LauncherHelper.getDefaultLauncher(getActivity());
            if (packageInfo == null) return;
            int id = LauncherHelper.getLauncherId(packageInfo[0]);
            if (id == LauncherHelper.UNKNOWN) return;

            CardView cardQuickApply = (CardView) getActivity().findViewById(R.id.card_quick_apply);
            cardQuickApply.setVisibility(View.VISIBLE);

            int color = ColorHelper.getAttributeColor(getActivity(),
                    android.R.attr.textColorSecondary);
            Drawable drawable = DrawableHelper.getTintedDrawable(
                    getActivity(), R.drawable.ic_home_quick_apply, color);
            ImageView quickApplyIcon = (ImageView) getActivity().findViewById(R.id.quick_apply_icon);
            quickApplyIcon.setImageDrawable(drawable);

            LinearLayout quickApply = (LinearLayout) getActivity().findViewById(R.id.quick_apply);
            quickApply.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                    R.drawable.card_item_list_dark : R.drawable.card_item_list);
            quickApply.setOnClickListener(view ->
                    LauncherHelper.apply(getActivity(), packageInfo[0], packageInfo[1]));

            String text = getActivity().getResources().getString(R.string.quick_apply_desc) +" "+
                    getActivity().getResources().getString(R.string.app_name) +" "+
                    getActivity().getResources().getString(R.string.quick_apply_desc_1);

            TextView quickApplyText = (TextView) getActivity().findViewById(R.id.quick_apply_text);
            if (quickApplyText != null) quickApplyText.setText(text);
        }
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
        SparseArrayCompat<Icon> features = new SparseArrayCompat<>();

        for (int i = 0; i < titles.length; i++) {
            int icon;
            if (i < titles.length && i < icons.length())
                icon = icons.getResourceId(i, -1);
            else icon = R.drawable.ic_feature_others;
            Icon feature = new Icon(titles[i], icon);

            if (i == 2 || i == 3) {
                if (!URLUtil.isValidUrl(wallpaperUrl)) feature = null;
            }

            if (feature != null) features.append(features.size(), feature);
        }

        icons.recycle();
        mFeatureList.setAdapter(new HomeFeaturesAdapter(getActivity(), features));
    }

    private void initMoreApps() {
        String link = getActivity().getResources().getString(R.string.google_play_dev);
        if (link.length() > 0) {
            CardView cardApps = (CardView) getActivity().findViewById(R.id.card_more_apps);
            cardApps.setVisibility(View.VISIBLE);

            int color = ColorHelper.getAttributeColor(getActivity(),
                    android.R.attr.textColorSecondary);
            Drawable drawable = DrawableHelper.getTintedDrawable(getActivity(),
                    R.drawable.ic_google_play_more_apps, color);
            ImageView appsIcon = (ImageView) getActivity().findViewById(R.id.more_apps_icon);
            appsIcon.setImageDrawable(drawable);

            LinearLayout moreApps = (LinearLayout) getActivity().findViewById(R.id.more_apps);
            moreApps.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                    R.drawable.card_item_list_dark : R.drawable.card_item_list);
            moreApps.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
            });
        }
    }

}
