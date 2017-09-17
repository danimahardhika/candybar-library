package com.dm.material.dashboard.candybar.fragments;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.SettingsAdapter;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Language;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.items.Setting;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

public class SettingsFragment extends Fragment {

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        if (!Preferences.get(getActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        initSettings();
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
                if (!Preferences.get(getActivity()).isPremiumRequest()) {
                    Preferences.get(getActivity()).setPremiumRequestProductId(productId);
                    Preferences.get(getActivity()).setPremiumRequestCount(
                            premiumRequestProductsCount[index]);
                    Preferences.get(getActivity()).setPremiumRequestTotal(
                            premiumRequestProductsCount[index]);
                    Preferences.get(getActivity()).setPremiumRequest(true);
                }
            }
        }
        int message = index > -1 ?
                R.string.pref_premium_request_restore_success :
                R.string.pref_premium_request_restore_empty;
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void initSettings() {
        List<Setting> settings = new ArrayList<>();

        double cache = (double) FileHelper.getDirectorySize(getActivity().getCacheDir()) / FileHelper.MB;
        NumberFormat formatter = new DecimalFormat("#0.00");

        settings.add(new Setting(R.drawable.ic_toolbar_storage,
                getActivity().getResources().getString(R.string.pref_data_header),
                "", "", "", Setting.Type.HEADER, -1));

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_data_cache),
                getActivity().getResources().getString(R.string.pref_data_cache_desc),
                String.format(getActivity().getResources().getString(R.string.pref_data_cache_size),
                        formatter.format(cache) + " MB"),
                Setting.Type.CACHE, -1));

        if (getActivity().getResources().getBoolean(R.bool.enable_icon_request) ||
                Preferences.get(getActivity()).isPremiumRequestEnabled() &&
                        !getActivity().getResources().getBoolean(R.bool.enable_icon_request_limit)) {
            settings.add(new Setting(-1, "",
                    getActivity().getResources().getString(R.string.pref_data_request),
                    getActivity().getResources().getString(R.string.pref_data_request_desc),
                    "", Setting.Type.ICON_REQUEST, -1));
        }

        if (Preferences.get(getActivity()).isPremiumRequestEnabled()) {
            settings.add(new Setting(R.drawable.ic_toolbar_premium_request,
                    getActivity().getResources().getString(R.string.pref_premium_request_header),
                    "", "", "", Setting.Type.HEADER, -1));

            settings.add(new Setting(-1, "",
                    getActivity().getResources().getString(R.string.pref_premium_request_restore),
                    getActivity().getResources().getString(R.string.pref_premium_request_restore_desc),
                    "", Setting.Type.RESTORE, -1));

            settings.add(new Setting(-1, "",
                    getActivity().getResources().getString(R.string.pref_premium_request_rebuild),
                    getActivity().getResources().getString(R.string.pref_premium_request_rebuild_desc),
                    "", Setting.Type.PREMIUM_REQUEST, -1));
        }

        if (CandyBarApplication.getConfiguration().isDashboardThemingEnabled()) {
            settings.add(new Setting(R.drawable.ic_toolbar_theme,
                    getActivity().getResources().getString(R.string.pref_theme_header),
                    "", "", "", Setting.Type.HEADER, -1));

            settings.add(new Setting(-1, "",
                    getActivity().getResources().getString(R.string.pref_theme_dark),
                    getActivity().getResources().getString(R.string.pref_theme_dark_desc),
                    "", Setting.Type.THEME, Preferences.get(getActivity()).isDarkTheme() ? 1 : 0));
        }

        if (WallpaperHelper.getWallpaperType(getActivity()) == WallpaperHelper.CLOUD_WALLPAPERS) {
            settings.add(new Setting(R.drawable.ic_toolbar_wallpapers,
                    getActivity().getResources().getString(R.string.pref_wallpaper_header),
                    "", "", "", Setting.Type.HEADER, -1));

            settings.add(new Setting(-1, "",
                    getActivity().getResources().getString(R.string.pref_wallpaper_location),
                    WallpaperHelper.getDefaultWallpapersDirectory(getActivity()).toString(), "",
                    Setting.Type.WALLPAPER, -1));
        }

        settings.add(new Setting(R.drawable.ic_toolbar_language,
                getActivity().getResources().getString(R.string.pref_language_header),
                "", "", "", Setting.Type.HEADER, -1));

        Language language = LocaleHelper.getCurrentLanguage(getActivity());
        settings.add(new Setting(-1, "",
                language.getName(),
                "", "", Setting.Type.LANGUAGE, -1));

        settings.add(new Setting(R.drawable.ic_toolbar_others,
                getActivity().getResources().getString(R.string.pref_others_header),
                "", "", "", Setting.Type.HEADER, -1));

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_others_changelog),
                "", "", Setting.Type.CHANGELOG, -1));

        if (getActivity().getResources().getBoolean(R.bool.enable_apply)) {
            settings.add(new Setting(-1, "",
                    getActivity().getResources().getString(R.string.pref_others_report_bugs),
                    "", "", Setting.Type.REPORT_BUGS, -1));
        }

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_others_reset_tutorial),
                "", "", Setting.Type.RESET_TUTORIAL, -1));

        mRecyclerView.setAdapter(new SettingsAdapter(getActivity(), settings));
    }

    public void rebuildPremiumRequest() {
        new AsyncTask<Void, Void, Boolean>() {

            MaterialDialog dialog;
            List<Request> requests;
            String log = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
                builder.typeface(
                        TypefaceHelper.getMedium(getActivity()),
                        TypefaceHelper.getRegular(getActivity()));
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
                        File directory = getActivity().getCacheDir();
                        requests = Database.get(getActivity()).getPremiumRequest(null);
                        if (requests.size() == 0) return true;

                        File appFilter = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.APPFILTER);
                        File appMap = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.APPMAP);
                        File themeResources = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.THEME_RESOURCES);
                        List<String> files = new ArrayList<>();

                        for (int i = 0; i < requests.size(); i++) {
                            Drawable drawable = DrawableHelper.getHighQualityIcon(
                                    getActivity(), requests.get(i).getPackageName());
                            String icon = IconsHelper.saveIcon(files, directory, drawable, requests.get(i).getName());
                            if (icon != null) files.add(icon);
                        }

                        if (appFilter != null) {
                            files.add(appFilter.toString());
                        }

                        if (appMap != null) {
                            files.add(appMap.toString());
                        }

                        if (themeResources != null) {
                            files.add(themeResources.toString());
                        }
                        CandyBarApplication.sZipPath = FileHelper.createZip(files, new File(directory.toString(),
                                RequestHelper.getGeneratedZipName(RequestHelper.REBUILD_ZIP)));
                        return true;
                    } catch (Exception e) {
                        log = e.toString();
                        LogUtil.e(Log.getStackTraceString(e));
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

                    IntentChooserFragment.showIntentChooserDialog(getActivity()
                            .getSupportFragmentManager(), IntentChooserFragment.REBUILD_ICON_REQUEST);
                } else {
                    Toast.makeText(getActivity(), "Failed: " +log, Toast.LENGTH_LONG).show();
                }
                dialog = null;
            }
        }.execute();
    }
}
