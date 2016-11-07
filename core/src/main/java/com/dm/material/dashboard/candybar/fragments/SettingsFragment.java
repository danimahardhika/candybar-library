package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;

import java.io.File;

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

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private LinearLayout mClearCache;
    private TextView mCacheSize;
    private LinearLayout mDarkTheme;
    private AppCompatCheckBox mDarkThemeCheck;
    private LinearLayout mRestorePurchases;
    private TextView mWallsDirectory;
    private NestedScrollView mScrollView;

    private File mCache;
    private File mCacheExternal;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mClearCache = (LinearLayout) view.findViewById(R.id.pref_cache_clear);
        mCacheSize = (TextView) view.findViewById(R.id.pref_cache_size);
        mDarkTheme = (LinearLayout) view.findViewById(R.id.pref_dark_theme);
        mDarkThemeCheck = (AppCompatCheckBox) view.findViewById(R.id.pref_dark_theme_check);
        mRestorePurchases = (LinearLayout) view.findViewById(R.id.pref_restore_purchases);
        mWallsDirectory = (TextView) view.findViewById(R.id.pref_walls_directory);
        mScrollView = (NestedScrollView) view.findViewById(R.id.scrollview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mScrollView, false);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);

        mClearCache.setOnClickListener(this);
        mClearCache.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        mDarkTheme.setOnClickListener(this);
        mDarkTheme.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        mRestorePurchases.setOnClickListener(this);
        mRestorePurchases.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        if (Preferences.getPreferences(getActivity()).getWallsDirectory().length() > 0) {
            String directory = Preferences.getPreferences(
                    getActivity()).getWallsDirectory() + File.separator;
            mWallsDirectory.setText(directory);
        }

        if (PermissionHelper.isPermissionStorageGranted(getActivity())) {
            initSettings(true);
        } else {
            PermissionHelper.requestStoragePermission(getActivity(),
                    PermissionHelper.PERMISSION_STORAGE_SETTINGS);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomMargin(getActivity(),
                mScrollView, newConfig.orientation);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.pref_cache_clear) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.pref_data_cache)
                    .content(R.string.pref_data_cache_clear_dialog)
                    .positiveText(R.string.clear)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        try {
                            clearCache(mCache);
                            if (PermissionHelper.isPermissionStorageGranted(getActivity())) {
                                if (mCacheExternal != null)
                                    clearCache(mCacheExternal);
                            }

                            String cacheSize = getActivity().getResources().getString(
                                    R.string.pref_data_cache_size)
                                    +" 0 KB";
                            mCacheSize.setText(cacheSize);

                            Toast.makeText(getActivity(), getActivity()
                                            .getResources().getString(
                                            R.string.pref_data_cache_cleared),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        }
                    })
                    .show();
        } else if (id == R.id.pref_dark_theme) {
            Preferences.getPreferences(getActivity()).setDarkTheme(!mDarkThemeCheck.isChecked());
            mDarkThemeCheck.setChecked(!mDarkThemeCheck.isChecked());
            getActivity().recreate();
        } else if (id == R.id.pref_restore_purchases) {
            try {
                InAppBillingListener listener = (InAppBillingListener) getActivity();
                listener.OnRestorePurchases();
            } catch (Exception ignored) {}
        }
    }

    public void initSettings(boolean granted) {
        mCache = new File(getActivity().getCacheDir().toString());

        if (granted) {
            File cacheExternal = getActivity().getExternalCacheDir();
            if (cacheExternal != null) mCacheExternal = new File(cacheExternal.toString());
        }

        long cache = cacheSize(mCache)/1024;
        long cacheEx = 0;
        if (mCacheExternal != null) cacheEx = cacheSize(mCacheExternal)/1024;
        long total = cache + cacheEx;

        String cacheSize = getActivity().getResources().getString(
                R.string.pref_data_cache_size)
                +" "+ (total) + " KB";

        mCacheSize.setText(cacheSize);
        mDarkThemeCheck.setChecked(Preferences.getPreferences(getActivity()).isDarkTheme());
    }

    private void clearCache (File file) {
        File[] list = file.listFiles();
        for (File temp : list) {
            temp.delete();
        }
    }

    private long cacheSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += cacheSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result;
        }
        return 0;
    }

}
