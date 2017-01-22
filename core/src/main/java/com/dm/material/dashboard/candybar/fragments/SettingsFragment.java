package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
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
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.DeviceHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private LinearLayout mClearCache;
    private TextView mCacheSize;
    private LinearLayout mDarkTheme;
    private AppCompatCheckBox mDarkThemeCheck;
    private LinearLayout mRestorePurchases;
    private LinearLayout mRebuildRequest;
    private TextView mWallsDirectory;
    private NestedScrollView mScrollView;

    private File mCache;

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
        mRebuildRequest = (LinearLayout) view.findViewById(R.id.pref_rebuild_premium_request);
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

        mRebuildRequest.setOnClickListener(this);
        mRebuildRequest.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        if (!Preferences.getPreferences(getActivity()).isPremiumRequestEnabled()) {
            mRestorePurchases.setVisibility(View.GONE);
            mRebuildRequest.setVisibility(View.GONE);
            View view = getActivity().findViewById(R.id.pref_restore_purchases_divider);
            if (view != null) view.setVisibility(View.GONE);
            View view2 = getActivity().findViewById(R.id.pref_rebuild_premium_request_divider);
            if (view2 != null) view2.setVisibility(View.GONE);
        }

        if (Preferences.getPreferences(getActivity()).getWallsDirectory().length() > 0) {
            String directory = Preferences.getPreferences(
                    getActivity()).getWallsDirectory() + File.separator;
            mWallsDirectory.setText(directory);
        }

        initSettings();
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
                            initSettings();

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
        } else if (id == R.id.pref_rebuild_premium_request) {
            rebuildPremiumRequest();
        }
    }

    public void restorePurchases(List<String> productsId, String[]premiumRequestProductsId,
                                 int[] premiumRequestProductsCount) {
        int index = -1;
        for (String productId : productsId) {
            for (int i = 0; i < premiumRequestProductsId.length; i ++) {
                if (premiumRequestProductsId[i].equals(productId)) {
                    index = i;
                    break;
                }
            }
            if (index > -1 && index < premiumRequestProductsCount.length) {
                if (!Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                    Preferences.getPreferences(getActivity()).setPremiumRequestProductId(productId);
                    Preferences.getPreferences(getActivity()).setPremiumRequestCount(
                            premiumRequestProductsCount[index]);
                    Preferences.getPreferences(getActivity()).setPremiumRequest(true);
                }
            }
        }
        int message = index > -1 ?
                R.string.pref_restore_purchases_success :
                R.string.pref_restore_purchases_empty;
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void initSettings() {
        mCache = new File(getActivity().getCacheDir().toString());

        double cache = (double) cacheSize(mCache)/1024/1024;
        NumberFormat formatter = new DecimalFormat("#0.00");
        String cacheSize = getActivity().getResources().getString(
                R.string.pref_data_cache_size)
                +" "+ (formatter.format(cache)) + " MB";

        mCacheSize.setText(cacheSize);
        mDarkThemeCheck.setChecked(Preferences.getPreferences(getActivity()).isDarkTheme());
    }

    private void clearCache(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                clearCache(child);
        fileOrDirectory.delete();
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

    private void rebuildPremiumRequest() {
        new AsyncTask<Void, Void, Boolean>() {

            MaterialDialog dialog;
            StringBuilder sb;
            SparseArrayCompat<Request> requests;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb = new StringBuilder();

                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
                builder.content(R.string.premium_request_rebuilding);
                builder.cancelable(false);
                builder.canceledOnTouchOutside(false);
                builder.progress(true, 0);
                builder.progressIndeterminateStyle(true);
                dialog = builder.build();
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Database database = new Database(getActivity());
                        requests = database.getPremiumRequest();
                        sb.append(DeviceHelper.getDeviceInfo(getActivity()));

                        for (int i = 0; i < requests.size(); i++) {
                            sb.append("\n\nOrder Id : ").append(requests.get(i).getOrderId())
                                    .append("\nProduct Id : ").append(requests.get(i).getProductId())
                                    .append(requests.get(i).getRequest());
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
                dialog.dismiss();
                if (aBoolean) {
                    if (requests.size() == 0) {
                        Toast.makeText(getActivity(), R.string.premium_request_rebuilding_empty,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String subject = "Rebuild Premium Icon Request "+
                            getActivity().getResources().getString(R.string.app_name);

                    Request request = new Request(subject, sb.toString(), "");
                    IntentChooserFragment.showIntentChooserDialog(getActivity()
                            .getSupportFragmentManager(), request);
                }
                dialog = null;
                sb.setLength(0);
            }

        }.execute();
    }

}
