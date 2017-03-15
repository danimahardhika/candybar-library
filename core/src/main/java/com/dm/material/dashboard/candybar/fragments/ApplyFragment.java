package com.dm.material.dashboard.candybar.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.LauncherAdapter;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.LogUtil;

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

public class ApplyFragment extends Fragment{

    private RecyclerView mRecyclerView;

    private AsyncTask<Void, Void, Boolean> mGetLaunchers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.apply_column_count)));

        getLaunchers();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetSpanSizeLookUp();
    }

    @Override
    public void onDestroy() {
        if (mGetLaunchers != null) mGetLaunchers.cancel(true);
        super.onDestroy();
    }

    private void resetSpanSizeLookUp() {
        int column = getActivity().getResources().getInteger(R.integer.apply_column_count);
        LauncherAdapter adapter = (LauncherAdapter) mRecyclerView.getAdapter();
        GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();

        try {
            manager.setSpanCount(column);

            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position == adapter.getFirstHeaderPosition() || position == adapter.getLastHeaderPosition())
                        return column;
                    return 1;
                }
            });
        } catch (Exception ignored) {}
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

    private void getLaunchers() {
        mGetLaunchers = new AsyncTask<Void, Void, Boolean>() {

            List<Icon> launchers;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                launchers = new ArrayList<>();
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

                        List<Icon> installed = new ArrayList<>();
                        List<Icon> supported = new ArrayList<>();

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

                        if (installed.size() > 0) {
                            launchers.add(new Icon(getActivity().getResources().getString(
                                    R.string.apply_installed), -1, null));
                        }

                        launchers.addAll(installed);
                        launchers.add(new Icon(getActivity().getResources().getString(
                                R.string.apply_supported), -2, null));
                        launchers.addAll(supported);

                        launcherIcons.recycle();
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
                if (aBoolean) {
                    mRecyclerView.setAdapter(new LauncherAdapter(getActivity(), launchers));
                    resetSpanSizeLookUp();
                }
                mGetLaunchers = null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
