package com.dm.material.dashboard.candybar.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.LauncherAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.views.AutoFitRecyclerView;

import java.util.ArrayList;
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

public class ApplyFragment extends Fragment implements View.OnClickListener {

    private TextView mNoLauncher;
    private AutoFitRecyclerView mInstalledGrid;
    private AutoFitRecyclerView mSupportedGrid;
    private NestedScrollView mScrollView;

    private AsyncTask<Void, Void, Boolean> mGetLaunchers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply, container, false);
        mScrollView = (NestedScrollView) view.findViewById(R.id.scrollview);
        mNoLauncher = (TextView) view.findViewById(R.id.no_launcher);
        mInstalledGrid = (AutoFitRecyclerView) view.findViewById(R.id.installed_grid);
        mSupportedGrid = (AutoFitRecyclerView) view.findViewById(R.id.supported_grid);

        if (Preferences.getPreferences(getActivity()).isShowApplyTips()) {
            LinearLayout applyTips = (LinearLayout) view.findViewById(
                    R.id.apply_tips_bar);
            applyTips.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mScrollView, false);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);

        mInstalledGrid.setHasFixedSize(false);
        mInstalledGrid.setNestedScrollingEnabled(false);
        mInstalledGrid.setItemAnimator(new DefaultItemAnimator());
        mInstalledGrid.getLayoutManager().setAutoMeasureEnabled(true);

        mSupportedGrid.setHasFixedSize(false);
        mSupportedGrid.setNestedScrollingEnabled(false);
        mSupportedGrid.setItemAnimator(new DefaultItemAnimator());
        mSupportedGrid.getLayoutManager().setAutoMeasureEnabled(true);

        initApplyTips();
        getLaunchers();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(), mScrollView, newConfig.orientation);
    }

    @Override
    public void onDestroy() {
        if (mGetLaunchers != null) mGetLaunchers.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.apply_tips_dismiss) {
            LinearLayout applyTips = (LinearLayout) getActivity()
                    .findViewById(R.id.apply_tips_bar);
            applyTips.setVisibility(View.GONE);
            Preferences.getPreferences(getActivity()).showApplyTips(false);
        }
    }

    private void initApplyTips() {
        if (!Preferences.getPreferences(getActivity()).isShowApplyTips()) return;

        int toolbarIcon = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        TextView desc = (TextView) getActivity().findViewById(R.id.apply_tips_desc);
        desc.setTextColor(ColorHelper.setColorAlpha(toolbarIcon, 0.6f));
    }
    private void initApplyTipsFab() {
        if (!Preferences.getPreferences(getActivity()).isShowApplyTips()) return;

        int accent = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
        int textColor = ColorHelper.getTitleTextColor(accent);
        AppCompatButton dismiss = (AppCompatButton) getActivity()
                .findViewById(R.id.apply_tips_dismiss);
        dismiss.setTextColor(textColor);
        dismiss.setOnClickListener(this);
    }


    private void getLaunchers() {
        mGetLaunchers = new AsyncTask<Void, Void, Boolean>() {

            List<Icon> installed;
            List<Icon> supported;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                installed = new ArrayList<>();
                supported = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        String[] launcherNames = getActivity().getResources().getStringArray(
                                R.array.launcher_names);
                        TypedArray launcherIcons = getActivity().getResources().obtainTypedArray(
                                R.array.launcher_icons);
                        String[] launcherPackages1 = getActivity().getResources().getStringArray(
                                R.array.launcher_packages_1);
                        String[] launcherPackages2 = getActivity().getResources().getStringArray(
                                R.array.launcher_packages_2);
                        String[] launcherPackages3 = getActivity().getResources().getStringArray(
                                R.array.launcher_packages_3);

                        for (int i = 0; i < launcherNames.length; i++) {
                            boolean isInstalled = isLauncherInstalled(
                                    launcherPackages1[i],
                                    launcherPackages2[i],
                                    launcherPackages3[i]);

                            int icon = R.drawable.ic_app_default;
                            if (i < launcherIcons.length())
                                icon = launcherIcons.getResourceId(i, icon);

                            String launcherPackage = launcherPackages1[i];
                            if (launcherPackages1[i].equals("com.lge.launcher2")) {
                                boolean lghome3 = isPackageInstalled(launcherPackages2[i]);
                                if (lghome3) launcherPackage = launcherPackages2[i];
                            }

                            Icon launcher = new Icon(launcherNames[i], icon, launcherPackage);
                            if (isInstalled) installed.add(launcher);
                            else supported.add(launcher);
                        }

                        try {
                            Collections.sort(installed, new AlphanumComparator() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    String s1 = ((Icon) o1).getTitle();
                                    String s2 = ((Icon) o2).getTitle();
                                    return super.compare(s1, s2);
                                }
                            });
                        } catch (Exception ignored) {}

                        try {
                            Collections.sort(supported, new AlphanumComparator() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    String s1 = ((Icon) o1).getTitle();
                                    String s2 = ((Icon) o2).getTitle();
                                    return super.compare(s1, s2);
                                }
                            });
                        } catch (Exception ignored) {}

                        launcherIcons.recycle();
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
                initApplyTipsFab();
                if (aBoolean) {
                    if (installed.size() > 0)
                        mInstalledGrid.setAdapter(new LauncherAdapter(getActivity(), installed));
                    else mNoLauncher.setVisibility(View.VISIBLE);

                   mSupportedGrid.setAdapter(new LauncherAdapter(getActivity(), supported));
                }
                mGetLaunchers = null;
            }
        }.execute();
    }

    private boolean isPackageInstalled(String pkg) {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(
                    pkg, PackageManager.GET_ACTIVITIES);
            return packageInfo != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isLauncherInstalled(String pkg1, String pkg2, String pkg3) {
        return isPackageInstalled(pkg1) | isPackageInstalled(pkg2) | isPackageInstalled(pkg3);
    }

}
