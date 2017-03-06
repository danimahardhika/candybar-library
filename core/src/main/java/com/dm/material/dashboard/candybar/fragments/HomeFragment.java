package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.HomeFeaturesAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Feature;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
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

public class HomeFragment extends Fragment {

    private RecyclerView mFeatureList;
    private NestedScrollView mScrollView;
    private HtmlTextView mDescription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mFeatureList = (RecyclerView) view.findViewById(R.id.home_feature_list);
        mScrollView = (NestedScrollView) view.findViewById(R.id.scrollview);
        mDescription = (HtmlTextView) view.findViewById(R.id.home_description);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);

        initDescription();
        initFeatures();
        initMoreApps();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(),
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
        mDescription.setHtml(desc);
    }

    private void initFeatures() {
        mFeatureList.setHasFixedSize(false);
        mFeatureList.setNestedScrollingEnabled(false);
        mFeatureList.setItemAnimator(new DefaultItemAnimator());
        mFeatureList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFeatureList.setFocusable(false);

        List<Feature> features = new ArrayList<>();

        features.add(new Feature(
                ContextCompat.getColor(getActivity(), R.color.homeFeatureIcons),
                String.format(getActivity().getResources().getString(R.string.home_feature_icons), CandyBarMainActivity.sIconsCount)));

        if (CandyBarMainActivity.sSections != null) {
            if (CandyBarMainActivity.sSections.size() > 0) {
                List<Icon> icons =  CandyBarMainActivity.sSections.get(0).getIcons();
                if (icons.size() > 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(
                            getActivity().getResources(), icons.get(0).getRes(), options);
                    features.add(new Feature(
                            ContextCompat.getColor(getActivity(), R.color.homeFeatureDimensions),
                            String.format(getActivity().getResources().getString(R.string.home_feature_dimensions),
                                    options.outWidth +" x "+ options.outHeight)));
                }
            }
        }

        if (WallpaperHelper.getWallpaperType(getActivity()) == WallpaperHelper.CLOUD_WALLPAPERS) {
            features.add(new Feature(
                    ContextCompat.getColor(getActivity(), R.color.homeFeatureCloudWallpapers),
                    String.format(getActivity().getResources().getString(R.string.home_feature_cloud_wallpaper),
                            Preferences.getPreferences(getActivity()).getAvailableWallpapersCount())));
            features.add(new Feature(
                    ContextCompat.getColor(getActivity(), R.color.homeFeatureMuzei),
                    getActivity().getResources().getString(R.string.home_feature_muzei)));
        }

        String[] launchers = getActivity().getResources().getStringArray(R.array.launcher_names);
        features.add(new Feature(
                ContextCompat.getColor(getActivity(), R.color.homeFeatureLaunchers),
                String.format(getActivity().getResources().getString(R.string.home_feature_launchers),
                        launchers.length)));

        if (getActivity().getResources().getBoolean(R.bool.enable_icon_request)) {
            String string = getActivity().getResources().getString(R.string.home_features_icon_request);
            if (Preferences.getPreferences(getActivity()).isPremiumRequestEnabled())
                string += " "+ getActivity().getResources().getString(R.string.home_features_icon_request_with)
                        +" "+ getActivity().getResources().getString(R.string.home_features_icon_request_premium);
            features.add(new Feature(
                    ContextCompat.getColor(getActivity(), R.color.homeFeaturesIconRequest),
                    string));
        } else if (Preferences.getPreferences(getActivity()).isPremiumRequestEnabled()) {
            features.add(new Feature(
                    ContextCompat.getColor(getActivity(), R.color.homeFeaturesIconRequest),
                    getActivity().getResources().getString(R.string.home_features_icon_request_premium)));
        }

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
            moreApps.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
            });
        }
    }

    public void resetWallpapersCount() {
        if (WallpaperHelper.getWallpaperType(getActivity()) == WallpaperHelper.CLOUD_WALLPAPERS) {
            if (mFeatureList == null) return;
            HomeFeaturesAdapter adapter = (HomeFeaturesAdapter) mFeatureList.getAdapter();
            adapter.resetWallpapersCount(Preferences.getPreferences(
                    getActivity()).getAvailableWallpapersCount());
        }
    }
}
