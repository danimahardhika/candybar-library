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
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.LauncherAdapter;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Launcher;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.views.AutoFitRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class ApplyFragment extends Fragment implements View.OnClickListener {

    private CardView mApplyTips;
    private TextView mGotIt;
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
        mApplyTips = (CardView) view.findViewById(R.id.apply_tips);
        mGotIt = (TextView) view.findViewById(R.id.gotit);
        mNoLauncher = (TextView) view.findViewById(R.id.no_launcher);
        mInstalledGrid = (AutoFitRecyclerView) view.findViewById(R.id.installed_grid);
        mSupportedGrid = (AutoFitRecyclerView) view.findViewById(R.id.supported_grid);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mScrollView, false);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);

        mGotIt.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.button_accent_dark : R.drawable.button_accent);

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
        super.onDestroy();
        if (mGetLaunchers != null) mGetLaunchers.cancel(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.gotit) {
            Preferences.getPreferences(getActivity()).showApplyTips(false);
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            fadeOut.setDuration(400);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mApplyTips.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mApplyTips.startAnimation(fadeOut);
        }
    }

    private void initApplyTips() {
        if (!Preferences.getPreferences(getActivity()).isShowApplyTips())
            mApplyTips.setVisibility(View.GONE);
        else {
            mGotIt.setOnClickListener(this);
        }
    }

    private void getLaunchers() {
        mGetLaunchers = new AsyncTask<Void, Void, Boolean>() {

            String[] launcherNames;
            TypedArray launcherIcons;
            String[] launcherPackages1;
            String[] launcherPackages2;
            String[] launcherPackages3;

            List<Launcher> installed;
            List<Launcher> supported;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                installed = new ArrayList<>();
                supported = new ArrayList<>();
                launcherNames = getActivity().getResources().getStringArray(
                        R.array.launcher_names);
                launcherIcons = getActivity().getResources().obtainTypedArray(
                        R.array.launcher_icons);
                launcherPackages1 = getActivity().getResources().getStringArray(
                        R.array.launcher_packages_1);
                launcherPackages2 = getActivity().getResources().getStringArray(
                        R.array.launcher_packages_2);
                launcherPackages3 = getActivity().getResources().getStringArray(
                        R.array.launcher_packages_3);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
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

                            Launcher launcher = new Launcher(icon, launcherNames[i], launcherPackage);
                            if (isInstalled) installed.add(launcher);
                            else supported.add(launcher);
                        }

                        try {
                            Collections.sort(installed, LauncherNameComparator);
                        } catch (Exception ignored) {}

                        try {
                            Collections.sort(supported, LauncherNameComparator);
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
                    if (installed.size() > 0)
                        mInstalledGrid.setAdapter(new LauncherAdapter(getActivity(), installed));
                    else mNoLauncher.setVisibility(View.VISIBLE);

                   mSupportedGrid.setAdapter(new LauncherAdapter(getActivity(), supported));
                }
                if (launcherIcons != null) launcherIcons.recycle();
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

    private Comparator<Launcher> LauncherNameComparator = (launcher, launcher1) -> {
        String name = launcher.getName();
        String name1 = launcher1.getName();
        return name.compareTo(name1);
    };

}
