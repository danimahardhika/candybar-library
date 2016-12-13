package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;

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

public class WallpaperOptionsFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "candybar.dialog.wallpaper.options";

    private static final String NAME = "name";
    private static final String URL = "url";

    private static WallpaperOptionsFragment newInstance(String url, String name) {
        WallpaperOptionsFragment fragment = new WallpaperOptionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        bundle.putString(NAME, name);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showWallpaperOptionsDialog(FragmentManager fm, String url, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        try {
            DialogFragment dialog = WallpaperOptionsFragment.newInstance(url, name);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException ignored) {}
    }

    private LinearLayout mApply;
    private LinearLayout mSave;
    private ImageView mApplyIcon;
    private ImageView mSaveIcon;

    private String mName;
    private String mUrl;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_wallpaper_options, false);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mApply = (LinearLayout) dialog.findViewById(R.id.apply);
        mSave = (LinearLayout) dialog.findViewById(R.id.save);
        mApplyIcon = (ImageView) dialog.findViewById(R.id.apply_icon);
        mSaveIcon = (ImageView) dialog.findViewById(R.id.save_icon);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mName = getArguments().getString(NAME);
        mUrl = getArguments().getString(URL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(URL);
            mName = savedInstanceState.getString(NAME);
        }

        int color = ColorHelper.getAttributeColor(getActivity(), android.R.attr.textColorPrimary);
        mApplyIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_apply, color));
        mSaveIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_save, color));

        mApply.setOnClickListener(this);
        mApply.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        mSave.setOnClickListener(this);
        mSave.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
        if (id == R.id.apply) {
            WallpaperHelper.applyWallpaper(getActivity(), null, color, mUrl, mName);
        } else if (id == R.id.save) {
            if (PermissionHelper.isPermissionStorageGranted(getActivity())) {
                WallpaperHelper.downloadWallpaper(getActivity(), color, mUrl, mName, -1);
            } else {
                PermissionHelper.requestStoragePermission(getActivity(),
                        PermissionHelper.PERMISSION_STORAGE);
            }
        }
        dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(URL, mUrl);
        outState.putString(NAME, mName);
        super.onSaveInstanceState(outState);
    }

}
